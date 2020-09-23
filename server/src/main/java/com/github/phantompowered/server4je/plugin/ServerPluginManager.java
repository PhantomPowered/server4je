/*
 * This file is part of server4je, licensed under the MIT License (MIT).
 *
 * Copyright (c) PhantomPowered <https://github.com/PhantomPowered>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.phantompowered.server4je.plugin;

import com.github.phantompowered.server4je.common.collect.Iterables;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.*;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerPluginManager implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPluginManager.class);
    private static final short PERM_FALSE = (short) 0;
    private static final short PERM_TRUE = (short) 1;

    private final Object2ObjectMap<String, Plugin> loadedPlugins = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private final Object2ObjectMap<String, Permission> permissions = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private final Short2ObjectMap<ObjectSet<Permission>> defaultPermissions = Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());
    private final Object2ObjectMap<String, Object2BooleanMap<Permissible>> permissionSubscriptions = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private final Object2ObjectMap<Boolean, Object2BooleanMap<Permissible>> defaultPermissionSubscriptions = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private final Object2ObjectMap<Pattern, PluginLoader> filePatternAssociations = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

    @Override
    public void registerInterface(@NotNull Class<? extends PluginLoader> aClass) throws IllegalArgumentException {
        PluginLoader instance;
        try {
            instance = aClass.getDeclaredConstructor(Server.class).newInstance(Bukkit.getServer());
        } catch (NoSuchMethodException exception) {
            throw new IllegalArgumentException("Plugin loader " + aClass.getName() + " does not have a constructor with org.bukkit.Server as argument");
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalArgumentException("Unable to create a new instance of plugin loader " + aClass.getName(), exception);
        }

        for (Pattern pluginFileFilter : instance.getPluginFileFilters()) {
            this.filePatternAssociations.put(pluginFileFilter, instance);
        }
    }

    @Override
    @Nullable
    public Plugin getPlugin(@NotNull String s) {
        return this.loadedPlugins.get(s);
    }

    @Override
    @NotNull
    public Plugin[] getPlugins() {
        return this.loadedPlugins.values().toArray(new Plugin[0]);
    }

    @Override
    public boolean isPluginEnabled(@NotNull String s) {
        return this.isPluginEnabled(this.getPlugin(s));
    }

    @Override
    public boolean isPluginEnabled(@Nullable Plugin plugin) {
        return plugin != null && this.loadedPlugins.containsValue(plugin) && plugin.isEnabled();
    }

    @Override
    @Nullable
    public Plugin loadPlugin(@NotNull File file) throws InvalidPluginException, UnknownDependencyException {
        PluginLoader pluginLoader = this.forFileName(file.getName());
        if (pluginLoader != null) {
            Plugin plugin = pluginLoader.loadPlugin(file);
            if (!this.loadedPlugins.containsKey(plugin.getName())) {
                this.loadedPlugins.put(plugin.getName(), plugin);
                return plugin;
            }
        }

        return null;
    }

    @Override
    @NotNull
    public Plugin[] loadPlugins(@NotNull File file) {
        Collection<FileLoaderWrapper> descriptionFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(file.toPath())) {
            for (Path path : stream) {
                PluginLoader pluginLoader = this.forFileName(path.getFileName().toString());
                if (pluginLoader == null) {
                    continue;
                }

                try {
                    descriptionFiles.add(new FileLoaderWrapper(pluginLoader.getPluginDescription(path.toFile()), pluginLoader, path));
                } catch (InvalidDescriptionException exception) {
                    LOGGER.error("Unable to load plugin " + path.toString() + " because of broken description file!", exception);
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Unable to load plugins", exception);
        }

        Plugin[] loaded = this.detectPlugins(descriptionFiles).toArray(Plugin[]::new);
        for (Plugin plugin : loaded) {
            this.loadedPlugins.put(plugin.getName(), plugin);
        }

        return loaded;
    }

    @NotNull
    private Collection<Plugin> detectPlugins(@NotNull Collection<FileLoaderWrapper> descriptionFiles) {
        Deque<FileLoaderWrapper> targetPluginLoads = new LinkedList<>();
        for (FileLoaderWrapper descriptionFile : descriptionFiles) {
            targetPluginLoads.push(descriptionFile);
            if (this.detectDepends(descriptionFile, targetPluginLoads, descriptionFiles)) {
                return Collections.emptyList();
            }

            this.detectLoadBefore(descriptionFile, descriptionFiles);
            this.detectSoftDepends(descriptionFile, targetPluginLoads, descriptionFiles);
        }

        Collection<Plugin> loadedPlugins = new ArrayList<>();
        while (!targetPluginLoads.isEmpty()) {
            FileLoaderWrapper wrapper = targetPluginLoads.pop();
            if (wrapper == null || Iterables.anyMatch(loadedPlugins, p -> p.getName().equals(wrapper.file.getName()))) {
                continue;
            }

            try {
                loadedPlugins.add(wrapper.loader.loadPlugin(wrapper.path.toFile()));
            } catch (InvalidPluginException exception) {
                throw new RuntimeException("Exception loading plugin " + wrapper.file.getName(), exception);
            }
        }

        return loadedPlugins;
    }

    private void detectLoadBefore(@NotNull FileLoaderWrapper current, @NotNull Collection<FileLoaderWrapper> loaderWrappers) {
        for (String s : current.file.getLoadBefore()) {
            FileLoaderWrapper wrapper = Iterables.first(loaderWrappers, file -> file.file.getName().equals(s)).orElse(null);
            if (wrapper == null) {
                continue;
            }

            wrapper.file.getSoftDepend().add(s);
        }
    }

    private void detectSoftDepends(@NotNull FileLoaderWrapper current, @NotNull Deque<FileLoaderWrapper> plugins, @NotNull Collection<FileLoaderWrapper> loaderWrappers) {
        for (String s : current.file.getSoftDepend()) {
            FileLoaderWrapper wrapper = Iterables.first(loaderWrappers, file -> file.file.getName().equals(s)).orElse(null);
            if (wrapper == null) {
                LOGGER.warn("Missing soft depend " + s + " for plugin " + current.file.getName());
                continue;
            }

            plugins.push(wrapper);
        }
    }

    private boolean detectDepends(@NotNull FileLoaderWrapper current, @NotNull Deque<FileLoaderWrapper> plugins, @NotNull Collection<FileLoaderWrapper> loaderWrappers) {
        for (String s : current.file.getDepend()) {
            FileLoaderWrapper wrapper = Iterables.first(loaderWrappers, file -> file.file.getName().equals(s)).orElse(null);
            if (wrapper == null) {
                LOGGER.warn("Missing depend " + s + " for plugin " + current.file.getName());
                return true;
            }

            plugins.push(wrapper);
        }

        return false;
    }

    @Nullable
    private PluginLoader forFileName(@NotNull String fileName) {
        for (Map.Entry<Pattern, PluginLoader> entry : this.filePatternAssociations.entrySet()) {
            Matcher matcher = entry.getKey().matcher(fileName);
            if (matcher.find()) {
                return entry.getValue();
            }
        }

        return null;
    }

    @Override
    public void disablePlugins() {
        for (Plugin value : this.loadedPlugins.values()) {
            this.disablePlugin(value);
        }
    }

    @Override
    public void clearPlugins() {
        this.disablePlugins();
        this.loadedPlugins.clear();
    }

    @Override
    public void callEvent(@NotNull Event event) throws IllegalStateException {
        for (RegisteredListener registeredListener : event.getHandlers().getRegisteredListeners()) {
            if (!registeredListener.getPlugin().isEnabled()) {
                continue;
            }

            try {
                registeredListener.callEvent(event);
            } catch (Throwable throwable) {
                LOGGER.error("Unable to post event " + event.getClass().getName() + " to listener of " + registeredListener.getPlugin().getName(), throwable);
            }
        }
    }

    @Override
    public void registerEvents(@NotNull Listener listener, @NotNull Plugin plugin) {
        Preconditions.checkArgument(plugin.isEnabled(), "Disabled plugin tries to register a listener");

        for (var entry : plugin.getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            this.getEventListeners(this.getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }
    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> aClass, @NotNull Listener listener, @NotNull EventPriority eventPriority, @NotNull EventExecutor eventExecutor, @NotNull Plugin plugin) {
        this.registerEvent(aClass, listener, eventPriority, eventExecutor, plugin, false);
    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> aClass, @NotNull Listener listener, @NotNull EventPriority eventPriority, @NotNull EventExecutor eventExecutor, @NotNull Plugin plugin, boolean b) {
        Preconditions.checkArgument(plugin.isEnabled(), "Disabled plugin tries to register a listener");
        this.getEventListeners(aClass).register(new RegisteredListener(listener, eventExecutor, eventPriority, plugin, b));
    }

    private HandlerList getEventListeners(@NotNull Class<? extends Event> type) {
        try {
            Method method = this.getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    private Class<? extends Event> getRegistrationClass(@NotNull Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                && !clazz.getSuperclass().equals(Event.class)
                && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return this.getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));
            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        if (plugin.isEnabled()) {
            return;
        }

        List<Command> parse = PluginCommandYamlParser.parse(plugin);
        if (!parse.isEmpty()) {
            Bukkit.getCommandMap().registerAll(plugin.getName(), parse);
        }

        try {
            plugin.getPluginLoader().enablePlugin(plugin);
        } catch (Throwable throwable) {
            LOGGER.error("Error enabling plugin " + plugin.getName(), throwable);
        }

        HandlerList.bakeAll();
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        this.disablePlugin(plugin, false);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin, boolean b) {
        if (!plugin.isEnabled()) {
            return;
        }

        plugin.getPluginLoader().disablePlugin(plugin, b);
        plugin.getServer().getScheduler().cancelTasks(plugin);
        plugin.getServer().getServicesManager().unregisterAll(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        for (World world : plugin.getServer().getWorlds()) {
            world.removePluginChunkTickets(plugin);
        }

        HandlerList.unregisterAll(plugin);
    }

    @Override
    @Nullable
    public Permission getPermission(@NotNull String s) {
        return this.permissions.get(s);
    }

    @Override
    public void addPermission(@NotNull Permission permission) {
        String name = permission.getName().toLowerCase();
        Preconditions.checkArgument(!this.permissions.containsKey(name), "Permission is already defined");

        this.permissions.put(name, permission);
        this.calculatePermission(permission);
    }

    @Override
    public void removePermission(@NotNull Permission permission) {
        if (this.permissions.remove(permission.getName()) != null) {
            this.calculatePermission(permission);
        }
    }

    @Override
    public void removePermission(@NotNull String s) {
        Permission permission = this.getPermission(s);
        if (permission != null) {
            this.removePermission(permission);
        }
    }

    @Override
    @NotNull
    public Set<Permission> getDefaultPermissions(boolean b) {
        return ImmutableSet.copyOf(this.defaultPermissions.get(b ? PERM_TRUE : PERM_FALSE));
    }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission permission) {
        if (this.permissions.containsKey(permission.getName().toLowerCase())) {
            this.defaultPermissions.get(PERM_FALSE).remove(permission);
            this.defaultPermissions.get(PERM_TRUE).remove(permission);

            this.calculatePermission(permission);
        }
    }

    @Override
    public void subscribeToPermission(@NotNull String s, @NotNull Permissible permissible) {
        this.permissionSubscriptions.computeIfAbsent(s.toLowerCase(), k -> Object2BooleanMaps.synchronize(new Object2BooleanOpenHashMap<>())).put(permissible, true);
    }

    @Override
    public void unsubscribeFromPermission(@NotNull String s, @NotNull Permissible permissible) {
        String name = s.toLowerCase();
        var map = this.permissionSubscriptions.get(name);

        if (map != null) {
            map.remove(permissible, true);
            map.remove(permissible, false);
            if (map.isEmpty()) {
                this.permissionSubscriptions.remove(name);
            }
        }
    }

    @Override
    @NotNull
    public Set<Permissible> getPermissionSubscriptions(@NotNull String s) {
        var map = this.permissionSubscriptions.get(s.toLowerCase());
        return map == null ? Collections.emptySet() : ImmutableSet.copyOf(map.keySet());
    }

    @Override
    public void subscribeToDefaultPerms(boolean b, @NotNull Permissible permissible) {
        this.defaultPermissionSubscriptions.computeIfAbsent(b, k -> Object2BooleanMaps.synchronize(new Object2BooleanOpenHashMap<>())).put(permissible, true);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean b, @NotNull Permissible permissible) {
        var map = this.defaultPermissionSubscriptions.get(b);
        if (map != null) {
            map.remove(permissible, true);
            map.remove(permissible, false);
            if (map.isEmpty()) {
                this.defaultPermissionSubscriptions.remove(b);
            }
        }
    }

    @Override
    @NotNull
    public Set<Permissible> getDefaultPermSubscriptions(boolean b) {
        var map = this.defaultPermissionSubscriptions.get(b);
        return map == null ? Collections.emptySet() : ImmutableSet.copyOf(map.keySet());
    }

    @Override
    @NotNull
    public Set<Permission> getPermissions() {
        return ImmutableSet.copyOf(this.permissions.values());
    }

    private void calculatePermission(@NotNull Permission permission) {
        if (permission.getDefault() == PermissionDefault.OP || permission.getDefault() == PermissionDefault.TRUE) {
            this.defaultPermissions.get(PERM_TRUE).add(permission);
            this.recalculatePerms(true);
        } else if (permission.getDefault() == PermissionDefault.NOT_OP || permission.getDefault() == PermissionDefault.FALSE) {
            this.defaultPermissions.get(PERM_FALSE).add(permission);
            this.recalculatePerms(false);
        }
    }

    private void recalculatePerms(boolean op) {
        for (Permissible defaultPermSubscription : this.getDefaultPermSubscriptions(op)) {
            defaultPermSubscription.recalculatePermissions();
        }
    }

    @Override
    public boolean useTimings() {
        return false;
    }

    private static final class FileLoaderWrapper {

        private final PluginDescriptionFile file;
        private final PluginLoader loader;
        private final Path path;

        public FileLoaderWrapper(PluginDescriptionFile file, PluginLoader loader, Path path) {
            this.file = file;
            this.loader = loader;
            this.path = path;
        }
    }
}

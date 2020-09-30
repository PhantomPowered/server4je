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
package com.github.phantompowered.server4je;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.github.phantompowered.server4je.api.PhantomServer;
import com.github.phantompowered.server4je.api.audience.Audience;
import com.github.phantompowered.server4je.api.config.ServerConfig;
import com.github.phantompowered.server4je.api.event.ServerInitDoneEvent;
import com.github.phantompowered.server4je.api.network.NetworkManager;
import com.github.phantompowered.server4je.api.player.OfflinePlayerManager;
import com.github.phantompowered.server4je.api.player.PlayerManager;
import com.github.phantompowered.server4je.api.profile.PhantomPlayerProfile;
import com.github.phantompowered.server4je.api.version.ServerVersion;
import com.github.phantompowered.server4je.authlib.profile.GameProfile;
import com.github.phantompowered.server4je.command.ServerCommandMap;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.config.JsonServerConfig;
import com.github.phantompowered.server4je.eula.Eula;
import com.github.phantompowered.server4je.gson.JsonDataLoader;
import com.github.phantompowered.server4je.network.ServerNettyNetworkManager;
import com.github.phantompowered.server4je.options.ServerCliOptionUtil;
import com.github.phantompowered.server4je.plugin.ServerPluginManager;
import com.github.phantompowered.server4je.scheduler.ServerScheduler;
import com.github.phantompowered.server4je.service.ServerServicesManager;
import com.github.phantompowered.server4je.tick.ServerTicker;
import com.github.phantompowered.server4je.unsafe.ServerUnsafeValues;
import com.github.phantompowered.server4je.version.PhantomServerVersion;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.concurrent.FastThreadLocalThread;
import joptsimple.OptionSet;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.*;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Server4JavaEdition extends PhantomServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server4JavaEdition.class);
    private static final java.util.logging.Logger JAVA_LOGGER = java.util.logging.Logger.getLogger(Server4JavaEdition.class.getSimpleName());

    private final Spigot spigot = new ServerSpigot();
    private final Thread primaryThread = Thread.currentThread();
    private final CommandMap commandMap = new ServerCommandMap();
    private final PluginManager pluginManager = new ServerPluginManager();
    private final BukkitScheduler bukkitScheduler = new ServerScheduler();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ServicesManager servicesManager = new ServerServicesManager();
    private final NetworkManager networkManager = new ServerNettyNetworkManager();

    private final OptionSet options;
    private final ServerVersion serverVersion;
    private final ListeningScheduledExecutorService executorService;

    private ServerConfig serverConfig;

    public Server4JavaEdition(OptionSet options) {
        this.options = options;
        this.executorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicLong threadCount = new AtomicLong();

            @Override
            @NotNull
            public Thread newThread(@NotNull Runnable r) {
                return new FastThreadLocalThread(r, "Server4JE Executor Thread #" + this.threadCount.getAndIncrement());
            }
        }));

        try (var stream = Server4JavaEdition.class.getClassLoader().getResourceAsStream("version_data.json")) {
            Preconditions.checkNotNull(stream, "version_data.json missing, custom build?");
            this.serverVersion = JsonDataLoader.loadFromStream(stream, PhantomServerVersion.class);
        } catch (IOException exception) {
            throw ReportedException.forThrowable(exception);
        }

        PhantomServer.setTheServer(this);
    }

    protected void bootstrap() {
        if (!Eula.loadEula()) {
            LOGGER.error("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            return;
        }

        this.serverConfig = JsonServerConfig.load(((File) this.options.valueOf("config")).toPath());

        Bukkit.getPluginManager().callEvent(new ServerInitDoneEvent(this));
        ServerTicker.start();
    }

    @Override
    @NotNull
    public ListeningScheduledExecutorService getExecutor() {
        return this.executorService;
    }

    @Override
    @NotNull
    public ServerVersion getServerVersion() {
        return this.serverVersion;
    }

    @Override
    @NotNull
    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    @Override
    public void broadcast(BaseComponent[] message, @NotNull String permission) {
        PhantomServer.getInstance().filter(player -> player.hasPermission(permission)).forEach(player -> player.sendMessage(message));
    }

    @Override
    @NotNull
    public PlayerManager getPlayerManager() {
        return null;
    }

    @Override
    @NotNull
    public OfflinePlayerManager getOfflinePlayerManager() {
        return null;
    }

    @Override
    @NotNull
    public String getPrompt() {
        return ServerCliOptionUtil.getOption(this.options, "prompt", Functions.identity(), "> ").orElseThrow();
    }

    @Override
    @NotNull
    public ServerConfig getConfig() {
        return this.serverConfig;
    }

    @Override
    @NotNull
    public String getName() {
        return "server4je (" + this.serverVersion.getName() + " @ " + this.serverVersion.getId() + ")";
    }

    @Override
    @NotNull
    public String getVersion() {
        return this.serverVersion.getId();
    }

    @Override
    @NotNull
    public String getBukkitVersion() {
        return this.serverVersion.getId();
    }

    @Override
    @NotNull
    public String getMinecraftVersion() {
        return this.serverVersion.getId();
    }

    @Override
    @NotNull
    public Collection<? extends Player> getOnlinePlayers() {
        return PhantomServer.getInstance().getTracked();
    }

    @Override
    public int getMaxPlayers() {
        return this.serverConfig.getMaxPlayers();
    }

    @Override
    public void setMaxPlayers(int i) {
        this.serverConfig.setMaxPlayers(i);
    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    @NotNull
    public String getIp() {
        return null;
    }

    @Override
    @NotNull
    public String getWorldType() {
        return null;
    }

    @Override
    public boolean getGenerateStructures() {
        return false;
    }

    @Override
    public boolean getAllowEnd() {
        return false;
    }

    @Override
    public boolean getAllowNether() {
        return false;
    }

    @Override
    public boolean hasWhitelist() {
        return false;
    }

    @Override
    public void setWhitelist(boolean b) {

    }

    @Override
    @NotNull
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        return null;
    }

    @Override
    public void reloadWhitelist() {

    }

    @Override
    public int broadcastMessage(@NotNull String message) {
        return this.getOnlinePlayers().size();
    }

    @Override
    @NotNull
    public String getUpdateFolder() {
        return null;
    }

    @Override
    @NotNull
    public File getUpdateFolderFile() {
        return null;
    }

    @Override
    public long getConnectionThrottle() {
        return 0;
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerWaterSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerWaterAmbientSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerAmbientSpawns() {
        return 0;
    }

    @Override
    public Player getPlayer(@NotNull String s) {
        return null;
    }

    @Override
    public Player getPlayerExact(@NotNull String s) {
        return null;
    }

    @Override
    @NotNull
    public List<Player> matchPlayer(@NotNull String s) {
        return null;
    }

    @Override
    public Player getPlayer(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public UUID getPlayerUniqueId(@NotNull String s) {
        return null;
    }

    @Override
    @NotNull
    public PluginManager getPluginManager() {
        return this.pluginManager;
    }

    @Override
    @NotNull
    public BukkitScheduler getScheduler() {
        return this.bukkitScheduler;
    }

    @Override
    @NotNull
    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }

    @Override
    @NotNull
    public List<World> getWorlds() {
        return null;
    }

    @Override
    public World createWorld(@NotNull WorldCreator worldCreator) {
        return null;
    }

    @Override
    public boolean unloadWorld(@NotNull String s, boolean b) {
        return false;
    }

    @Override
    public boolean unloadWorld(@NotNull World world, boolean b) {
        return false;
    }

    @Override
    public World getWorld(@NotNull String s) {
        return null;
    }

    @Override
    public World getWorld(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public MapView getMap(int i) {
        return null;
    }

    @Override
    @NotNull
    public MapView createMap(@NotNull World world) {
        return null;
    }

    @Override
    @NotNull
    public ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType) {
        return null;
    }

    @Override
    @NotNull
    public ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType, int i, boolean b) {
        return null;
    }

    @Override
    public void reloadData() {

    }

    @Override
    @NotNull
    public java.util.logging.Logger getLogger() {
        return JAVA_LOGGER;
    }

    @Override
    public PluginCommand getPluginCommand(@NotNull String s) {
        return null;
    }

    @Override
    public void savePlayers() {

    }

    @Override
    public boolean dispatchCommand(@NotNull CommandSender commandSender, @NotNull String s) throws CommandException {
        return false;
    }

    @Override
    public boolean addRecipe(@Nullable Recipe recipe) {
        return false;
    }

    @Override
    @NotNull
    public List<Recipe> getRecipesFor(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public Recipe getRecipe(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    @NotNull
    public Iterator<Recipe> recipeIterator() {
        return null;
    }

    @Override
    public void clearRecipes() {

    }

    @Override
    public void resetRecipes() {

    }

    @Override
    public boolean removeRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    @NotNull
    public Map<String, String[]> getCommandAliases() {
        return null;
    }

    @Override
    public int getSpawnRadius() {
        return 0;
    }

    @Override
    public void setSpawnRadius(int i) {

    }

    @Override
    public boolean getOnlineMode() {
        return false;
    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int broadcast(@NotNull String s, @NotNull String s1) {
        return 0;
    }

    @Override
    @NotNull
    public OfflinePlayer getOfflinePlayer(@NotNull String s) {
        return null;
    }

    @Override
    @NotNull
    public OfflinePlayer getOfflinePlayer(@NotNull UUID uuid) {
        return null;
    }

    @Override
    @NotNull
    public Set<String> getIPBans() {
        return null;
    }

    @Override
    public void banIP(@NotNull String s) {

    }

    @Override
    public void unbanIP(@NotNull String s) {

    }

    @Override
    @NotNull
    public Set<OfflinePlayer> getBannedPlayers() {
        return null;
    }

    @Override
    @NotNull
    public BanList getBanList(@NotNull BanList.Type type) {
        return null;
    }

    @Override
    @NotNull
    public Set<OfflinePlayer> getOperators() {
        return null;
    }

    @Override
    @NotNull
    public GameMode getDefaultGameMode() {
        return null;
    }

    @Override
    public void setDefaultGameMode(@NotNull GameMode gameMode) {

    }

    @Override
    @NotNull
    public ConsoleCommandSender getConsoleSender() {
        return null;
    }

    @Override
    @NotNull
    public File getWorldContainer() {
        return null;
    }

    @Override
    @NotNull
    public OfflinePlayer[] getOfflinePlayers() {
        return new OfflinePlayer[0];
    }

    @Override
    @NotNull
    public Messenger getMessenger() {
        return null;
    }

    @Override
    @NotNull
    public HelpMap getHelpMap() {
        return null;
    }

    @Override
    @NotNull
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType) {
        return null;
    }

    @Override
    @NotNull
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType, @NotNull String s) {
        return null;
    }

    @Override
    @NotNull
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i) throws IllegalArgumentException {
        return null;
    }

    @Override
    @NotNull
    public Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i, @NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    @NotNull
    public Merchant createMerchant(@Nullable String s) {
        return null;
    }

    @Override
    public int getMonsterSpawnLimit() {
        return 0;
    }

    @Override
    public int getAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public int getAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public final boolean isPrimaryThread() {
        return this.isPrimaryThread(Thread.currentThread());
    }

    @Override
    public final boolean isPrimaryThread(@Nullable Thread thread) {
        return thread != null && this.primaryThread == thread;
    }

    @Override
    public final void ensureMainThread() {
        Preconditions.checkArgument(this.isPrimaryThread(), "Not on the primary thread");
    }

    @Override
    @NotNull
    public String getMotd() {
        return this.serverConfig.getMessages().getMessageOfTheDay();
    }

    @Override
    public String getShutdownMessage() {
        return this.serverConfig.getMessages().getShutdownMessage();
    }

    @Override
    @NotNull
    public Warning.WarningState getWarningState() {
        return null;
    }

    @Override
    @NotNull
    public ItemFactory getItemFactory() {
        return null;
    }

    @Override
    @NotNull
    public ScoreboardManager getScoreboardManager() {
        return null;
    }

    @Override
    public CachedServerIcon getServerIcon() {
        return null;
    }

    @Override
    @NotNull
    public CachedServerIcon loadServerIcon(@NotNull File file) throws IllegalArgumentException, Exception {
        return null;
    }

    @Override
    @NotNull
    public CachedServerIcon loadServerIcon(@NotNull BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
        return null;
    }

    @Override
    public void setIdleTimeout(int i) {

    }

    @Override
    public int getIdleTimeout() {
        return 0;
    }

    @Override
    @NotNull
    public ChunkGenerator.ChunkData createChunkData(@NotNull World world) {
        return null;
    }

    @Override
    @NotNull
    public ChunkGenerator.ChunkData createVanillaChunkData(@NotNull World world, int i, int i1) {
        return null;
    }

    @Override
    @NotNull
    public BossBar createBossBar(@Nullable String s, @NotNull BarColor barColor, @NotNull BarStyle barStyle, BarFlag... barFlags) {
        return null;
    }

    @Override
    @NotNull
    public KeyedBossBar createBossBar(@NotNull NamespacedKey namespacedKey, @Nullable String s, @NotNull BarColor barColor, @NotNull BarStyle barStyle, BarFlag... barFlags) {
        return null;
    }

    @Override
    @NotNull
    public Iterator<KeyedBossBar> getBossBars() {
        return null;
    }

    @Override
    public KeyedBossBar getBossBar(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public boolean removeBossBar(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public Entity getEntity(@NotNull UUID uuid) {
        return null;
    }

    @Override
    @NotNull
    public double[] getTPS() {
        return new double[0];
    }

    @Override
    @NotNull
    public long[] getTickTimes() {
        return new long[0];
    }

    @Override
    public double getAverageTickTime() {
        return 0;
    }

    @Override
    @NotNull
    public CommandMap getCommandMap() {
        return this.commandMap;
    }

    @Override
    public Advancement getAdvancement(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    @NotNull
    public Iterator<Advancement> advancementIterator() {
        return null;
    }

    @Override
    @NotNull
    public BlockData createBlockData(@NotNull Material material) {
        return null;
    }

    @Override
    @NotNull
    public BlockData createBlockData(@NotNull Material material, @Nullable Consumer<BlockData> consumer) {
        return null;
    }

    @Override
    @NotNull
    public BlockData createBlockData(@NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    @NotNull
    public BlockData createBlockData(@Nullable Material material, @Nullable String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    @NotNull
    public <T extends Keyed> Tag<T> getTag(@NotNull String s, @NotNull NamespacedKey namespacedKey, @NotNull Class<T> aClass) {
        return null;
    }

    @Override
    @NotNull
    public <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String s, @NotNull Class<T> aClass) {
        return null;
    }

    @Override
    public LootTable getLootTable(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    @NotNull
    public List<Entity> selectEntities(@NotNull CommandSender commandSender, @NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    @NotNull
    @Deprecated
    public UnsafeValues getUnsafe() {
        return ServerUnsafeValues.INSTANCE;
    }

    @Override
    @NotNull
    public Spigot spigot() {
        return this.spigot;
    }

    @Override
    public void reloadPermissions() {

    }

    @Override
    public boolean reloadCommandAliases() {
        return false;
    }

    @Override
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        return false;
    }

    @Override
    @NotNull
    public String getPermissionMessage() {
        return this.serverConfig.getMessages().getNoPermissionMessage();
    }

    @Override
    @NotNull
    public PhantomPlayerProfile createProfile(@NotNull UUID uuid) {
        return new GameProfile(uuid, null);
    }

    @Override
    @NotNull
    public PhantomPlayerProfile createProfile(@NotNull String s) {
        return new GameProfile(null, s);
    }

    @Override
    @NotNull
    public PhantomPlayerProfile createProfile(@Nullable UUID uuid, @Nullable String s) {
        return new GameProfile(uuid, s);
    }

    @Override
    public int getCurrentTick() {
        return Math.toIntExact(ServerTicker.getCurrentTick());
    }

    @Override
    public boolean isStopping() {
        return !this.running.get();
    }

    @Override
    @NotNull
    public MobGoals getMobGoals() {
        return null;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin plugin, @NotNull String s, byte[] bytes) {

    }

    @Override
    @NotNull
    public Set<String> getListeningPluginChannels() {
        return null;
    }

    @Override
    @NotNull
    public Audience<Player> filter(@NotNull Predicate<Player> filter) {
        return null;
    }

    @Override
    @NotNull
    public Audience<Player> track(@NotNull Player toTracked) {
        return null;
    }

    @Override
    @NotNull
    public Audience<Player> untrack(@NotNull Player toUntracked) {
        return null;
    }

    @Override
    @NotNull
    public Collection<Player> getTracked() {
        return null;
    }

    @Override
    public void forEach(@NotNull Consumer<Player> consumer) {

    }

    private static final class ServerSpigot extends Spigot {

        @Override
        public void broadcast(@NotNull BaseComponent component) {
            PhantomServer.getInstance().forEach(player -> player.sendMessage(component));
        }

        @Override
        public void broadcast(@NotNull BaseComponent... components) {
            PhantomServer.getInstance().forEach(player -> player.sendMessage(components));
        }

        @Override
        public void restart() {
            // Silently discard
        }
    }
}

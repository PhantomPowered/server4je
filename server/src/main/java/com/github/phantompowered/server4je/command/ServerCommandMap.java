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
package com.github.phantompowered.server4je.command;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerCommandException;
import com.github.phantompowered.server4je.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerCommandMap implements CommandMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommandMap.class);
    private static final char DEFAULT_SEPARATOR = ':';

    private final Collection<CommandContainer> commands = new CopyOnWriteArrayList<>();
    private final Map<String, Command> legacyCommandLookup = new ConcurrentHashMap<>(); // used for paper api only

    @Override
    public void registerAll(@NotNull String s, @NotNull List<Command> list) {
        for (Command command : list) {
            this.register(s, command);
        }
    }

    @Override
    public boolean register(@NotNull String s, @NotNull String s1, @NotNull Command command) {
        if (this.getCommand(s) != null) {
            return false;
        }

        this.commands.add(new CommandContainer(command, s, s1));

        // legacy lookup
        this.legacyCommandLookup.put(s + DEFAULT_SEPARATOR + s1, command);
        this.legacyCommandLookup.put(s1, command);
        for (String alias : command.getAliases()) {
            this.legacyCommandLookup.put(alias, command);
        }

        return true;
    }

    @Override
    public boolean register(@NotNull String s, @NotNull Command command) {
        return this.register(command.getName(), s, command);
    }

    @Override
    public boolean dispatch(@NotNull CommandSender commandSender, @NotNull String s) throws CommandException {
        final String commandName;
        final String[] args;
        if (s.indexOf(' ') == -1) {
            // no need for a split
            commandName = s;
            args = new String[0]; // some plugins do weired stuff... so stay safe
        } else {
            String[] split = s.split(" ");
            commandName = split[0];
            args = Arrays.copyOfRange(split, 1, split.length);
        }

        final Command command = this.getCommand(commandName);
        if (command == null || (commandSender != Bukkit.getConsoleSender() && !command.testPermissionSilent(commandSender))) {
            return false;
        }

        try {
            command.execute(commandSender, commandName, args);
        } catch (Throwable throwable) {
            if (throwable instanceof CommandException) {
                Bukkit.getPluginManager().callEvent(new ServerExceptionEvent(new ServerCommandException(throwable, command, commandSender, args)));
            }

            LOGGER.error("Fatal exception processing command", throwable);
            return false;
        }

        return true;
    }

    @Override
    public void clearCommands() {
        this.commands.clear();
    }

    @Override
    @Nullable
    public Command getCommand(@NotNull String s) {
        return Iterables.first(this.commands, container -> {
            String command = s; // hack
            if (command.startsWith(container.getFallbackPrefix() + DEFAULT_SEPARATOR)) {
                command = command.replaceFirst(container.getFallbackPrefix() + DEFAULT_SEPARATOR, "");
            }

            if (container.getCommand().getName().equalsIgnoreCase(command)) {
                return true;
            }

            for (String alias : container.getCommand().getAliases()) {
                if (alias.equalsIgnoreCase(command)) {
                    return true;
                }
            }

            return false;
        }).map(CommandContainer::getCommand).orElse(null);
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String s) throws IllegalArgumentException {
        return this.tabComplete(commandSender, s, null);
    }

    @Override
    @Nullable
    public List<String> tabComplete(@NotNull CommandSender commandSender, @NotNull String s, @Nullable Location location) throws IllegalArgumentException {
        final List<String> result = new ArrayList<>();
        final String prefix = commandSender instanceof Player ? "/" : "";

        if (s.indexOf(' ') == -1) {
            for (CommandContainer command : this.commands) {
                if (Bukkit.getConsoleSender() != commandSender && !command.getCommand().testPermissionSilent(commandSender)) {
                    continue;
                }

                String name = command.getFallbackPrefix() + DEFAULT_SEPARATOR + command.getLabel();
                if (StringUtil.startsWithIgnoreCase(name, s)) {
                    result.add(prefix + name);
                }
            }
        } else {
            final String[] split = s.split(" ");
            final Command command = this.getCommand(split[0]);
            if (command == null || (Bukkit.getConsoleSender() != commandSender && !command.testPermissionSilent(commandSender))) {
                return null;
            }

            final String[] args = Arrays.copyOfRange(split, 1, split.length);
            try {
                result.addAll(command.tabComplete(commandSender, s, args, location));
            } catch (Throwable throwable) {
                if (throwable instanceof CommandException) {
                    Bukkit.getPluginManager().callEvent(new ServerExceptionEvent(new ServerCommandException(throwable, command, commandSender, args)));
                }

                LOGGER.error("Fatal exception processing tab complete", throwable);
                return null;
            }
        }

        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    @Override
    @NotNull
    public Map<String, Command> getKnownCommands() {
        return this.legacyCommandLookup;
    }
}

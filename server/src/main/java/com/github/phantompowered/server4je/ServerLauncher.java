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

import com.github.phantompowered.server4je.api.PhantomServer;
import com.github.phantompowered.server4je.common.misc.KeyValueHolder;
import com.github.phantompowered.server4je.options.ServerCliOptionUtil;
import io.netty.util.ResourceLeakDetector;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecrell.terminalconsole.TerminalConsoleAppender;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public final class ServerLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);

    public static synchronized void main(String[] args) {
        initConsole();

        System.setSecurityManager(null);

        setSystemPropertyIfUnset("io.netty.allocator.maxOrder", "9");
        setSystemPropertyIfUnset("io.netty.noPreferDirect", "true");
        setSystemPropertyIfUnset("io.netty.maxDirectMemory", "0");
        setSystemPropertyIfUnset("io.netty.recycler.maxCapacity", "0");
        setSystemPropertyIfUnset("io.netty.recycler.maxCapacity.default", "0");
        setSystemPropertyIfUnset("io.netty.selectorAutoRebuildThreshold", "0");
        setSystemPropertyIfUnset("io.netty.allocator.type", "UNPOOLED");
        setSystemPropertyIfUnset("io.netty.tryReflectionSetAccessible", "true");

        if (System.getProperty("io.netty.leakDetectionLevel") == null) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }

        KeyValueHolder<OptionSet, OptionParser> options = ServerCliOptionUtil.parseOptions(args);
        if (options.getKey().has("?")) {
            try {
                options.getValue().printHelpOn(System.out);
            } catch (IOException exception) {
                LOGGER.error("Exception printing help to System.out", exception);
            }

            return;
        }

        if (options.getKey().has("v")) {
            LOGGER.info(ServerLauncher.class.getPackage().getImplementationVersion());
            return;
        }

        String path = Paths.get("").toAbsolutePath().toString();
        if (path.contains("!") || path.contains("+")) {
            LOGGER.error("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
            return;
        }

        System.setProperty("library.jansi.version", "server4je");

        Server4JavaEdition server = new Server4JavaEdition(options.getKey());
        postInitConsole(); // Bukkit setup done!
        setupShutdownHook(server);

        server.bootstrap(); // bootstrap now
    }

    private static void initConsole() {
        Terminal terminal = TerminalConsoleAppender.getTerminal();
        if (terminal != null) {
            LineReader lineReader = LineReaderBuilder.builder()
                .appName("server4je")
                .terminal(terminal)
                .completer((reader, line, candidates) -> {
                    List<String> suggestions = Bukkit.getCommandMap().tabComplete(Bukkit.getConsoleSender(), line.line());
                    if (suggestions != null) {
                        candidates.addAll(suggestions.stream().map(Candidate::new).collect(Collectors.toList()));
                    }
                })
                .build();
            lineReader.setKeyMap("emacs");
            TerminalConsoleAppender.setReader(lineReader);
        }
    }

    private static void postInitConsole() {
        LineReader lineReader = TerminalConsoleAppender.getReader();
        if (lineReader != null) {
            Thread readingThread = new Thread(() -> {
                String line;
                while (!Bukkit.isStopping()) {
                    try {
                        line = lineReader.readLine(PhantomServer.getInstance().getPrompt());
                        if (line != null && !line.trim().isEmpty()) {
                            Bukkit.getCommandMap().dispatch(Bukkit.getConsoleSender(), line);
                        }
                    } catch (UserInterruptException exception) {
                        LOGGER.warn("Please use \"stop\" instead of Control+C");
                        Bukkit.shutdown();
                    } catch (Exception exception) {
                        LOGGER.error("Unable to read line", exception);
                    }
                }
            }, "Server4JE cli reading thread");
            readingThread.setDaemon(true);
            readingThread.start();
        }
    }

    private static void setupShutdownHook(@NotNull Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }

    private static void setSystemPropertyIfUnset(@NotNull String key, @NotNull String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}

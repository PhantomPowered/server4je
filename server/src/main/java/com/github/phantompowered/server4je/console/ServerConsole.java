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
package com.github.phantompowered.server4je.console;

import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.bukkit.Bukkit;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ServerConsole extends SimpleTerminalConsole {

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder.appName("server4je").completer((reader, line, candidates) -> {
            List<String> suggestions = Bukkit.getCommandMap().tabComplete(Bukkit.getConsoleSender(), line.line());
            if (suggestions != null) {
                candidates.addAll(suggestions.stream().map(Candidate::new).collect(Collectors.toList()));
            }
        }));
    }

    @Override
    protected boolean isRunning() {
        return !Bukkit.isStopping();
    }

    @Override
    protected void runCommand(String command) {
        Bukkit.getCommandMap().dispatch(Bukkit.getConsoleSender(), command);
    }

    @Override
    protected void shutdown() {
        Bukkit.shutdown();
    }
}

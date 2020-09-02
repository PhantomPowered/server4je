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

import com.github.phantompowered.server4je.logging.ConsoleColour;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

import java.util.concurrent.CompletableFuture;

/* package */ class ConsoleReadThread extends Thread {

    protected ConsoleReadThread(@NotNull DefaultServerConsole console) {
        super("ReformCloud console read thread");
        this.console = console;
    }

    private final DefaultServerConsole console;
    private CompletableFuture<String> currentTask;

    @Override
    public void run() {
        String line;
        while (!super.isInterrupted() && (line = this.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }

            if (this.currentTask != null) {
                this.currentTask.complete(line);
                this.currentTask = null;
                continue;
            }

            this.dispatchLine(line);
        }
    }

    @Nullable
    private String readLine() {
        try {
            return this.console.getLineReader().readLine(this.console.getPrompt());
        } catch (EndOfFileException ignored) {
        } catch (UserInterruptException exception) {
            System.exit(-1);
        }

        return null;
    }

    private void dispatchLine(@NotNull String line) {
        if (!Bukkit.getCommandMap().dispatch(Bukkit.getConsoleSender(), line)) {
            Bukkit.getConsoleSender().sendMessage(ConsoleColour.RED + "Unknown command. See \"help\" for a list of all commands.");
        }
    }

    @NotNull
    protected CompletableFuture<String> getCurrentTask() {
        if (this.currentTask == null) {
            return this.currentTask = new CompletableFuture<>();
        }

        return this.currentTask;
    }
}

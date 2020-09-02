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

import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.logging.ConsoleColour;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;

public class DefaultServerConsole {

    private static final String USER = System.getProperty("user.name");
    private static final String VERSION = DefaultServerConsole.class.getPackage().getImplementationVersion();

    private final ConsoleReadThread consoleReadThread = new ConsoleReadThread(this);
    private final Terminal terminal;
    private final LineReader lineReader;

    private String prompt = System.getProperty("server.console-prompt-pattern", "[&cserver&7-&b{0}&7@&b{1} &f~&r]$ ");

    public DefaultServerConsole() {
        System.setProperty("library.jansi.version", "Server4JE");
        AnsiConsole.systemInstall();

        this.prompt = ConsoleColour.toColouredString('&', this.prompt);
        this.prompt = MessageFormat.format(this.prompt, USER, VERSION);

        try {
            this.terminal = TerminalBuilder.builder().system(true).encoding(StandardCharsets.UTF_8).build();
            this.lineReader = LineReaderBuilder.builder()
                .terminal(this.terminal)
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .build();
        } catch (IOException exception) {
            throw ReportedException.forMessageAndThrowable(exception, "Unable to create console");
        }

        this.consoleReadThread.start();
    }

    @NotNull
    public CompletableFuture<String> readString() {
        return this.consoleReadThread.getCurrentTask();
    }

    @NotNull
    public String getPrompt() {
        return this.prompt;
    }

    public void setPrompt(@NotNull String prompt) {
        this.prompt = ConsoleColour.toColouredString('&', prompt);
    }

    public void clearScreen() {
        this.terminal.puts(InfoCmp.Capability.clear_screen);
        this.terminal.flush();
    }

    public void close() throws Exception {
        this.consoleReadThread.interrupt();
        this.terminal.flush();
        this.terminal.close();

        AnsiConsole.systemUninstall();
    }

    @NotNull
    @ApiStatus.Internal
    public LineReader getLineReader() {
        return this.lineReader;
    }
}

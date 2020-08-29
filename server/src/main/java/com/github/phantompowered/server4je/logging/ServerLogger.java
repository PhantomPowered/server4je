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
package com.github.phantompowered.server4je.logging;

import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.common.io.IOUtil;
import com.github.phantompowered.server4je.sentry.SentryPublisher;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ServerLogger extends Logger {

    private final RecordDispatcher recordDispatcher = new RecordDispatcher(this);

    public ServerLogger(@NotNull LineReader lineReader) {
        super("Server4JavaEditionLogger", null);
        super.setLevel(Level.ALL);

        try {
            var logFilePath = Path.of(System.getProperty("server.logFile", "logs/server.log"));
            if (logFilePath.getParent() != null) {
                IOUtil.createDirectories(logFilePath.getParent());
            }

            var fileWriter = new FileHandler(logFilePath.toString(), 1 << 24, 8, true);
            fileWriter.setFormatter(new DefaultFormatter(false));
            fileWriter.setEncoding(StandardCharsets.UTF_8.name());
            super.addHandler(fileWriter);

            var consoleWriter = new ColouredWriter(lineReader);
            consoleWriter.setLevel(Level.parse(System.getProperty("server.console-log-level", "ALL")));
            consoleWriter.setFormatter(new DefaultFormatter(true));
            consoleWriter.setEncoding(StandardCharsets.UTF_8.name());
            super.addHandler(consoleWriter);
        } catch (IOException exception) {
            if (SentryPublisher.ENABLED) {
                SentryPublisher.empty().throwable(exception).send("Exception during logger init");
            }

            ReportedException.throwWrapped(exception, "Unable to initialize logger");
        }

        System.setOut(new PrintStream(new LoggingOutputStream(this, Level.INFO), true));
        System.setErr(new PrintStream(new LoggingOutputStream(this, Level.SEVERE), true));

        this.recordDispatcher.start();
    }

    @Override
    public void log(LogRecord record) {
        this.recordDispatcher.enqueueRecord(record);
    }

    protected void writeRecord(@NotNull LogRecord logRecord) {
        Preconditions.checkNotNull(logRecord);
        super.log(logRecord);
    }
}

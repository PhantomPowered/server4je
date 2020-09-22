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

import com.github.phantompowered.server4je.ServerLauncher;
import com.github.phantompowered.server4je.security.CallerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class LoggingOutputStream extends ByteArrayOutputStream {

    public static void init() {
        CallerUtils.getCallerClass().ifPresent(clazz -> {
            if (clazz.equals(ServerLauncher.class)) {
                System.setOut(new PrintStream(new LoggingOutputStream(Level.INFO), true));
                System.setErr(new PrintStream(new LoggingOutputStream(Level.SEVERE), true));
            } else {
                throw new IllegalStateException("Caller is not " + ServerLauncher.class);
            }
        });
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingOutputStream.class);
    private final Level level;

    private LoggingOutputStream(Level level) {
        this.level = level;
    }

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            super.flush();
            String content = super.toString(StandardCharsets.UTF_8);
            super.reset();

            if (!content.isBlank() && !content.equals(System.lineSeparator())) {
                LOGGER.error("Logging shouldn't be done using System.out or System.err! Create your own fucking logger!");

                if (this.level == Level.INFO) {
                    LOGGER.info(content);
                } else if (this.level == Level.SEVERE) {
                    LOGGER.error(content);
                } else {
                    LOGGER.warn(content);
                }
            }
        }
    }
}

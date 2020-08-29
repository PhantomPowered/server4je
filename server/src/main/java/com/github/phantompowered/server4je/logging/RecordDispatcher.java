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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogRecord;

/* package */ final class RecordDispatcher extends Thread {

    private final ServerLogger serverLogger;
    private final BlockingQueue<LogRecord> recordQueue = new LinkedBlockingQueue<>();

    public RecordDispatcher(ServerLogger serverLogger) {
        this.serverLogger = serverLogger;
    }

    public void enqueueRecord(@NotNull LogRecord logRecord) {
        Preconditions.checkNotNull(logRecord);
        this.recordQueue.add(logRecord);
    }

    @Override
    public void run() {
        while (!super.isInterrupted()) {
            try {
                this.serverLogger.writeRecord(this.recordQueue.take());
            } catch (InterruptedException exception) {
                break;
            }
        }

        for (LogRecord logRecord : this.recordQueue) {
            this.serverLogger.writeRecord(logRecord);
        }
    }
}

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
package com.github.phantompowered.server4je.sentry;

import com.google.common.base.Preconditions;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;

import java.net.InetAddress;

public class SentryPublisher {

    public static final boolean ENABLED = !Boolean.getBoolean("sentry.publisher.disabled");

    private static final String DSN = "https://82f0effc75394b4f93906b6ef717663e@o440889.ingest.sentry.io/5410593?async=true";
    private static final SystemInfo INFO = new SystemInfo();
    private static final String HOST;

    static {
        String host;
        try {
            host = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Throwable t) {
            host = "unknown host";
        }

        HOST = host;
    }

    private final SentryClient sentryClient;
    private final Context context;

    private @Nullable Throwable throwable;

    private SentryPublisher() {
        this.sentryClient = SentryClientFactory.sentryClient(SentryPublisher.DSN);
        // TODO this.sentryClient.setRelease();
        this.sentryClient.setServerName(SentryPublisher.HOST);

        this.context = this.sentryClient.getContext();
        this.context.addTag("java_version", System.getProperty("java.vm.name") + " (" + System.getProperty("java.runtime.version") + ")");

        this.context.addExtra("system.process_memory_free", formatBytes(Runtime.getRuntime().freeMemory()));
        this.context.addExtra("system.process_memory_total", formatBytes(Runtime.getRuntime().totalMemory()));

        this.context.addExtra("system.os", INFO.getOperatingSystem().getFamily() + " (" + INFO.getOperatingSystem().getVersionInfo().getVersion() + ")");
        this.context.addExtra("system.cpu", INFO.getHardware().getProcessor().getProcessorIdentifier().getName());
        this.context.addExtra("system.memory", formatBytes(INFO.getHardware().getMemory().getTotal()));

        this.context.addExtra("system.current_thread", Thread.currentThread().getName());
    }

    @NotNull
    public SentryPublisher extra(@NotNull String key, @NotNull String value) {
        this.context.addExtra(Preconditions.checkNotNull(key), Preconditions.checkNotNull(value));
        return this;
    }

    @NotNull
    public SentryPublisher throwable(@NotNull Throwable throwable) {
        this.throwable = Preconditions.checkNotNull(throwable);
        return this;
    }

    public void send() {
        this.send(null);
    }

    public void send(@Nullable String message) {
        if (this.throwable != null) {
            this.sentryClient.sendException(this.throwable);
            return;
        }

        this.sentryClient.sendMessage(message);
    }

    @NotNull
    // Thanks stackoverflow
    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %siB", bytes / Math.pow(1024, exp), pre);
    }

    @NotNull
    public static SentryPublisher empty() {
        return new SentryPublisher();
    }
}

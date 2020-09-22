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
package com.github.phantompowered.server4je.tick;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.github.phantompowered.server4je.scheduler.ServerScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public final class ServerTicker {

    private ServerTicker() {
        throw new UnsupportedOperationException();
    }

    // ===

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTicker.class);

    private static final AtomicLong CURRENT_TICK = new AtomicLong(-1);
    private static final Queue<FutureCallableWrapper<?>> TASK_QUEUE = new ArrayDeque<>();
    private static final int TPS = 20;
    private static final long TICKS_PER_SECOND = 1000 / TPS;

    public static ServerScheduler SERVER_SCHEDULER;

    public static void start() {
        long currentTickTime = System.currentTimeMillis();

        while (!Bukkit.isStopping()) {
            try {
                long currentTickDiff = System.currentTimeMillis() - currentTickTime;
                currentTickTime = System.currentTimeMillis();
                if (currentTickDiff < TICKS_PER_SECOND) {
                    try {
                        Thread.sleep(TICKS_PER_SECOND - currentTickDiff);
                    } catch (InterruptedException exception) {
                        LOGGER.error("Error oversleeping sleep time {}", TICKS_PER_SECOND - currentTickDiff);
                    }
                }

                long currentTick = CURRENT_TICK.incrementAndGet();
                Bukkit.getPluginManager().callEvent(new ServerTickStartEvent(Math.toIntExact(currentTick)));

                while (!TASK_QUEUE.isEmpty()) {
                    FutureCallableWrapper<?> wrapper = TASK_QUEUE.poll();
                    if (wrapper != null && wrapper.plugin.isEnabled()) {
                        wrapper.call();
                    }
                }

                if (currentTick % TPS == 0) {
                    SERVER_SCHEDULER.fullHeartbeat(currentTick);
                } else {
                    SERVER_SCHEDULER.heartbeat(currentTick);
                }

                Bukkit.getPluginManager().callEvent(new ServerTickEndEvent(
                    CURRENT_TICK.intValue(),
                    System.currentTimeMillis() - currentTickTime,
                    TICKS_PER_SECOND - (System.currentTimeMillis() - currentTickTime)
                ));
            } catch (Throwable throwable) {
                LOGGER.error("Error executing server tick", throwable);
            }
        }
    }

    public static long getCurrentTick() {
        return CURRENT_TICK.get();
    }

    @NotNull
    public static <T> Future<T> call(@NotNull Callable<T> callable, @NotNull Plugin plugin) {
        FutureCallableWrapper<T> wrapper = new FutureCallableWrapper<>(plugin, callable);
        TASK_QUEUE.add(wrapper);
        return wrapper.future;
    }

    private static final class FutureCallableWrapper<T> {

        private final CompletableFuture<T> future = new CompletableFuture<>();
        private final Plugin plugin;
        private final Callable<T> callable;

        public FutureCallableWrapper(Plugin plugin, Callable<T> callable) {
            this.plugin = plugin;
            this.callable = callable;
        }

        private void call() throws Exception {
            this.future.complete(this.callable.call());
        }
    }
}

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
package com.github.phantompowered.server4je.scheduler;

import com.github.phantompowered.server4je.tick.ServerTicker;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerTask implements BukkitTask, Runnable {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ServerTask.class);

    protected final int taskId;
    protected final Plugin owner;
    protected final Runnable execute;
    protected final long delayBetweenRuns;
    protected final boolean sync;
    protected long nextRunningTick;
    protected boolean running;

    protected ServerTask(int taskId, Plugin owner, Runnable execute, long delayBetweenRuns, long nextRunningTick, boolean sync) {
        this.taskId = taskId;
        this.owner = owner;
        this.execute = execute;
        this.delayBetweenRuns = delayBetweenRuns;
        this.nextRunningTick = nextRunningTick;
        this.sync = sync;
        this.running = true;
    }

    protected ServerTask(int taskId, Plugin owner, Runnable execute, boolean sync, long nextRunningTick) {
        this.taskId = taskId;
        this.owner = owner;
        this.execute = execute;
        this.delayBetweenRuns = -1L;
        this.sync = sync;
        this.nextRunningTick = nextRunningTick;
    }

    @Override
    public int getTaskId() {
        return this.taskId;
    }

    @Override
    public @NotNull
    Plugin getOwner() {
        return this.owner;
    }

    @Override
    public boolean isSync() {
        return this.sync;
    }

    @Override
    public boolean isCancelled() {
        return !this.running;
    }

    @Override
    public void cancel() {
        synchronized (this) {
            if (this.isCancelled()) {
                return;
            }

            this.running = false;
            this.nextRunningTick = -1L;
        }
    }

    public long getNextRunningTick() {
        return this.nextRunningTick;
    }

    protected void execute(long currentTick) {
        synchronized (this) {
            Preconditions.checkArgument(!this.sync || Bukkit.isPrimaryThread(), "Called sync task from async context");

            if (this.running && (!this.sync || (this.nextRunningTick != -1 && this.nextRunningTick == currentTick))) {
                if (this.delayBetweenRuns > 0) {
                    this.nextRunningTick = currentTick + this.delayBetweenRuns;
                } else {
                    this.nextRunningTick = -1;
                    this.running = false;
                }

                this.run0();
            }
        }
    }

    @Override
    public final void run() {
        this.execute(ServerTicker.getCurrentTick());
    }

    protected void run0() {
        try {
            this.execute.run();
        } catch (Throwable throwable) {
            LOGGER.error("Unable to handle tick of task " + this.execute.getClass().getName() + " for plugin "
                + (this.owner == null ? "server" : this.owner.getName()) + ". Stopping to prevent further issues.", throwable);
            this.nextRunningTick = -1;
            this.running = false;
        }
    }
}

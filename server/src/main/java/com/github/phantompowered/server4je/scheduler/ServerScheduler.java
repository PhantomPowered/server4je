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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ServerScheduler implements BukkitScheduler {

    private final AtomicInteger taskIds = new AtomicInteger();
    private final ScheduledExecutorService asyncWorker = Executors.newScheduledThreadPool(4);
    private final ObjectList<ServerTask> queuedTasks = ObjectLists.synchronize(new ObjectArrayList<>());

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        return this.runTaskLater(plugin, task, delay).getTaskId();
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay) {
        return this.scheduleSyncDelayedTask(plugin, (Runnable) task, delay);
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        return this.scheduleSyncDelayedTask(plugin, task, 1);
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task) {
        return this.scheduleSyncDelayedTask(plugin, task, 1);
    }

    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        return this.runTaskTimer(plugin, task, delay, period).getTaskId();
    }

    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay, long period) {
        return this.scheduleSyncRepeatingTask(plugin, (Runnable) task, delay, period);
    }

    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        return this.runTaskLaterAsynchronously(plugin, task, delay).getTaskId();
    }

    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        return this.scheduleAsyncDelayedTask(plugin, task, 1);
    }

    @Override
    public int scheduleAsyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        return this.runTaskTimerAsynchronously(plugin, task, delay, period).getTaskId();
    }

    @Override
    @NotNull
    public <T> Future<T> callSyncMethod(@NotNull Plugin plugin, @NotNull Callable<T> task) {
        return ServerTicker.call(task, plugin);
    }

    @Override
    public void cancelTask(int taskId) {
        for (ServerTask queuedTask : this.queuedTasks) {
            if (queuedTask.getTaskId() == taskId) {
                queuedTask.cancel();
                this.queuedTasks.remove(queuedTask);
                break;
            }
        }
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        for (ServerTask queuedTask : this.queuedTasks) {
            if (queuedTask.getOwner().equals(plugin)) {
                queuedTask.cancel();
            }
        }

        this.cleanupTasks();
    }

    @Override
    public boolean isCurrentlyRunning(int taskId) {
        for (ServerTask queuedTask : this.queuedTasks) {
            if (queuedTask.getTaskId() == taskId) {
                return !queuedTask.isCancelled() && queuedTask.getNextRunningTick() == ServerTicker.getCurrentTick();
            }
        }

        return false;
    }

    @Override
    public boolean isQueued(int taskId) {
        for (ServerTask queuedTask : this.queuedTasks) {
            if (queuedTask.getTaskId() == taskId) {
                return !queuedTask.isCancelled();
            }
        }

        return false;
    }

    @Override
    @NotNull
    public List<BukkitWorker> getActiveWorkers() {
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public List<BukkitTask> getPendingTasks() {
        return Collections.unmodifiableList(this.queuedTasks);
    }

    @Override
    @NotNull
    public BukkitTask runTask(@NotNull Plugin plugin, @NotNull Runnable task) throws IllegalArgumentException {
        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, true, this.nextTick());
        this.queuedTasks.add(serverTask);
        return serverTask;
    }

    @Override
    public void runTask(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task) throws IllegalArgumentException {
        ServerTask bukkitTask = new ConsumerServerTask(this.taskIds.incrementAndGet(), plugin, true, this.nextTick(), task);
        this.queuedTasks.add(bukkitTask);
        task.accept(bukkitTask);
    }

    @Override
    @NotNull
    public BukkitTask runTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task) throws IllegalArgumentException {
        return this.runTask(plugin, (Runnable) task);
    }

    @Override
    @NotNull
    public BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task) throws IllegalArgumentException {
        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, false, this.nextTick());
        this.queuedTasks.add(serverTask);
        this.asyncWorker.execute(serverTask);
        return serverTask;
    }

    @Override
    public void runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task) throws IllegalArgumentException {
        ServerTask serverTask = new ConsumerServerTask(this.taskIds.incrementAndGet(), plugin, false, this.nextTick(), task);
        this.queuedTasks.add(serverTask);
        this.asyncWorker.execute(serverTask);
    }

    @Override
    @NotNull
    public BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable task) throws IllegalArgumentException {
        return this.runTaskAsynchronously(plugin, (Runnable) task);
    }

    @Override
    @NotNull
    public BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable task, long delay) throws IllegalArgumentException {
        Preconditions.checkArgument(delay > 0, "delay smaller than 1");
        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, true, this.targetTick(delay));
        this.queuedTasks.add(serverTask);
        return serverTask;
    }

    @Override
    public void runTaskLater(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay) throws IllegalArgumentException {
        Preconditions.checkArgument(delay > 0, "delay smaller than 1");
        ServerTask serverTask = new ConsumerServerTask(this.taskIds.incrementAndGet(), plugin, true, this.targetTick(delay), task);
        this.queuedTasks.add(serverTask);
    }

    @Override
    @NotNull
    public BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay) throws IllegalArgumentException {
        Preconditions.checkArgument(delay > 0, "delay smaller than 1");
        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, true, this.targetTick(delay));
        this.queuedTasks.add(serverTask);
        return serverTask;
    }

    @Override
    @NotNull
    public BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task, long delay) throws IllegalArgumentException {
        Preconditions.checkArgument(delay > 0, "delay smaller than 1");
        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, false, this.targetTick(delay));
        this.queuedTasks.add(serverTask);
        this.asyncWorker.schedule(serverTask, delay * (1000 / 20), TimeUnit.MILLISECONDS);
        return serverTask;
    }

    @Override
    public void runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay) throws IllegalArgumentException {
        Preconditions.checkArgument(delay > 0, "delay smaller than 1");
        ServerTask serverTask = new ConsumerServerTask(this.taskIds.incrementAndGet(), plugin, false, this.targetTick(delay), task);
        this.queuedTasks.add(serverTask);
        this.asyncWorker.schedule(serverTask, delay * (1000 / 20), TimeUnit.MILLISECONDS);
    }

    @Override
    public @NotNull
    BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay) throws IllegalArgumentException {
        return this.runTaskLaterAsynchronously(plugin, (Runnable) task, delay);
    }

    @Override
    @NotNull
    public BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) throws IllegalArgumentException {
        Preconditions.checkArgument(delay >= 0, "delay smaller than 0");
        Preconditions.checkArgument(period > 0, "period smaller than 1");

        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, period, this.targetTick(delay), true);
        this.queuedTasks.add(serverTask);
        return serverTask;
    }

    @Override
    public void runTaskTimer(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay, long period) throws IllegalArgumentException {
        Preconditions.checkArgument(delay >= 0, "delay smaller than 0");
        Preconditions.checkArgument(period > 0, "period smaller than 1");

        ServerTask serverTask = new ConsumerServerTask(this.taskIds.incrementAndGet(), plugin, task, period, this.targetTick(delay), true);
        this.queuedTasks.add(serverTask);
    }

    @Override
    @NotNull
    public BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay, long period) throws IllegalArgumentException {
        return this.runTaskTimer(plugin, (Runnable) task, delay, period);
    }

    @Override
    @NotNull
    public BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) throws IllegalArgumentException {
        Preconditions.checkArgument(delay >= 0, "delay smaller than 0");
        Preconditions.checkArgument(period > 0, "period smaller than 1");

        ServerTask serverTask = new ServerTask(this.taskIds.incrementAndGet(), plugin, task, false, this.targetTick(delay));
        this.queuedTasks.add(serverTask);
        this.asyncWorker.scheduleAtFixedRate(serverTask, delay * (1000 / 20), period * (1000 / 20), TimeUnit.MILLISECONDS);
        return serverTask;
    }

    @Override
    public void runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay, long period) throws IllegalArgumentException {
        Preconditions.checkArgument(delay >= 0, "delay smaller than 0");
        Preconditions.checkArgument(period > 0, "period smaller than 1");

        ServerTask serverTask = new ConsumerServerTask(this.taskIds.incrementAndGet(), plugin, false, this.targetTick(delay), task);
        this.queuedTasks.add(serverTask);
        this.asyncWorker.scheduleAtFixedRate(serverTask, delay * (1000 / 20), period * (1000 / 20), TimeUnit.MILLISECONDS);
    }

    @Override
    @NotNull
    public BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay, long period) throws IllegalArgumentException {
        return this.runTaskTimerAsynchronously(plugin, (Runnable) task, delay, period);
    }

    public void heartbeat(long currentTick) {
        for (ServerTask queuedTask : this.queuedTasks) {
            if (queuedTask.isSync()) {
                queuedTask.execute(currentTick);
            }
        }
    }

    public void fullHeartbeat(long currentTick) {
        this.heartbeat(currentTick);
        this.cleanupTasks();
    }

    private void cleanupTasks() {
        this.queuedTasks.removeIf(ServerTask::isCancelled);
    }

    private long nextTick() {
        return ServerTicker.getCurrentTick() + 1;
    }

    private long targetTick(long delay) {
        return ServerTicker.getCurrentTick() + (delay == 0 ? 1 : delay);
    }
}

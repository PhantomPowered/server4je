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
package com.github.phantompowered.server4je.api;

import com.github.phantompowered.server4je.api.network.NetworkManager;
import com.github.phantompowered.server4je.api.player.OfflinePlayerManager;
import com.github.phantompowered.server4je.api.player.PlayerManager;
import com.github.phantompowered.server4je.api.version.ServerVersion;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningExecutorService;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;

public abstract class PhantomServer implements Server {

    private static PhantomServer theServer;

    @NotNull
    public static PhantomServer getInstance() {
        return Preconditions.checkNotNull(PhantomServer.theServer, "Server instance was not set yet");
    }

    public static void setTheServer(@NotNull PhantomServer theServer) {
        PhantomServer.theServer = Preconditions.checkNotNull(theServer, "Server instance has to be non-null");
    }

    // ===

    @NotNull
    public abstract ListeningExecutorService getExecutor();

    @NotNull
    public abstract ServerVersion getServerVersion();

    @NotNull
    public abstract NetworkManager getNetworkManager();

    public abstract void broadcast(@NotNull BaseComponent[] message, @NotNull String permission);

    @NotNull
    public abstract PlayerManager getPlayerManager();

    @NotNull
    public abstract OfflinePlayerManager getOfflinePlayerManager();

    @Override
    @Deprecated
    public final void reload() {
    }

    @Override
    @Deprecated
    public abstract @NotNull Spigot spigot();
}

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
package com.github.phantompowered.server4je.network;

import com.github.phantompowered.server4je.api.network.NetworkListener;
import com.github.phantompowered.server4je.api.network.NetworkManager;
import com.github.phantompowered.server4je.common.collect.Iterables;
import com.github.phantompowered.server4je.network.handler.init.NetworkChannelInitializer;
import com.github.phantompowered.server4je.network.transport.EventLoopGroupType;
import com.github.phantompowered.server4je.network.transport.TransportType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class ServerNettyNetworkManager implements NetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerNettyNetworkManager.class);
    private static final WriteBufferWaterMark WATER_MARK = new WriteBufferWaterMark(524_288, 2_097_152);
    private static final CompletableFuture<Boolean> FALSE_COMPLETED_FUTURE = CompletableFuture.completedFuture(false);
    private static final TransportType TRANSPORT_TYPE = TransportType.getBestType();

    private final EventLoopGroup boss = TRANSPORT_TYPE.getEventLoopGroup(EventLoopGroupType.BOSS);
    private final EventLoopGroup worker = TRANSPORT_TYPE.getEventLoopGroup(EventLoopGroupType.WORKER);
    private final Object2ObjectMap<NetworkListener, ChannelFuture> activeChannels = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

    @Override
    @NotNull
    @UnmodifiableView
    public Collection<NetworkListener> getNetworkListeners() {
        return Collections.unmodifiableCollection(this.activeChannels.keySet());
    }

    @Override
    @NotNull
    public CompletableFuture<Boolean> addNetworkListener(@NotNull NetworkListener networkListener) {
        if (this.activeChannels.containsKey(networkListener)) {
            return FALSE_COMPLETED_FUTURE;
        }

        try {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            new ServerBootstrap()
                .channelFactory(TRANSPORT_TYPE.getServerSocketChannelFactory())
                .group(this.boss, this.worker)

                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.IP_TOS, 0x18)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WATER_MARK)

                .handler(new NetworkChannelInitializer(networkListener))
                .bind(networkListener.getHost())
                .addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        this.activeChannels.put(networkListener, channelFuture);
                        future.complete(true);
                    } else {
                        future.completeExceptionally(channelFuture.cause());
                    }
                });
            return future;
        } catch (Throwable throwable) {
            LOGGER.error("Unable to bind network listener " + networkListener.toString(), throwable);
            return FALSE_COMPLETED_FUTURE;
        }
    }

    @Override
    public boolean closeNetworkListener(@NotNull NetworkListener networkListener) {
        ChannelFuture channelFuture = this.activeChannels.remove(networkListener);
        if (channelFuture != null) {
            channelFuture.cancel(true);
        }

        return channelFuture != null;
    }

    @Override
    public void closeNetworkListeners(@NotNull Predicate<NetworkListener> listenerFilter) {
        for (Map.Entry<NetworkListener, ChannelFuture> entry : Iterables.allEntries(this.activeChannels.entrySet(), listenerFilter)) {
            this.closeNetworkListener(entry.getKey());
        }
    }

    @Override
    public void closeAllNetworkListeners() {
        for (NetworkListener networkListener : this.activeChannels.keySet()) {
            this.closeNetworkListener(networkListener);
        }
    }

    @Override
    public void close() {
        this.closeAllNetworkListeners();
        this.boss.shutdownGracefully();
        this.worker.shutdownGracefully();
    }
}

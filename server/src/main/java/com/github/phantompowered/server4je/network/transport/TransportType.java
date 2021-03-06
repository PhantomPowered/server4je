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
package com.github.phantompowered.server4je.network.transport;

import com.github.phantompowered.server4je.network.thread.FastNettyThreadFactory;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.function.BiFunction;

public enum TransportType {

    EPOLL("Epoll", Epoll.isAvailable(), EpollServerSocketChannel::new, EpollSocketChannel::new,
        (type, name) -> new EpollEventLoopGroup(Math.min(4, Runtime.getRuntime().availableProcessors() * 2), newThreadFactory(name, type))),
    K_QUEUE("KQueue", KQueue.isAvailable(), KQueueServerSocketChannel::new, KQueueSocketChannel::new,
        (type, name) -> new KQueueEventLoopGroup(Math.min(4, Runtime.getRuntime().availableProcessors() * 2), newThreadFactory(name, type))),
    NIO("Nio", true, NioServerSocketChannel::new, NioSocketChannel::new,
        (type, name) -> new NioEventLoopGroup(Math.min(4, Runtime.getRuntime().availableProcessors() * 2), newThreadFactory(name, type)));

    public static final TransportType[] VALUES = TransportType.values(); // prevent copy

    private final String name;
    private final boolean available;
    private final ChannelFactory<? extends ServerSocketChannel> serverSocketChannelFactory;
    private final ChannelFactory<? extends SocketChannel> socketChannelFactory;
    private final BiFunction<EventLoopGroupType, String, EventLoopGroup> eventLoopGroupFactory;

    TransportType(String name, boolean available, ChannelFactory<? extends ServerSocketChannel> serverSocketChannelFactory,
                  ChannelFactory<? extends SocketChannel> socketChannelFactory, BiFunction<EventLoopGroupType, String, EventLoopGroup> eventLoopGroupFactory) {
        this.name = name;
        this.available = available;
        this.serverSocketChannelFactory = serverSocketChannelFactory;
        this.socketChannelFactory = socketChannelFactory;
        this.eventLoopGroupFactory = eventLoopGroupFactory;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public @NotNull ChannelFactory<? extends ServerSocketChannel> getServerSocketChannelFactory() {
        return this.serverSocketChannelFactory;
    }

    public @NotNull ChannelFactory<? extends SocketChannel> getSocketChannelFactory() {
        return this.socketChannelFactory;
    }

    public @NotNull EventLoopGroup getEventLoopGroup(@NotNull EventLoopGroupType type) {
        return this.eventLoopGroupFactory.apply(type, this.getName());
    }

    public static @NotNull TransportType getBestType() {
        if (Boolean.getBoolean("netty.native-disabled")) {
            return NIO;
        }

        for (TransportType value : VALUES) {
            if (value.isAvailable()) {
                return value;
            }
        }

        return NIO;
    }

    @NotNull
    public static ThreadFactory newThreadFactory(@NotNull String name, @NotNull EventLoopGroupType type) {
        return new FastNettyThreadFactory("Netty " + type.getName() + ' ' + name + " Thread#%d");
    }
}

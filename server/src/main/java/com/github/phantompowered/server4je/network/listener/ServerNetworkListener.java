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
package com.github.phantompowered.server4je.network.listener;

import com.github.phantompowered.server4je.api.network.NetworkListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class ServerNetworkListener implements NetworkListener {

    private final String host;
    private final int port;

    private transient InetSocketAddress inetAddress;
    private boolean preventProxyConnections;

    public ServerNetworkListener(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ServerNetworkListener(String host, int port, boolean preventProxyConnections) {
        this.host = host;
        this.port = port;
        this.preventProxyConnections = preventProxyConnections;
    }

    @Override
    @NotNull
    public InetSocketAddress getHost() {
        if (this.inetAddress == null) {
            try {
                this.inetAddress = new InetSocketAddress(InetAddress.getByName(this.host), this.port);
            } catch (UnknownHostException exception) {
                throw new RuntimeException("Unable to parse inet address provided by " + this.host, exception);
            }
        }

        return this.inetAddress;
    }

    @Override
    @NotNull
    public String getHostString() {
        return this.host;
    }

    @Override
    public @Range(from = 0, to = 65535) int getPort() {
        return this.port;
    }

    @Override
    public boolean isPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    @Override
    public void setPreventProxyConnections(boolean preventProxyConnections) {
        this.preventProxyConnections = preventProxyConnections;
    }
}

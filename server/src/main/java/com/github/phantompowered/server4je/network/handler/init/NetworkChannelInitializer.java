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
package com.github.phantompowered.server4je.network.handler.init;

import com.github.phantompowered.server4je.api.PhantomServer;
import com.github.phantompowered.server4je.api.network.NetworkListener;
import com.github.phantompowered.server4je.network.NetworkConstants;
import com.github.phantompowered.server4je.network.handler.legacy.LegacyDecoder;
import com.github.phantompowered.server4je.network.handler.legacy.LegacyEncoder;
import com.github.phantompowered.server4je.network.handler.packet.PacketDecoder;
import com.github.phantompowered.server4je.network.handler.packet.PacketEncoder;
import com.github.phantompowered.server4je.network.handler.varint.VarInt21FrameDecoder;
import com.github.phantompowered.server4je.network.handler.varint.VarInt21FrameEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class NetworkChannelInitializer extends ChannelInitializer<Channel> {

    private final PhantomServer phantomServer = PhantomServer.getInstance();
    private final NetworkListener networkListener;

    public NetworkChannelInitializer(@NotNull NetworkListener networkListener) {
        this.networkListener = networkListener;
    }

    @Override
    protected void initChannel(@NotNull Channel channel) {
        channel.pipeline()
            .addLast(NetworkConstants.READ_TIMEOUT, new ReadTimeoutHandler(this.phantomServer.getConfig().getReadTimeoutMilliseconds(), TimeUnit.MILLISECONDS))
            .addLast(NetworkConstants.LEGACY_DECODER, new LegacyDecoder())
            .addLast(NetworkConstants.VAR_INT_21_FRAME_DECODER, new VarInt21FrameDecoder())
            .addLast(NetworkConstants.LEGACY_ENCODER, LegacyEncoder.LEGACY_ENCODER)
            .addLast(NetworkConstants.VAR_INT_21_FRAME_ENCODER, VarInt21FrameEncoder.ENCODER)
            .addLast(NetworkConstants.PACKET_DECODER, new PacketDecoder(null))  // TODO
            .addLast(NetworkConstants.PACKET_ENCODER, new PacketEncoder(null)); // TODO
    }
}

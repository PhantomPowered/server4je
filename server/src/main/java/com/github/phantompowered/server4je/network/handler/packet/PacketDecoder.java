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
package com.github.phantompowered.server4je.network.handler.packet;

import com.github.phantompowered.server4je.network.buffer.ByteBufUtil;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.buffer.DataBufferFactory;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketDecoder extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketDecoder.class);
    private static final IllegalStateException DECODE_EXCEPTION = new IllegalStateException("Did not decode packet correctly");

    private final DataBufferFactory dataBufferFactory;
    private ProtocolState protocolState;

    public PacketDecoder(DataBufferFactory dataBufferFactory) {
        this.dataBufferFactory = dataBufferFactory;
        this.protocolState = ProtocolState.HANDSHAKING;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf byteBuf = (ByteBuf) msg;
        if (!ctx.channel().isActive()) {
            ByteBufUtil.releaseFully(byteBuf);
            return;
        }

        final DataBuffer dataBuffer = this.dataBufferFactory.createDataBuffer(byteBuf);
        try {
            final Packet packet = PacketIdUtil.getClientPacket(this.protocolState, (short) dataBuffer.readVarInt());

            try {
                packet.readData(dataBuffer);
            } catch (Throwable throwable) {
                throw LOGGER.isDebugEnabled() ? new RuntimeException(
                    "Error decoding packet " + packet.getClass().getName() + " @ " + packet.getId(), throwable
                ) : DECODE_EXCEPTION;
            }

            if (dataBuffer.isReadable()) {
                throw LOGGER.isDebugEnabled() ? new RuntimeException(
                    "Did not read all bytes from packet " + packet.getClass().getName() + " @ " + packet.getId()
                ) : DECODE_EXCEPTION;
            }

            ctx.fireChannelRead(packet);
        } finally {
            ByteBufUtil.releaseFully(dataBuffer);
        }
    }

    public ProtocolState getProtocolState() {
        return this.protocolState;
    }

    public void setProtocolState(ProtocolState protocolState) {
        this.protocolState = protocolState;
    }
}

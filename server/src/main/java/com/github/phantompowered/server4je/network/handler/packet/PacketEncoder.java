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

import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.buffer.DataBufferFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketEncoder.class);
    private static final IllegalStateException ENCODE_ERROR = new IllegalStateException("Unable to encode packet correctly");
    private final DataBufferFactory dataBufferFactory;

    public PacketEncoder(DataBufferFactory dataBufferFactory) {
        this.dataBufferFactory = dataBufferFactory;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) {
        try {
            DataBuffer dataBuffer = this.dataBufferFactory.createDataBuffer(byteBuf);
            dataBuffer.writeVarInt(packet.getId());
            packet.writeData(dataBuffer);
        } catch (Throwable throwable) {
            throw LOGGER.isDebugEnabled() ? new RuntimeException(
                "Unable to encode packet " + packet.getClass().getName() + " correctly", throwable
            ) : ENCODE_ERROR;
        }
    }
}

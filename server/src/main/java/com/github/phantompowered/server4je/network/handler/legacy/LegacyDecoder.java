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
package com.github.phantompowered.server4je.network.handler.legacy;

import com.github.phantompowered.server4je.protocol.legacy.in.PacketInLegacyHandshake;
import com.github.phantompowered.server4je.protocol.legacy.in.PacketInLegacyPing;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LegacyDecoder extends ByteToMessageDecoder {

    private static final short PING_IDENTIFIER = 0xfe;
    private static final short HANDSHAKE_IDENTIFIER = 0x02;
    private static final String CHANNEL_1_6 = "MC|PingHost";

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        if (!byteBuf.isReadable()) {
            return;
        }

        if (!channelHandlerContext.channel().isActive()) {
            byteBuf.skipBytes(byteBuf.readableBytes());
            return;
        }

        final int readerIndex = byteBuf.readerIndex();
        final short packetIdentifier = byteBuf.readUnsignedByte();
        if (packetIdentifier == PING_IDENTIFIER) {
            if (!byteBuf.isReadable()) {
                // beta 1.8 - 1.3 ping
                list.add(new PacketInLegacyPing(PacketInLegacyPing.Version.MC_1_3));
                return;
            }

            short pingPayload = byteBuf.readUnsignedByte();
            if (pingPayload == 0x01 && !byteBuf.isReadable()) {
                // 1.4 - 1.5 ping
                list.add(new PacketInLegacyPing(PacketInLegacyPing.Version.MC_1_4));
                return;
            }

            // 1.6 ping
            list.add(readPing(byteBuf));
        } else if (packetIdentifier == HANDSHAKE_IDENTIFIER && byteBuf.isReadable()) {
            // legacy handshake
            byteBuf.skipBytes(byteBuf.readableBytes());
            list.add(new PacketInLegacyHandshake());
        } else {
            byteBuf.readerIndex(readerIndex);
            channelHandlerContext.pipeline().remove(this);
        }
    }

    @NotNull
    private static PacketInLegacyPing readPing(@NotNull ByteBuf byteBuf) {
        byteBuf.skipBytes(1); // skip the plugin message packet identifier

        final String channelIdentifier = readUTF16BEString(byteBuf);
        Preconditions.checkArgument(channelIdentifier.endsWith(CHANNEL_1_6), "Invalid channel sent by client for legacy ping");

        byteBuf.skipBytes(3); // skip the rest data length (2) and the protocol version (1)

        final String connectingHost = readUTF16BEString(byteBuf);
        final int connectingPort = byteBuf.readInt();

        return new PacketInLegacyPing(PacketInLegacyPing.Version.MC_1_6, InetSocketAddress.createUnresolved(connectingHost, connectingPort));
    }

    @NotNull
    private static String readUTF16BEString(@NotNull ByteBuf byteBuf) {
        final int stringLength = byteBuf.readShort() * Character.BYTES;
        Preconditions.checkArgument(byteBuf.isReadable(stringLength), "Invalid string length sent");

        final String string = byteBuf.toString(byteBuf.readerIndex(), stringLength, StandardCharsets.UTF_16BE);
        byteBuf.skipBytes(stringLength);
        return string;
    }
}

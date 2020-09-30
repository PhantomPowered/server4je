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
package com.github.phantompowered.server4je.network.handler.varint;

import com.github.phantompowered.server4je.network.buffer.ByteBufUtil;
import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class VarInt21FrameEncoder extends MessageToByteEncoder<ByteBuf> {

    public static final VarInt21FrameEncoder ENCODER = new VarInt21FrameEncoder();
    private static final boolean HEAP_BUFFERS = Natives.cipher.get() == JavaVelocityCipher.FACTORY;

    private VarInt21FrameEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) {
        ByteBufUtil.writeUnsignedVarInt(byteBuf2, byteBuf.readableBytes());
        byteBuf2.writeBytes(byteBuf);
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        return HEAP_BUFFERS
            ? ctx.alloc().heapBuffer(5 + msg.readableBytes())
            : ctx.alloc().directBuffer(5 + msg.readableBytes());
    }
}

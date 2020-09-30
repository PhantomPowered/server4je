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
package com.github.phantompowered.server4je.network.handler.cipher;

import com.github.phantompowered.server4je.network.buffer.ByteBufUtil;
import com.velocitypowered.natives.encryption.VelocityCipher;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class PacketCipher extends MessageToMessageEncoder<ByteBuf> {

    private final VelocityCipher cipher;

    public PacketCipher(VelocityCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) {
        ByteBuf in = MoreByteBufUtils.ensureCompatible(context.alloc(), this.cipher, byteBuf);
        try {
            this.cipher.process(in);
            list.add(in);
        } catch (Throwable throwable) {
            ByteBufUtil.releaseFully(in);
            throw throwable;
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.cipher.close();
    }
}

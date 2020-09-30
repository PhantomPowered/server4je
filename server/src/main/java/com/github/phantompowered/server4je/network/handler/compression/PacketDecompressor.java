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
package com.github.phantompowered.server4je.network.handler.compression;

import com.github.phantompowered.server4je.network.buffer.ByteBufUtil;
import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.natives.util.MoreByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class PacketDecompressor extends MessageToMessageDecoder<ByteBuf> {

    private static final int MAX_UNCOMPRESSED_SIZE = 2 * 1024 * 1024;

    private final int threshold;
    private final VelocityCompressor compressor;

    public PacketDecompressor(int threshold, VelocityCompressor compressor) {
        this.threshold = threshold;
        this.compressor = compressor;
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf byteBuf, List<Object> list) throws Exception {
        int uncompressedSize = ByteBufUtil.readUnsignedVarInt(byteBuf);
        if (uncompressedSize == 0) {
            // uncompressed message
            list.add(byteBuf.retainedSlice());
            return;
        }

        checkArgument(
            uncompressedSize >= this.threshold,
            "Uncompressed size %s is smaller than threshold %s", uncompressedSize, this.threshold
        );
        checkArgument(
            uncompressedSize <= MAX_UNCOMPRESSED_SIZE,
            "Uncompressed size %s exceeds hard limit of %s bytes", uncompressedSize, MAX_UNCOMPRESSED_SIZE
        );

        ByteBuf in = MoreByteBufUtils.ensureCompatible(context.alloc(), this.compressor, byteBuf);
        ByteBuf out = MoreByteBufUtils.preferredBuffer(context.alloc(), this.compressor, uncompressedSize);
        try {
            this.compressor.inflate(in, out, uncompressedSize);
            list.add(out);
        } catch (Throwable throwable) {
            ByteBufUtil.releaseFully(out);
            throw throwable;
        } finally {
            ByteBufUtil.releaseFully(in);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.compressor.close();
    }
}

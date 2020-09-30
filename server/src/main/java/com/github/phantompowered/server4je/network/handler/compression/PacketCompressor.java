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
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketCompressor extends MessageToByteEncoder<ByteBuf> {

    private final int threshold;
    private final VelocityCompressor compressor;

    public PacketCompressor(int threshold, VelocityCompressor compressor) {
        this.threshold = threshold;
        this.compressor = compressor;
    }

    @Override
    protected void encode(ChannelHandlerContext context, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
        int uncompressed = byteBuf.readableBytes();
        if (uncompressed <= this.threshold) {
            ByteBufUtil.writeUnsignedVarInt(byteBuf2, 0);
            byteBuf2.writeBytes(byteBuf);
            return;
        }

        ByteBufUtil.writeUnsignedVarInt(byteBuf2, uncompressed);
        ByteBuf in = MoreByteBufUtils.ensureCompatible(context.alloc(), this.compressor, byteBuf);

        try {
            this.compressor.deflate(in, byteBuf2);
        } finally {
            ByteBufUtil.releaseFully(in);
        }
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
        // We allocate bytes to be compressed plus 1 byte. This covers two cases:
        //
        // - Compression
        //    According to https://github.com/ebiggers/libdeflate/blob/master/libdeflate.h#L103,
        //    if the data compresses well (and we do not have some pathological case) then the maximum
        //    size the compressed size will ever be is the input size minus one.
        // - Uncompressed
        //    This is fairly obvious - we will then have one more than the uncompressed size.
        // (Copied from https://github.com/VelocityPowered/Velocity class MinecraftCompressEncoder)
        return MoreByteBufUtils.preferredBuffer(ctx.alloc(), this.compressor, msg.readableBytes() + 1);
    }
}

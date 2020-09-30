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
package com.github.phantompowered.server4je.network.buffer;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ByteBufUtil {

    private static final IllegalArgumentException BAD_VAR_INT_DECODED = new IllegalArgumentException("Bad VarInt decoded");

    private ByteBufUtil() {
        throw new UnsupportedOperationException();
    }

    public static void writeUnsignedVarInt(ByteBuf byteBuf, int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            byteBuf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }

        byteBuf.writeByte(value & 0x7F);
    }

    public static void writeUnsignedVarLong(ByteBuf byteBuf, long value) {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            byteBuf.writeByte(((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }

        byteBuf.writeByte((int) value & 0x7F);
    }

    public static int readUnsignedVarInt(@NotNull ByteBuf byteBuf) {
        return (int) readUnsignedLongFixedMaximumLength(byteBuf, Math.min(5, byteBuf.readableBytes()));
    }

    public static long readUnsignedVarLong(@NotNull ByteBuf byteBuf) {
        return readUnsignedLongFixedMaximumLength(byteBuf, Math.min(10, byteBuf.readableBytes()));
    }

    private static long readUnsignedLongFixedMaximumLength(@NotNull ByteBuf byteBuf, int maxRead) {
        Long varLong = readUnsignedLongFixedMaximumLengthSilently(byteBuf, maxRead);
        if (varLong == null) {
            throw BAD_VAR_INT_DECODED;
        }

        return varLong;
    }

    @Nullable
    public static Long readUnsignedLongFixedMaximumLengthSilently(@NotNull ByteBuf byteBuf, int maxRead) {
        long i = 0;
        for (int j = 0; j < maxRead; j++) {
            int k = byteBuf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }

        return null;
    }

    public static void releaseFully(@NotNull ByteBuf byteBuf) {
        if (byteBuf.refCnt() > 0) {
            byteBuf.release(byteBuf.refCnt());
        }
    }
}

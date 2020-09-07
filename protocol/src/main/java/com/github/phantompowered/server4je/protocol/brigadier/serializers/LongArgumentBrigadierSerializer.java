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
package com.github.phantompowered.server4je.protocol.brigadier.serializers;

import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.brigadier.BrigadierSerializer;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.mojang.brigadier.arguments.LongArgumentType;
import org.jetbrains.annotations.NotNull;

public final class LongArgumentBrigadierSerializer implements BrigadierSerializer<LongArgumentType> {

    public static BrigadierSerializer<LongArgumentType> INSTANCE = new LongArgumentBrigadierSerializer();

    private LongArgumentBrigadierSerializer() {
    }

    @Override
    public void serialize(@NotNull LongArgumentType longArgumentType, @BufferStatus(BufferStatus.Status.EMPTY) @NotNull DataBuffer dataBuffer) {
        boolean hasMinimum = longArgumentType.getMinimum() != Long.MIN_VALUE;
        boolean hasMaximum = longArgumentType.getMaximum() != Long.MAX_VALUE;

        dataBuffer.writeByte(DoubleArgumentBrigadierSerializer.getFlags(hasMinimum, hasMaximum));
        if (hasMinimum) {
            dataBuffer.writeLong(longArgumentType.getMinimum());
        }

        if (hasMaximum) {
            dataBuffer.writeLong(longArgumentType.getMaximum());
        }
    }

    @Override
    public @NotNull LongArgumentType deserialize(@BufferStatus(BufferStatus.Status.FILLED) @NotNull DataBuffer dataBuffer) {
        byte flags = dataBuffer.readByte();
        long min = (flags & 1) != 0 ? dataBuffer.readLong() : Long.MIN_VALUE;
        long max = (flags & 2) != 0 ? dataBuffer.readLong() : Long.MAX_VALUE;
        return LongArgumentType.longArg(min, max);
    }
}

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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.jetbrains.annotations.NotNull;

public final class IntegerArgumentBrigadierSerializer implements BrigadierSerializer<IntegerArgumentType> {

    public static final BrigadierSerializer<IntegerArgumentType> INSTANCE = new IntegerArgumentBrigadierSerializer();

    private IntegerArgumentBrigadierSerializer() {
    }

    @Override
    public void serialize(@NotNull IntegerArgumentType integerArgumentType, @BufferStatus(BufferStatus.Status.EMPTY) @NotNull DataBuffer dataBuffer) {
        boolean hasMinimum = integerArgumentType.getMinimum() != Integer.MIN_VALUE;
        boolean hasMaximum = integerArgumentType.getMaximum() != Integer.MAX_VALUE;

        dataBuffer.writeByte(DoubleArgumentBrigadierSerializer.getFlags(hasMinimum, hasMaximum));
        if (hasMinimum) {
            dataBuffer.writeInt(integerArgumentType.getMinimum());
        }

        if (hasMaximum) {
            dataBuffer.writeInt(integerArgumentType.getMaximum());
        }
    }

    @Override
    public @NotNull IntegerArgumentType deserialize(@BufferStatus(BufferStatus.Status.FILLED) @NotNull DataBuffer dataBuffer) {
        byte flags = dataBuffer.readByte();
        int min = (flags & 1) != 0 ? dataBuffer.readInt() : Integer.MIN_VALUE;
        int max = (flags & 2) != 0 ? dataBuffer.readInt() : Integer.MAX_VALUE;
        return IntegerArgumentType.integer(min, max);
    }
}

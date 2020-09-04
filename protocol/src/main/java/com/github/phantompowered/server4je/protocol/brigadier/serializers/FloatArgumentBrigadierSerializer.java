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
import com.mojang.brigadier.arguments.FloatArgumentType;
import org.jetbrains.annotations.NotNull;

public final class FloatArgumentBrigadierSerializer implements BrigadierSerializer<FloatArgumentType> {

    public static final BrigadierSerializer<FloatArgumentType> INSTANCE = new FloatArgumentBrigadierSerializer();

    private FloatArgumentBrigadierSerializer() {
    }

    @Override
    public void serialize(@NotNull FloatArgumentType floatArgumentType, @BufferStatus(BufferStatus.Status.EMPTY) @NotNull DataBuffer dataBuffer) {
        boolean hasMinimum = Float.compare(floatArgumentType.getMinimum(), Float.MIN_VALUE) != 0;
        boolean hasMaximum = Float.compare(floatArgumentType.getMaximum(), Float.MAX_VALUE) != 0;

        dataBuffer.writeByte(DoubleArgumentBrigadierSerializer.getFlags(hasMinimum, hasMaximum));
        if (hasMinimum) {
            dataBuffer.writeFloat(floatArgumentType.getMinimum());
        }

        if (hasMaximum) {
            dataBuffer.writeFloat(floatArgumentType.getMaximum());
        }
    }

    @Override
    public @NotNull FloatArgumentType deserialize(@BufferStatus(BufferStatus.Status.FILLED) @NotNull DataBuffer dataBuffer) {
        byte flags = dataBuffer.readByte();
        float min = (flags & 1) != 0 ? dataBuffer.readFloat() : Float.MIN_VALUE;
        float max = (flags & 2) != 0 ? dataBuffer.readFloat() : Float.MAX_VALUE;
        return FloatArgumentType.floatArg(min, max);
    }
}

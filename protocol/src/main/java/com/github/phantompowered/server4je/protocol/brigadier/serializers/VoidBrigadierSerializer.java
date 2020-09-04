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
import org.jetbrains.annotations.NotNull;

public final class VoidBrigadierSerializer implements BrigadierSerializer<Void> {

    public static final BrigadierSerializer<Void> INSTANCE = new VoidBrigadierSerializer();

    private VoidBrigadierSerializer() {
    }

    @Override
    public void serialize(@NotNull Void unused, @BufferStatus(BufferStatus.Status.EMPTY) @NotNull DataBuffer dataBuffer) {
    }

    @Override
    public @NotNull Void deserialize(@BufferStatus(BufferStatus.Status.FILLED) @NotNull DataBuffer dataBuffer) {
        return null;
    }
}

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
package com.github.phantompowered.server4je.protocol.play.out;

import com.destroystokyo.paper.Namespaced;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import org.bukkit.SoundCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutStopSound implements Packet {

    private Namespaced key;
    private SoundCategory soundCategory;

    public PacketOutStopSound() {
    }

    public PacketOutStopSound(Namespaced key, SoundCategory soundCategory) {
        this.key = key;
        this.soundCategory = soundCategory;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        if (this.soundCategory != null) {
            if (this.key == null) {
                dataBuffer.writeByte(1);
                dataBuffer.writeVarInt(this.soundCategory.ordinal());
                return;
            }

            dataBuffer.writeByte(3);
            dataBuffer.writeVarInt(this.soundCategory.ordinal());
            dataBuffer.writeNamespaced(this.key);
            return;
        }

        if (this.key != null) {
            dataBuffer.writeByte(2);
            dataBuffer.writeNamespaced(this.key);
            return;
        }

        dataBuffer.writeByte(0);
    }

    @Override
    public void releaseData() {
        this.key = null;
        this.soundCategory = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return 0;
    }

    public Namespaced getKey() {
        return this.key;
    }

    public void setKey(Namespaced key) {
        this.key = key;
    }

    public SoundCategory getSoundCategory() {
        return this.soundCategory;
    }

    public void setSoundCategory(SoundCategory soundCategory) {
        this.soundCategory = soundCategory;
    }
}

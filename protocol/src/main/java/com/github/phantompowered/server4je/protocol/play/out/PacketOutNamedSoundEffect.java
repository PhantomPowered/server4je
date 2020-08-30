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

import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutNamedSoundEffect implements Packet {

    private Sound sound;
    private SoundCategory soundCategory;
    private int x;
    private int y;
    private int z;
    private float volume;
    private float pitch;

    public PacketOutNamedSoundEffect() {
    }

    public PacketOutNamedSoundEffect(Sound sound, SoundCategory soundCategory, int x, int y, int z, float volume, float pitch) {
        this.sound = sound;
        this.soundCategory = soundCategory;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.sound.ordinal());
        dataBuffer.writeVarInt(this.soundCategory.ordinal());
        dataBuffer.writeInt(this.x);
        dataBuffer.writeInt(this.y);
        dataBuffer.writeInt(this.z);
        dataBuffer.writeFloat(this.volume);
        dataBuffer.writeFloat(this.pitch);
    }

    @Override
    public void releaseData() {
        this.sound = null;
        this.soundCategory = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return 0;
    }

    public Sound getSound() {
        return sound;
    }

    public void setSound(Sound sound) {
        this.sound = sound;
    }

    public SoundCategory getSoundCategory() {
        return soundCategory;
    }

    public void setSoundCategory(SoundCategory soundCategory) {
        this.soundCategory = soundCategory;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}

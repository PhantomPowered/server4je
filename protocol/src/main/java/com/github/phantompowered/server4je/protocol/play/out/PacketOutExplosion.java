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

import com.github.phantompowered.server4je.common.math.MathHelper;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;

public class PacketOutExplosion implements Packet {

    private double x;
    private double y;
    private double z;
    private float strength;
    private Collection<Location> records;
    private Vector playerMotion;

    public PacketOutExplosion() {
    }

    public PacketOutExplosion(double x, double y, double z, float strength, Collection<Location> records, Vector playerMotion) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strength = strength;
        this.records = records;
        this.playerMotion = playerMotion;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeFloat((float) this.x);
        dataBuffer.writeFloat((float) this.y);
        dataBuffer.writeFloat((float) this.z);
        dataBuffer.writeFloat(this.strength);

        final int flooredX = MathHelper.floor(this.x);
        final int flooredY = MathHelper.floor(this.y);
        final int flooredZ = MathHelper.floor(this.z);

        dataBuffer.writeInt(this.records.size());
        for (Location record : this.records) {
            dataBuffer.writeByte(record.getBlockX() - flooredX);
            dataBuffer.writeByte(record.getBlockY() - flooredY);
            dataBuffer.writeByte(record.getBlockZ() - flooredZ);
        }

        dataBuffer.writeFloat((float) this.playerMotion.getX());
        dataBuffer.writeFloat((float) this.playerMotion.getY());
        dataBuffer.writeFloat((float) this.playerMotion.getZ());
    }

    @Override
    public void releaseData() {
        this.records = null;
        this.playerMotion = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutExplosion.class);
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getStrength() {
        return this.strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public Collection<Location> getRecords() {
        return this.records;
    }

    public void setRecords(Collection<Location> records) {
        this.records = records;
    }

    public Vector getPlayerMotion() {
        return this.playerMotion;
    }

    public void setPlayerMotion(Vector playerMotion) {
        this.playerMotion = playerMotion;
    }
}

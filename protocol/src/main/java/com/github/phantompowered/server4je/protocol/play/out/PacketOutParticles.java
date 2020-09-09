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
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

public class PacketOutParticles implements Packet {

    private int id;
    private boolean overrideLimiter;
    private double x;
    private double y;
    private double z;
    private float xDistance;
    private float yDistance;
    private float zDistance;
    private float maxSpeed;
    private int count;
    private Consumer<DataBuffer> particleSerializer;

    public PacketOutParticles() {
    }

    public PacketOutParticles(int id, double x, double y, double z, float xDistance, float yDistance,
                              float zDistance, float maxSpeed, int count, boolean overrideLimiter, Consumer<DataBuffer> particleSerializer) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xDistance = xDistance;
        this.yDistance = yDistance;
        this.zDistance = zDistance;
        this.maxSpeed = maxSpeed;
        this.count = count;
        this.overrideLimiter = overrideLimiter;
        this.particleSerializer = particleSerializer;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeInt(this.id);
        dataBuffer.writeBoolean(this.overrideLimiter);
        dataBuffer.writeDouble(this.x);
        dataBuffer.writeDouble(this.y);
        dataBuffer.writeDouble(this.z);
        dataBuffer.writeFloat(this.xDistance);
        dataBuffer.writeFloat(this.yDistance);
        dataBuffer.writeFloat(this.zDistance);
        dataBuffer.writeFloat(this.maxSpeed);
        dataBuffer.writeInt(this.count);
        this.particleSerializer.accept(dataBuffer);
    }

    @Override
    public void releaseData() {
        this.particleSerializer = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutParticles.class);
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
    }

    public void setOverrideLimiter(boolean overrideLimiter) {
        this.overrideLimiter = overrideLimiter;
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

    public float getxDistance() {
        return this.xDistance;
    }

    public void setxDistance(float xDistance) {
        this.xDistance = xDistance;
    }

    public float getyDistance() {
        return this.yDistance;
    }

    public void setyDistance(float yDistance) {
        this.yDistance = yDistance;
    }

    public float getzDistance() {
        return this.zDistance;
    }

    public void setzDistance(float zDistance) {
        this.zDistance = zDistance;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Consumer<DataBuffer> getParticleSerializer() {
        return this.particleSerializer;
    }

    public void setParticleSerializer(Consumer<DataBuffer> particleSerializer) {
        this.particleSerializer = particleSerializer;
    }
}

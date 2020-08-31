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
package com.github.phantompowered.server4je.protocol.play.in;

import com.github.phantompowered.server4je.common.annotation.Note;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyFromClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class PacketInPlayerMovement implements Packet {

    @Note("Always present")
    private boolean onGround;

    protected Double x;
    protected Double y;
    protected Double z;

    protected Float yaw;
    protected Float pitch;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        this.onGround = dataBuffer.readUnsignedByte() != 0;
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        PacketOnlyFromClientException.throwNow();
    }

    @Override
    public void releaseData() {
        this.x = null;
        this.y = null;
        this.z = null;
        this.yaw = null;
        this.pitch = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getClientPacketId(ProtocolState.PLAY, PacketInPlayerMovement.class);
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    @Nullable
    public Double getX() {
        return this.x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    @Nullable
    public Double getY() {
        return this.y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    @Nullable
    public Double getZ() {
        return this.z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    @Nullable
    public Float getYaw() {
        return this.yaw;
    }

    public void setYaw(Float yaw) {
        this.yaw = yaw;
    }

    @Nullable
    public Float getPitch() {
        return this.pitch;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }
}

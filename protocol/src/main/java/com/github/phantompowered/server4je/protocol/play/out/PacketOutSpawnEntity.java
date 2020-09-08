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
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public class PacketOutSpawnEntity implements Packet {

    private int entityId;
    private UUID entityUniqueId;
    private int entityType;
    private double x;
    private double y;
    private double z;
    private int yaw;
    private int pitch;
    private int data;
    private int velocityX;
    private int velocityY;
    private int velocityZ;

    public PacketOutSpawnEntity() {
    }

    public PacketOutSpawnEntity(@NotNull Entity entity, int data, @NotNull Vector velocity) {
        this.entityId = entity.getEntityId();
        this.entityUniqueId = entity.getUniqueId();
        this.entityType = entity.getType().getTypeId();
        this.x = entity.getLocation().getX();
        this.y = entity.getLocation().getY();
        this.z = entity.getLocation().getZ();
        this.yaw = MathHelper.floor(entity.getLocation().getYaw() * 256.0F / 360.0F);
        this.pitch = MathHelper.floor(entity.getLocation().getPitch() * 256.0F / 360.0F);
        this.data = data;
        this.velocityX = (int) (MathHelper.clamp(velocity.getX(), -3.9D, 3.9D) * 8000D);
        this.velocityY = (int) (MathHelper.clamp(velocity.getY(), -3.9D, 3.9D) * 8000D);
        this.velocityZ = (int) (MathHelper.clamp(velocity.getZ(), -3.9D, 3.9D) * 8000D);
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.entityId);
        dataBuffer.writeUniqueId(this.entityUniqueId);
        dataBuffer.writeVarInt(this.entityType);
        dataBuffer.writeDouble(this.x);
        dataBuffer.writeDouble(this.y);
        dataBuffer.writeDouble(this.z);
        dataBuffer.writeByte(this.pitch);
        dataBuffer.writeByte(this.yaw);
        dataBuffer.writeInt(this.data);
        dataBuffer.writeShort(this.velocityX);
        dataBuffer.writeShort(this.velocityY);
        dataBuffer.writeShort(this.velocityZ);
    }

    @Override
    public void releaseData() {
        this.entityUniqueId = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutSpawnEntity.class);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public UUID getEntityUniqueId() {
        return this.entityUniqueId;
    }

    public void setEntityUniqueId(UUID entityUniqueId) {
        this.entityUniqueId = entityUniqueId;
    }

    public int getEntityType() {
        return this.entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
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

    public int getYaw() {
        return this.yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getPitch() {
        return this.pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getData() {
        return this.data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public int getVelocityX() {
        return this.velocityX;
    }

    public void setVelocityX(int velocityX) {
        this.velocityX = velocityX;
    }

    public int getVelocityY() {
        return this.velocityY;
    }

    public void setVelocityY(int velocityY) {
        this.velocityY = velocityY;
    }

    public int getVelocityZ() {
        return this.velocityZ;
    }

    public void setVelocityZ(int velocityZ) {
        this.velocityZ = velocityZ;
    }
}

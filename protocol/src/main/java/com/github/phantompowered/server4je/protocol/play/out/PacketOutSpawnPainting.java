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

import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.github.phantompowered.server4je.protocol.utils.LocationUtil;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Painting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public class PacketOutSpawnPainting implements Packet {

    private int entityId;
    private UUID entityUniqueId;
    private int motive;
    private Location position;
    private int direction;

    public PacketOutSpawnPainting() {
    }

    public PacketOutSpawnPainting(@NotNull Painting painting) {
        this.entityId = painting.getEntityId();
        this.entityUniqueId = painting.getUniqueId();
        this.motive = painting.getArt().getId();
        this.position = painting.getLocation();
        this.direction = PacketOutSpawnPainting.getRotationValueFromBlockFace(painting.getFacing());
        Preconditions.checkArgument(this.direction != -1, "Unable to use UP or DOWN direction.");
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.entityId);
        dataBuffer.writeUniqueId(this.entityUniqueId);
        dataBuffer.writeVarInt(this.motive);
        dataBuffer.writeLong(LocationUtil.locationToLong(this.position));
        dataBuffer.writeByte(this.direction);
    }

    @Override
    public void releaseData() {
        this.entityUniqueId = null;
        this.position = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutSpawnPainting.class);
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

    public int getMotive() {
        return this.motive;
    }

    public void setMotive(int motive) {
        this.motive = motive;
    }

    public Location getPosition() {
        return this.position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public int getDirection() {
        return this.direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public static int getRotationValueFromBlockFace(@NotNull BlockFace blockFace) {
        switch (blockFace) {
            case DOWN:
            case UP:
                return -1;
            case SOUTH:
                return 0;
            case WEST:
                return 1;
            case NORTH:
                return 2;
            case EAST:
                return 3;
            default:
                throw ReportedException.forMessage("Rotation value is unknown for block face " + blockFace);
        }
    }
}

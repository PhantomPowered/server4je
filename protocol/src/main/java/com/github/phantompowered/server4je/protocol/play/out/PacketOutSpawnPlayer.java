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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public class PacketOutSpawnPlayer implements Packet {

    private int entityId;
    private UUID playerUniqueId;
    private Location location;

    public PacketOutSpawnPlayer() {
    }

    public PacketOutSpawnPlayer(@NotNull Player player) {
        this(player.getEntityId(), player.getUniqueId(), player.getLocation());
    }

    public PacketOutSpawnPlayer(int entityId, UUID playerUniqueId, Location location) {
        this.entityId = entityId;
        this.playerUniqueId = playerUniqueId;
        this.location = location;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.entityId);
        dataBuffer.writeUniqueId(this.playerUniqueId);
        dataBuffer.writeDouble(this.location.getX());
        dataBuffer.writeDouble(this.location.getY());
        dataBuffer.writeDouble(this.location.getZ());
        dataBuffer.writeByte((byte) (this.location.getYaw() * 256.0F / 360.0F));
        dataBuffer.writeByte((byte) (this.location.getPitch() * 256.0F / 360.0F));
    }

    @Override
    public void releaseData() {
        this.playerUniqueId = null;
        this.location = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutSpawnPlayer.class);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public UUID getPlayerUniqueId() {
        return this.playerUniqueId;
    }

    public void setPlayerUniqueId(UUID playerUniqueId) {
        this.playerUniqueId = playerUniqueId;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

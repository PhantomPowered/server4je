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
import com.github.phantompowered.server4je.protocol.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutMultiBlockChange implements Packet {

    private static short toBlockLocation(@NotNull Location blockLocation) {
        return (short) ((blockLocation.getBlockX() & 15) << 8 | (blockLocation.getBlockZ() & 15) << 4 | blockLocation.getBlockY() & 15);
    }

    private Location sectionLocation;
    private boolean trustEdges;
    private Block[] blockData;

    public PacketOutMultiBlockChange() {
    }

    public PacketOutMultiBlockChange(Location sectionLocation, boolean trustEdges, Block[] blockData) {
        this.sectionLocation = sectionLocation;
        this.trustEdges = trustEdges;
        this.blockData = blockData;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeLong(LocationUtil.writeLocationAsPosition(this.sectionLocation));
        dataBuffer.writeBoolean(!this.trustEdges);

        dataBuffer.writeVarInt(this.blockData.length);
        for (Block block : this.blockData) {
            dataBuffer.writeVarLong(block.getType().getId() << 12 | PacketOutMultiBlockChange.toBlockLocation(block.getLocation()));
        }
    }

    @Override
    public void releaseData() {
        this.blockData = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutMultiBlockChange.class);
    }

    public Location getSectionLocation() {
        return this.sectionLocation;
    }

    public void setSectionLocation(Location sectionLocation) {
        this.sectionLocation = sectionLocation;
    }

    public boolean isTrustEdges() {
        return this.trustEdges;
    }

    public void setTrustEdges(boolean trustEdges) {
        this.trustEdges = trustEdges;
    }

    public Block[] getBlockData() {
        return this.blockData;
    }

    public void setBlockData(Block[] blockData) {
        this.blockData = blockData;
    }
}

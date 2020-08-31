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

import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyFromClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.github.phantompowered.server4je.protocol.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketInPlayerDigging implements Packet {

    private Type type;
    private Location blockLocation;
    private BlockFace facing;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        this.type = Type.values()[dataBuffer.readVarInt()];
        this.blockLocation = LocationUtil.locationFromLong(dataBuffer.readLong());

        int type = dataBuffer.readUnsignedByte();
        switch (type) {
            case 0:
                this.facing = BlockFace.DOWN;
                break;
            case 1:
                this.facing = BlockFace.UP;
                break;
            case 2:
                this.facing = BlockFace.NORTH;
                break;
            case 3:
                this.facing = BlockFace.SOUTH;
                break;
            case 4:
                this.facing = BlockFace.WEST;
                break;
            case 5:
                this.facing = BlockFace.EAST;
                break;
            default:
                ReportedException.throwWrapped("Illegal byte enum block face received: " + type);
        }
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        PacketOnlyFromClientException.throwNow();
    }

    @Override
    public void releaseData() {
        this.facing = null;
        this.blockLocation = null;
        this.type = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getClientPacketId(ProtocolState.PLAY, PacketInPlayerDigging.class);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Location getBlockLocation() {
        return blockLocation;
    }

    public void setBlockLocation(Location blockLocation) {
        this.blockLocation = blockLocation;
    }

    public BlockFace getFacing() {
        return facing;
    }

    public void setFacing(BlockFace facing) {
        this.facing = facing;
    }

    public enum Type {

        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND
    }
}

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
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBufOutputStream;
import net.kyori.nbt.CompoundTag;
import net.kyori.nbt.TagIO;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.OutputStream;

public class PacketOutBlockEntityData implements Packet {

    public static final int MIN = 1;
    public static final int MAX = 14;
    public static final ImmutableList<Integer> UNUSED_TYPES = ImmutableList.of(10);

    public static final int SET_MOB_SPAWNER_DATA = 1;
    public static final int SET_COMMAND_BLOCK_TEXT = 2;
    public static final int SET_BEACON_LEVEL = 3;
    public static final int SET_ROTATION_AND_SKIN_OF_HEAD = 4;
    public static final int DECLARE_CONDUIT = 5;
    public static final int SET_BANNER_COLOR_AND_PATTERN = 6;
    public static final int SET_STRUCTURE_TILE_ENTITY_DATA = 7;
    public static final int SET_END_GATEWAY_DESTINATION = 8;
    public static final int SET_SIGN_TEXT = 9;
    // 10 is unused
    public static final int DECLARE_BED = 11;
    public static final int SET_JIGSAW_BLOCK_DATA = 12;
    public static final int SET_ITEMS_IN_CAMPFIRE = 13;
    public static final int BEEHIVE_INFORMATION = 14;

    private Location position;
    private int action;
    private CompoundTag data;

    public PacketOutBlockEntityData() {
    }

    public PacketOutBlockEntityData(Location position, int action, CompoundTag data) {
        Preconditions.checkArgument(action >= MIN);
        Preconditions.checkArgument(action <= MAX);
        Preconditions.checkArgument(!UNUSED_TYPES.contains(action));

        this.position = position;
        this.action = action;
        this.data = data;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeLong(LocationUtil.locationToLong(this.position));
        dataBuffer.writeByte(this.action);
        try (OutputStream outputStream = new ByteBufOutputStream(dataBuffer)) {
            TagIO.writeOutputStream(this.data, outputStream);
        } catch (IOException exception) {
            ReportedException.throwWrapped(exception, "Unable to encode data in entity data packet");
        }
    }

    @Override
    public void releaseData() {
        this.position = null;
        this.data = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutBlockEntityData.class);
    }

    public Location getPosition() {
        return this.position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public CompoundTag getData() {
        return this.data;
    }

    public void setData(CompoundTag data) {
        this.data = data;
    }
}

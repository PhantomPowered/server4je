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

import com.github.phantompowered.server4je.common.math.MathHelper;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyFromClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.github.phantompowered.server4je.protocol.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.data.type.StructureBlock;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketInUpdateStructureBlock implements Packet {

    private Location position;
    private Action action;
    private StructureBlock.Mode mode;
    private String name;
    private Vector offset;
    private Vector size;
    private Mirror mirror;
    private StructureRotation rotation;
    private String metadata;
    private float integrity;
    private long seed;
    private byte flags;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        this.position = LocationUtil.locationFromLong(dataBuffer.readLong());
        this.action = Action.values()[dataBuffer.readVarInt()];
        this.mode = StructureBlock.Mode.values()[dataBuffer.readVarInt()];
        this.name = dataBuffer.readString();
        this.offset = new Vector(
            MathHelper.clamp(dataBuffer.readByte(), -48, 48),
            MathHelper.clamp(dataBuffer.readByte(), -48, 48),
            MathHelper.clamp(dataBuffer.readByte(), -48, 48)
        );
        this.size = new Vector(
            MathHelper.clamp(dataBuffer.readByte(), 0, 48),
            MathHelper.clamp(dataBuffer.readByte(), 0, 48),
            MathHelper.clamp(dataBuffer.readByte(), 0, 48)
        );
        this.mirror = Mirror.values()[dataBuffer.readVarInt()];
        this.rotation = StructureRotation.values()[dataBuffer.readVarInt()];
        this.metadata = dataBuffer.readString(12);
        this.integrity = MathHelper.clamp(dataBuffer.readFloat(), 0F, 1F);
        this.seed = dataBuffer.readVarLong();
        this.flags = dataBuffer.readByte();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        PacketOnlyFromClientException.throwNow();
    }

    @Override
    public void releaseData() {
        this.position = null;
        this.action = null;
        this.mode = null;
        this.name = null;
        this.offset = null;
        this.size = null;
        this.mirror = null;
        this.rotation = null;
        this.metadata = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getClientPacketId(ProtocolState.PLAY, PacketInUpdateStructureBlock.class);
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public StructureBlock.Mode getMode() {
        return mode;
    }

    public void setMode(StructureBlock.Mode mode) {
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector getOffset() {
        return offset;
    }

    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    public Vector getSize() {
        return size;
    }

    public void setSize(Vector size) {
        this.size = size;
    }

    public Mirror getMirror() {
        return mirror;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public StructureRotation getRotation() {
        return rotation;
    }

    public void setRotation(StructureRotation rotation) {
        this.rotation = rotation;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public float getIntegrity() {
        return integrity;
    }

    public void setIntegrity(float integrity) {
        this.integrity = integrity;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public boolean isIgnoreEntities() {
        return (this.flags & 1) != 0;
    }

    public boolean isShowAir() {
        return (this.flags & 2) != 0;
    }

    public boolean isShowBoundingBox() {
        return (this.flags & 4) != 0;
    }

    public enum Action {

        UPDATE_DATA,
        SAVE_STRUCTURE,
        LOAD_STRUCTURE,
        DETECT_SIZE
    }
}

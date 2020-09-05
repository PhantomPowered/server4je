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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.map.MapCursor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutMapData implements Packet {

    private int mapId;
    private byte scale;
    private boolean trackingPosition;
    private boolean locked;
    private MapCursor[] cursors;
    private int columns;
    // optional
    private byte rows;
    private byte x;
    private byte z;
    private int length;
    private byte[] data;

    public PacketOutMapData() {
    }

    public PacketOutMapData(int mapId, byte scale, boolean trackingPosition, boolean locked, MapCursor[] cursors, byte[] renderBuffer,
                            int columns, byte rows, byte x, byte z, int length) {
        this.mapId = mapId;
        this.scale = scale;
        this.trackingPosition = trackingPosition;
        this.locked = locked;
        this.cursors = cursors;
        this.columns = columns;
        if (columns > 0) {
            this.rows = rows;
            this.x = x;
            this.z = z;
            this.length = length;
            this.data = new byte[columns * rows];

            for (int i = 0; i < columns; ++i) {
                for (int u = 0; u < rows; ++u) {
                    this.data[i + u * columns] = renderBuffer[x + i + (z + u) * 128];
                }
            }
        }
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.mapId);
        dataBuffer.writeByte(this.scale);
        dataBuffer.writeBoolean(this.trackingPosition);
        dataBuffer.writeBoolean(this.locked);

        dataBuffer.writeVarInt(this.cursors.length);
        for (MapCursor cursor : this.cursors) {
            dataBuffer.writeVarInt(cursor.getType().ordinal());
            dataBuffer.writeByte(cursor.getX());
            dataBuffer.writeByte(cursor.getY());
            dataBuffer.writeByte(cursor.getDirection());
            dataBuffer.writeBoolean(cursor.getCaption() != null);
            if (cursor.getCaption() != null) {
                dataBuffer.writeString(ComponentSerializer.toString(TextComponent.fromLegacyText(cursor.getCaption())));
            }
        }

        dataBuffer.writeByte(this.columns);
        if (this.columns > 0) {
            dataBuffer.writeByte(this.rows);
            dataBuffer.writeByte(this.x);
            dataBuffer.writeByte(this.z);
            dataBuffer.writeVarInt(this.length);
            dataBuffer.writeByteArray(this.data);
        }
    }

    @Override
    public void releaseData() {
        this.cursors = null;
        this.data = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutMapData.class);
    }

    public int getMapId() {
        return this.mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public byte getScale() {
        return this.scale;
    }

    public void setScale(byte scale) {
        this.scale = scale;
    }

    public boolean isTrackingPosition() {
        return this.trackingPosition;
    }

    public void setTrackingPosition(boolean trackingPosition) {
        this.trackingPosition = trackingPosition;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public MapCursor[] getCursors() {
        return this.cursors;
    }

    public void setCursors(MapCursor[] cursors) {
        this.cursors = cursors;
    }

    public int getColumns() {
        return this.columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public byte getRows() {
        return this.rows;
    }

    public void setRows(byte rows) {
        this.rows = rows;
    }

    public byte getX() {
        return this.x;
    }

    public void setX(byte x) {
        this.x = x;
    }

    public byte getZ() {
        return this.z;
    }

    public void setZ(byte z) {
        this.z = z;
    }

    public int getLength() {
        return this.length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

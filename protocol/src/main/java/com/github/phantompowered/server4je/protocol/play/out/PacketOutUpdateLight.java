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

import java.util.List;

public class PacketOutUpdateLight implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean trustEdges;
    private int skyLightMask;
    private int blockLightMask;
    private int emptySkyLightMask;
    private int emptyBlockLightMask;
    private List<byte[]> skyLightArrays;
    private List<byte[]> blockLightArrays;

    public PacketOutUpdateLight() {
    }

    public PacketOutUpdateLight(int chunkX, int chunkZ, boolean trustEdges, int skyLightMask, int blockLightMask, int emptySkyLightMask,
                                int emptyBlockLightMask, List<byte[]> skyLightArrays, List<byte[]> blockLightArrays) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.trustEdges = trustEdges;
        this.skyLightMask = skyLightMask;
        this.blockLightMask = blockLightMask;
        this.emptySkyLightMask = emptySkyLightMask;
        this.emptyBlockLightMask = emptyBlockLightMask;
        this.skyLightArrays = skyLightArrays;
        this.blockLightArrays = blockLightArrays;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.chunkX);
        dataBuffer.writeVarInt(this.chunkZ);
        dataBuffer.writeBoolean(this.trustEdges);
        dataBuffer.writeVarInt(this.skyLightMask);
        dataBuffer.writeVarInt(this.blockLightMask);
        dataBuffer.writeVarInt(this.emptySkyLightMask);
        dataBuffer.writeVarInt(this.emptyBlockLightMask);

        dataBuffer.writeVarInt(this.skyLightArrays.size());
        for (byte[] skyLightArray : this.skyLightArrays) {
            dataBuffer.writeByteArray(skyLightArray);
        }

        dataBuffer.writeVarInt(this.blockLightArrays.size());
        for (byte[] blockLightArray : this.blockLightArrays) {
            dataBuffer.writeByteArray(blockLightArray);
        }
    }

    @Override
    public void releaseData() {
        this.skyLightArrays = null;
        this.blockLightArrays = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutUpdateLight.class);
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }

    public boolean isTrustEdges() {
        return this.trustEdges;
    }

    public void setTrustEdges(boolean trustEdges) {
        this.trustEdges = trustEdges;
    }

    public int getSkyLightMask() {
        return this.skyLightMask;
    }

    public void setSkyLightMask(int skyLightMask) {
        this.skyLightMask = skyLightMask;
    }

    public int getBlockLightMask() {
        return this.blockLightMask;
    }

    public void setBlockLightMask(int blockLightMask) {
        this.blockLightMask = blockLightMask;
    }

    public int getEmptySkyLightMask() {
        return this.emptySkyLightMask;
    }

    public void setEmptySkyLightMask(int emptySkyLightMask) {
        this.emptySkyLightMask = emptySkyLightMask;
    }

    public int getEmptyBlockLightMask() {
        return this.emptyBlockLightMask;
    }

    public void setEmptyBlockLightMask(int emptyBlockLightMask) {
        this.emptyBlockLightMask = emptyBlockLightMask;
    }

    public List<byte[]> getSkyLightArrays() {
        return this.skyLightArrays;
    }

    public void setSkyLightArrays(List<byte[]> skyLightArrays) {
        this.skyLightArrays = skyLightArrays;
    }

    public List<byte[]> getBlockLightArrays() {
        return this.blockLightArrays;
    }

    public void setBlockLightArrays(List<byte[]> blockLightArrays) {
        this.blockLightArrays = blockLightArrays;
    }
}

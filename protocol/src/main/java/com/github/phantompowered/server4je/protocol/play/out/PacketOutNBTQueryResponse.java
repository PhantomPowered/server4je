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
import io.netty.buffer.ByteBufOutputStream;
import net.kyori.nbt.CompoundTag;
import net.kyori.nbt.TagIO;
import net.kyori.nbt.TagType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.OutputStream;

public class PacketOutNBTQueryResponse implements Packet {

    private int transactionId;
    private @Nullable CompoundTag compoundTag;

    public PacketOutNBTQueryResponse() {
    }

    public PacketOutNBTQueryResponse(int transactionId) {
        this(transactionId, null);
    }

    public PacketOutNBTQueryResponse(int transactionId, @Nullable CompoundTag compoundTag) {
        this.transactionId = transactionId;
        this.compoundTag = compoundTag;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.transactionId);
        if (this.compoundTag == null) {
            dataBuffer.writeByte(TagType.END.id());
            return;
        }

        try (OutputStream outputStream = new ByteBufOutputStream(dataBuffer)) {
            TagIO.writeOutputStream(this.compoundTag, outputStream);
        } catch (IOException exception) {
            ReportedException.throwWrapped(exception, "Unable to write compound tag into data buffer");
        }
    }

    @Override
    public void releaseData() {
        this.compoundTag = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutNBTQueryResponse.class);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public CompoundTag getCompoundTag() {
        return this.compoundTag;
    }

    public void setCompoundTag(CompoundTag compoundTag) {
        this.compoundTag = compoundTag;
    }
}

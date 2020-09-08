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

import com.destroystokyo.paper.Namespaced;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;

public class PacketOutTags implements Packet {

    private Collection<ProtocolTagWrapper> blockTags;
    private Collection<ProtocolTagWrapper> itemTags;
    private Collection<ProtocolTagWrapper> fluidTags;
    private Collection<ProtocolTagWrapper> entityTags;

    public PacketOutTags() {
    }

    public PacketOutTags(Collection<ProtocolTagWrapper> blockTags, Collection<ProtocolTagWrapper> itemTags,
                         Collection<ProtocolTagWrapper> fluidTags, Collection<ProtocolTagWrapper> entityTags) {
        this.blockTags = blockTags;
        this.itemTags = itemTags;
        this.fluidTags = fluidTags;
        this.entityTags = entityTags;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        this.writeTags(this.blockTags, dataBuffer);
        this.writeTags(this.itemTags, dataBuffer);
        this.writeTags(this.fluidTags, dataBuffer);
        this.writeTags(this.entityTags, dataBuffer);
    }

    @Override
    public void releaseData() {
        this.blockTags = null;
        this.itemTags = null;
        this.fluidTags = null;
        this.entityTags = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutTags.class);
    }

    private void writeTags(@NotNull Collection<ProtocolTagWrapper> tags, @NotNull DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(tags.size());
        for (ProtocolTagWrapper tag : tags) {
            dataBuffer.writeNamespaced(tag.key);
            dataBuffer.writeVarInt(tag.values.size());
            for (Integer value : tag.values) {
                dataBuffer.writeVarInt(value);
            }
        }
    }

    public Collection<ProtocolTagWrapper> getBlockTags() {
        return this.blockTags;
    }

    public void setBlockTags(Collection<ProtocolTagWrapper> blockTags) {
        this.blockTags = blockTags;
    }

    public Collection<ProtocolTagWrapper> getItemTags() {
        return this.itemTags;
    }

    public void setItemTags(Collection<ProtocolTagWrapper> itemTags) {
        this.itemTags = itemTags;
    }

    public Collection<ProtocolTagWrapper> getFluidTags() {
        return this.fluidTags;
    }

    public void setFluidTags(Collection<ProtocolTagWrapper> fluidTags) {
        this.fluidTags = fluidTags;
    }

    public Collection<ProtocolTagWrapper> getEntityTags() {
        return this.entityTags;
    }

    public void setEntityTags(Collection<ProtocolTagWrapper> entityTags) {
        this.entityTags = entityTags;
    }

    public static class ProtocolTagWrapper {

        private final Namespaced key;
        private final Collection<Integer> values;

        public ProtocolTagWrapper(Namespaced key, Collection<Integer> values) {
            this.key = key;
            this.values = values;
        }
    }
}

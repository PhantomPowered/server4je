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
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.scoreboard.RenderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class PacketOutScoreboardObjective implements Packet {

    private String name;
    private Mode mode;
    private @Nullable TextComponent value;
    private @Nullable RenderType renderType;

    public PacketOutScoreboardObjective() {
    }

    public PacketOutScoreboardObjective(String name, Mode mode) {
        this.name = name;
        this.mode = mode;
    }

    public PacketOutScoreboardObjective(String name, Mode mode, @Nullable TextComponent value, @Nullable RenderType renderType) {
        this.name = name;
        this.mode = mode;
        this.value = value;
        this.renderType = renderType;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeString(this.name, 16);
        dataBuffer.writeByte(this.mode.ordinal());
        if (this.mode != Mode.REMOVE) {
            dataBuffer.writeString(ComponentSerializer.toString(Preconditions.checkNotNull(this.value)));
            dataBuffer.writeVarInt(Preconditions.checkNotNull(this.renderType).ordinal());
        }
    }

    @Override
    public void releaseData() {
        this.name = null;
        this.mode = null;
        this.value = null;
        this.renderType = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutScoreboardObjective.class);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public TextComponent getValue() {
        return this.value;
    }

    public void setValue(TextComponent value) {
        this.value = value;
    }

    public RenderType getRenderType() {
        return this.renderType;
    }

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }

    public enum Mode {

        CREATE,
        REMOVE,
        UPDATE
    }
}

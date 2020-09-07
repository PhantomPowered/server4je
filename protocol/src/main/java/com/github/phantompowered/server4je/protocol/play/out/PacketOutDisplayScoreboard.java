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
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutDisplayScoreboard implements Packet {

    private static final String SIDEBAR_PLACEHOLDER = "sidebar.team.";
    private static final int SIDEBAR_PLACEHOLDER_LENGTH = SIDEBAR_PLACEHOLDER.length();

    public static int getSlotForName(@NotNull String slot) {
        if (slot.equalsIgnoreCase("list")) {
            return 0;
        }

        if (slot.equalsIgnoreCase("sidebar")) {
            return 1;
        }

        if (slot.equalsIgnoreCase("belowName")) {
            return 2;
        }

        if (slot.regionMatches(0, PacketOutDisplayScoreboard.SIDEBAR_PLACEHOLDER, 0, PacketOutDisplayScoreboard.SIDEBAR_PLACEHOLDER_LENGTH)) {
            ChatColor chatColor = ChatColor.getByChar(slot.substring(PacketOutDisplayScoreboard.SIDEBAR_PLACEHOLDER_LENGTH));
            if (chatColor != null && chatColor.ordinal() <= 15) {
                return chatColor.ordinal() + 3;
            }
        }

        return -1;
    }

    private int position;
    private String scoreName;

    public PacketOutDisplayScoreboard() {
    }

    public PacketOutDisplayScoreboard(@NotNull DisplaySlot displaySlot, String scoreName) {
        this(displaySlot.ordinal(), scoreName);
    }

    public PacketOutDisplayScoreboard(int position, String scoreName) {
        this.position = position;
        this.scoreName = scoreName;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeByte(this.position);
        dataBuffer.writeString(this.scoreName, 16);
    }

    @Override
    public void releaseData() {
        this.scoreName = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutDisplayScoreboard.class);
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getScoreName() {
        return this.scoreName;
    }

    public void setScoreName(String scoreName) {
        this.scoreName = scoreName;
    }
}

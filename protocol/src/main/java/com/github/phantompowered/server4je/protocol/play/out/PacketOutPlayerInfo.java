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

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketOutPlayerInfo implements Packet {

    private Action action;
    private InfoData[] infoData;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.action.ordinal());
        dataBuffer.writeVarInt(this.infoData.length);
        for (InfoData info : this.infoData) {
            info.serialize(dataBuffer);
        }
    }

    @Override
    public void releaseData() {
        this.action = null;
        this.infoData = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutPlayerInfo.class);
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public InfoData[] getInfoData() {
        return this.infoData;
    }

    public void setInfoData(InfoData[] infoData) {
        this.infoData = infoData;
    }

    @FunctionalInterface
    public interface InfoData {

        void serialize(@NotNull DataBuffer dataBuffer);
    }

    public static class InfoDataBuilder {

        @NotNull
        @Contract(value = "_ -> new", pure = true)
        public static InfoDataBuilder of(@NotNull UUID playerUniqueId) {
            return new InfoDataBuilder(playerUniqueId);
        }

        private final UUID playerUniqueId;
        private final List<Object> data = new ArrayList<>();

        private InfoDataBuilder(UUID playerUniqueId) {
            this.playerUniqueId = playerUniqueId;
        }

        @NotNull
        public InfoDataBuilder data(@NotNull Object data) {
            this.data.add(data);
            return this;
        }

        @NotNull
        public InfoData build() {
            return dataBuffer -> {
                dataBuffer.writeUniqueId(this.playerUniqueId);
                for (Object data : this.data) {
                    if (data instanceof String) {
                        dataBuffer.writeString((String) data, 16);
                    } else if (data instanceof ProfileProperty[]) {
                        ProfileProperty[] profileProperties = (ProfileProperty[]) data;
                        dataBuffer.writeVarInt(profileProperties.length);
                        for (ProfileProperty profileProperty : profileProperties) {
                            dataBuffer.writeString(profileProperty.getName());
                            dataBuffer.writeString(profileProperty.getValue());
                            dataBuffer.writeBoolean(profileProperty.isSigned());
                            if (profileProperty.getSignature() != null) {
                                dataBuffer.writeString(profileProperty.getSignature());
                            }
                        }
                    } else if (data instanceof GameMode) {
                        dataBuffer.writeVarInt(((GameMode) data).ordinal());
                    } else if (data instanceof Integer) {
                        dataBuffer.writeVarInt((Integer) data);
                    } else if (data instanceof BaseComponent[]) {
                        dataBuffer.writeString(ComponentSerializer.toString((BaseComponent[]) data));
                    } else if (data instanceof Boolean) {
                        dataBuffer.writeBoolean((Boolean) data);
                    } else {
                        ReportedException.throwWrapped("Invalid data object " + data.getClass().getName());
                    }
                }
            };
        }
    }

    public enum Action {

        ADD_PLAYER,
        UPDATE_GAME_MODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER
    }
}

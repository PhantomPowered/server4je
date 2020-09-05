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
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class PacketOutJoinGame implements Packet {

    private int entityId;
    private boolean hardcore;
    private GameMode gameMode;
    private GameMode previousGameMode;
    private Collection<Namespaced> worldNames;
    private CompoundTag dimensionCodec;
    private CompoundTag dimension;
    private Namespaced worldName;
    private long seed;
    private int maxPlayers;
    private @Range(from = 2, to = 32) int viewDistance;
    private boolean reducedDebugInfo;
    private boolean enableRespawnScreen;
    private boolean debug;
    private boolean flat;

    public PacketOutJoinGame() {
    }

    public PacketOutJoinGame(int entityId, boolean hardcore, GameMode gameMode, GameMode previousGameMode, Collection<Namespaced> worldNames,
                             CompoundTag dimensionCodec, CompoundTag dimension, Namespaced worldName, long seed, int maxPlayers,
                             @Range(from = 2, to = 32) int viewDistance, boolean reducedDebugInfo, boolean enableRespawnScreen, boolean debug, boolean flat) {
        this.entityId = entityId;
        this.hardcore = hardcore;
        this.gameMode = gameMode;
        this.previousGameMode = previousGameMode;
        this.worldNames = worldNames;
        this.dimensionCodec = dimensionCodec;
        this.dimension = dimension;
        this.worldName = worldName;
        this.seed = seed;
        this.maxPlayers = maxPlayers;
        this.viewDistance = viewDistance;
        this.reducedDebugInfo = reducedDebugInfo;
        this.enableRespawnScreen = enableRespawnScreen;
        this.debug = debug;
        this.flat = flat;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeInt(this.entityId);
        dataBuffer.writeBoolean(this.hardcore);
        dataBuffer.writeByte(this.gameMode.ordinal());
        dataBuffer.writeByte(this.previousGameMode.ordinal());

        dataBuffer.writeVarInt(this.worldNames.size());
        for (Namespaced name : this.worldNames) {
            dataBuffer.writeNamespaced(name);
        }

        try (OutputStream outputStream = new ByteBufOutputStream(dataBuffer)) {
            TagIO.writeOutputStream(this.dimensionCodec, outputStream);
            TagIO.writeOutputStream(this.dimension, outputStream);
        } catch (IOException exception) {
            ReportedException.throwWrapped(exception, "Unable to write nbt compound into JoinGame packet");
        }

        dataBuffer.writeNamespaced(this.worldName);
        dataBuffer.writeLong(this.seed);
        dataBuffer.writeVarInt(this.maxPlayers);
        dataBuffer.writeVarInt(this.viewDistance);
        dataBuffer.writeBoolean(this.reducedDebugInfo);
        dataBuffer.writeBoolean(this.enableRespawnScreen);
        dataBuffer.writeBoolean(this.debug);
        dataBuffer.writeBoolean(this.flat);
    }

    @Override
    public void releaseData() {
        this.gameMode = null;
        this.previousGameMode = null;
        this.worldNames = null;
        this.dimensionCodec = null;
        this.dimension = null;
        this.worldName = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutJoinGame.class);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getPreviousGameMode() {
        return this.previousGameMode;
    }

    public void setPreviousGameMode(GameMode previousGameMode) {
        this.previousGameMode = previousGameMode;
    }

    public Collection<Namespaced> getWorldNames() {
        return this.worldNames;
    }

    public void setWorldNames(Collection<Namespaced> worldNames) {
        this.worldNames = worldNames;
    }

    public CompoundTag getDimensionCodec() {
        return this.dimensionCodec;
    }

    public void setDimensionCodec(CompoundTag dimensionCodec) {
        this.dimensionCodec = dimensionCodec;
    }

    public CompoundTag getDimension() {
        return this.dimension;
    }

    public void setDimension(CompoundTag dimension) {
        this.dimension = dimension;
    }

    public Namespaced getWorldName() {
        return this.worldName;
    }

    public void setWorldName(Namespaced worldName) {
        this.worldName = worldName;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public boolean isReducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        this.reducedDebugInfo = reducedDebugInfo;
    }

    public boolean isEnableRespawnScreen() {
        return this.enableRespawnScreen;
    }

    public void setEnableRespawnScreen(boolean enableRespawnScreen) {
        this.enableRespawnScreen = enableRespawnScreen;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isFlat() {
        return this.flat;
    }

    public void setFlat(boolean flat) {
        this.flat = flat;
    }
}

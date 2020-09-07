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

public class PacketOutRespawn implements Packet {

    private CompoundTag dimension;
    private Namespaced worldName;
    private long seed;
    private GameMode gameMode;
    private GameMode previousGameMode;
    private boolean debug;
    private boolean flat;
    private boolean copyMetadata;

    public PacketOutRespawn() {
    }

    public PacketOutRespawn(CompoundTag dimension, Namespaced worldName, long seed,
                            GameMode gameMode, GameMode previousGameMode, boolean debug, boolean flat, boolean copyMetadata) {
        this.dimension = dimension;
        this.worldName = worldName;
        this.seed = seed;
        this.gameMode = gameMode;
        this.previousGameMode = previousGameMode;
        this.debug = debug;
        this.flat = flat;
        this.copyMetadata = copyMetadata;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        try (OutputStream outputStream = new ByteBufOutputStream(dataBuffer)) {
            TagIO.writeOutputStream(this.dimension, outputStream);
        } catch (IOException exception) {
            ReportedException.throwWrapped(exception, "Unable to encode dimension data in respawn packet");
        }

        dataBuffer.writeNamespaced(this.worldName);
        dataBuffer.writeLong(this.seed);
        dataBuffer.writeByte(this.gameMode.ordinal());
        dataBuffer.writeByte(this.previousGameMode.ordinal());
        dataBuffer.writeBoolean(this.debug);
        dataBuffer.writeBoolean(this.flat);
        dataBuffer.writeBoolean(this.copyMetadata);
    }

    @Override
    public void releaseData() {
        this.dimension = null;
        this.worldName = null;
        this.gameMode = null;
        this.previousGameMode = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutRespawn.class);
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

    public boolean isCopyMetadata() {
        return this.copyMetadata;
    }

    public void setCopyMetadata(boolean copyMetadata) {
        this.copyMetadata = copyMetadata;
    }
}

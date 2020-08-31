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

import com.destroystokyo.paper.Namespaced;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.github.phantompowered.server4je.protocol.utils.LocationUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketInUpdateJigsawBlock implements Packet {

    private Location position;
    private Namespaced name;
    private Namespaced target;
    private Namespaced pool;
    private String finalState;
    private Type type;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        this.position = LocationUtil.locationFromLong(dataBuffer.readLong());
        this.name = dataBuffer.readNamespaced();
        this.target = dataBuffer.readNamespaced();
        this.pool = dataBuffer.readNamespaced();
        this.finalState = dataBuffer.readString();
        this.type = Type.getByNameSafe(dataBuffer.readString(Type.MAX_NAME_LENGTH));
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {

    }

    @Override
    public void releaseData() {
        this.position = null;
        this.name = null;
        this.target = null;
        this.pool = null;
        this.finalState = null;
        this.type = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getClientPacketId(ProtocolState.PLAY, PacketInUpdateJigsawBlock.class);
    }

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public Namespaced getName() {
        return name;
    }

    public void setName(Namespaced name) {
        this.name = name;
    }

    public Namespaced getTarget() {
        return target;
    }

    public void setTarget(Namespaced target) {
        this.target = target;
    }

    public Namespaced getPool() {
        return pool;
    }

    public void setPool(Namespaced pool) {
        this.pool = pool;
    }

    public String getFinalState() {
        return finalState;
    }

    public void setFinalState(String finalState) {
        this.finalState = finalState;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {

        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private static final Map<String, Type> BY_NAME = new ConcurrentHashMap<>();
        @ApiStatus.Internal // may change from release to release
        private static final short MAX_NAME_LENGTH = 8;

        private final String name;

        Type(String name) {
            this.name = name;
        }

        static {
            for (Type value : Type.values()) {
                BY_NAME.put(value.name, value);
            }
        }

        @NotNull
        public static Type getByNameSafe(@NotNull String name) {
            return BY_NAME.getOrDefault(name, Type.ALIGNED);
        }

        @Nullable
        public static Type getByName(@NotNull String name) {
            return BY_NAME.get(name);
        }
    }
}

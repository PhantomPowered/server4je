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
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PacketOutFacePlayer implements Packet {

    private BodyFacing feetOrEyes;
    private double targetX;
    private double targetY;
    private double targetZ;
    // optional
    private Integer entityId;
    private BodyFacing entityFeetOrEyes;

    public PacketOutFacePlayer(BodyFacing feetOrEyes, double targetX, double targetY, double targetZ) {
        this.feetOrEyes = feetOrEyes;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
    }

    public PacketOutFacePlayer(BodyFacing feetOrEyes, Entity entity, BodyFacing entityFeetOrEyes) {
        this.feetOrEyes = feetOrEyes;
        this.entityId = entity.getEntityId();
        this.entityFeetOrEyes = entityFeetOrEyes;

        Vector target = entityFeetOrEyes.entityVectorCalculator.apply(entity.getLocation().toVector(), entity);
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.targetZ = target.getZ();
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.feetOrEyes.ordinal());
        dataBuffer.writeDouble(this.targetX);
        dataBuffer.writeDouble(this.targetY);
        dataBuffer.writeDouble(this.targetZ);
        dataBuffer.writeBoolean(this.entityId != null);
        if (this.entityId != null) {
            dataBuffer.writeVarInt(this.entityId);
            dataBuffer.writeVarInt(this.entityFeetOrEyes.ordinal());
        }
    }

    @Override
    public void releaseData() {
        this.feetOrEyes = null;
        this.entityFeetOrEyes = null;
        this.entityId = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutFacePlayer.class);
    }

    public BodyFacing getFeetOrEyes() {
        return this.feetOrEyes;
    }

    public void setFeetOrEyes(BodyFacing feetOrEyes) {
        this.feetOrEyes = feetOrEyes;
    }

    public double getTargetX() {
        return this.targetX;
    }

    public void setTargetX(double targetX) {
        this.targetX = targetX;
    }

    public double getTargetY() {
        return this.targetY;
    }

    public void setTargetY(double targetY) {
        this.targetY = targetY;
    }

    public double getTargetZ() {
        return this.targetZ;
    }

    public void setTargetZ(double targetZ) {
        this.targetZ = targetZ;
    }

    public Integer getEntityId() {
        return this.entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public BodyFacing getEntityFeetOrEyes() {
        return this.entityFeetOrEyes;
    }

    public void setEntityFeetOrEyes(BodyFacing entityFeetOrEyes) {
        this.entityFeetOrEyes = entityFeetOrEyes;
    }

    public enum BodyFacing {

        FEET("feet", (vector, ignored) -> vector),
        EYES("eyes", (vector, entity) -> new Vector(vector.getX(), vector.getY() + entity.getHeight(), vector.getZ()));

        private static final Map<String, BodyFacing> BY_NAME = new ConcurrentHashMap<>();

        static {
            for (BodyFacing value : BodyFacing.values()) {
                BY_NAME.put(value.name, value);
            }
        }

        @Nullable
        public static BodyFacing getByName(@NotNull String name) {
            return BodyFacing.BY_NAME.get(name);
        }

        private final String name;
        private final BiFunction<Vector, Entity, Vector> entityVectorCalculator;

        BodyFacing(String name, BiFunction<Vector, Entity, Vector> entityVectorCalculator) {
            this.name = name;
            this.entityVectorCalculator = entityVectorCalculator;
        }
    }
}

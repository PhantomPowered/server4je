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

import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyFromClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketInInteractEntity implements Packet {

    private int entityId;
    private UseAction action;
    private Vector vector;
    private PacketInArmAnimation.Hand hand;
    private boolean sneaking;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        this.entityId = dataBuffer.readVarInt();
        this.action = UseAction.values()[dataBuffer.readVarInt()];
        if (this.action == UseAction.INTERACT_AT) {
            this.vector = new Vector(dataBuffer.readFloat(), dataBuffer.readFloat(), dataBuffer.readFloat());
        }

        if (this.action.isInteract()) {
            this.hand = PacketInArmAnimation.Hand.values()[dataBuffer.readVarInt()];
        }

        this.sneaking = dataBuffer.readBoolean();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        PacketOnlyFromClientException.throwNow();
    }

    @Override
    public void releaseData() {
        this.action = null;
        this.vector = null;
        this.hand = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getClientPacketId(ProtocolState.PLAY, PacketInInteractEntity.class);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public UseAction getAction() {
        return this.action;
    }

    public void setAction(UseAction action) {
        this.action = action;
    }

    public Vector getVector() {
        return this.vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public PacketInArmAnimation.Hand getHand() {
        return this.hand;
    }

    public void setHand(PacketInArmAnimation.Hand hand) {
        this.hand = hand;
    }

    public boolean isSneaking() {
        return this.sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public enum UseAction {

        INTERACT,
        ATTACK,
        INTERACT_AT;

        public boolean isInteract() {
            return this == UseAction.INTERACT || this == UseAction.INTERACT_AT;
        }
    }
}

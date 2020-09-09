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

import com.github.phantompowered.server4je.common.enums.EnumUtil;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;

public class PacketOutEntityProperties implements Packet {

    private int entityId;
    private Collection<AttributeInstance> attributeInstances;

    public PacketOutEntityProperties() {
    }

    public PacketOutEntityProperties(@NotNull LivingEntity entity) {
        this.entityId = entity.getEntityId();
        this.attributeInstances = new ArrayList<>();
        for (Attribute attribute : EnumUtil.getEnumEntries(Attribute.class)) {
            AttributeInstance attributeInstance = entity.getAttribute(attribute);
            if (attributeInstance != null) {
                this.attributeInstances.add(attributeInstance);
            }
        }
    }

    public PacketOutEntityProperties(int entityId, Collection<AttributeInstance> attributeInstances) {
        this.entityId = entityId;
        this.attributeInstances = attributeInstances;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.entityId);
        dataBuffer.writeInt(this.attributeInstances.size());
        for (AttributeInstance attributeInstance : this.attributeInstances) {
            dataBuffer.writeNamespaced(attributeInstance.getAttribute().getKey());
            dataBuffer.writeDouble(attributeInstance.getBaseValue());
            dataBuffer.writeVarInt(attributeInstance.getModifiers().size());
            for (AttributeModifier modifier : attributeInstance.getModifiers()) {
                dataBuffer.writeUniqueId(modifier.getUniqueId());
                dataBuffer.writeDouble(modifier.getAmount());
                dataBuffer.writeByte(modifier.getOperation().ordinal());
            }
        }
    }

    @Override
    public void releaseData() {
        this.attributeInstances = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutEntityProperties.class);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public Collection<AttributeInstance> getAttributeInstances() {
        return this.attributeInstances;
    }

    public void setAttributeInstances(Collection<AttributeInstance> attributeInstances) {
        this.attributeInstances = attributeInstances;
    }
}

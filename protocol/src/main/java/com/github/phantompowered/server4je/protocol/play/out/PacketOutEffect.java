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

import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.defaults.PrimitivePacket;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.github.phantompowered.server4je.protocol.utils.LocationUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutEffect extends PrimitivePacket {

    // sound
    public static final int DISPENSER_DISPENSES = 1000;
    public static final int DISPENSER_FAIL_DISPENSE = 1001;
    public static final int DISPENSER_SHOOTS = 1002;
    public static final int ENDER_EYE_LAUNCHED = 1003;
    public static final int FIREWORK_SHOT = 1004;
    public static final int IRON_DOOR_OPENED = 1005;
    public static final int WOODEN_DOOR_OPENED = 1006;
    public static final int WOODEN_TRAPDOOR_OPENED = 1007;
    public static final int FENCE_GATE_OPENED = 1008;
    public static final int FIRE_EXTINGUISHED = 1009;
    public static final int PLAY_RECORD = 1010;
    public static final int IRON_DOOR_CLOSED = 1011;
    public static final int WOODEN_DOOR_CLOSED = 1012;
    public static final int WOODEN_TRAPDOOR_CLOSED = 1013;
    public static final int FENCE_GATE_CLOSED = 1014;
    public static final int GHAST_WARNS = 1015;
    public static final int GHAST_SHOOTS = 1016;
    public static final int ENDER_DRAGON_SHOOTS = 1017;
    public static final int BLAZE_SHOOTS = 1018;
    public static final int ZOMBIE_ATTACKS_WOODEN_DOOR = 1019;
    public static final int ZOMBIE_ATTACKS_IRON_DOOR = 1020;
    public static final int ZOMBIE_BREAKS_WOOD_DOOR = 1021;
    public static final int WITHER_BREAKS_BLOCK = 1022;
    public static final int WITHER_SPAWNED = 1023;
    public static final int WITHER_SHOOTS = 1024;
    public static final int BAT_TAKES_OFF = 1025;
    public static final int ZOMBIE_INFECTS = 1026;
    public static final int ZOMBIE_VILLAGER_CONVERTED = 1027;
    public static final int ENDER_DRAGON_DEATH = 1028;
    public static final int ANVIL_DESTROYED = 1029;
    public static final int ANVIL_USED = 1030;
    public static final int ANVIL_LANDED = 1031;
    public static final int PORTAL_TRAVEL = 1032;
    public static final int CHORUS_FLOWER_GROWN = 1033;
    public static final int CHORUS_FLOWER_DIED = 1034;
    public static final int BREWING_STAND_BREWED = 1035;
    public static final int IRON_TRAPDOOR_OPENED = 1036;
    public static final int IRON_TRAPDOOR_CLOSED = 1037;
    public static final int END_PORTAL_CREATED = 1038;
    public static final int PHANTOM_BITES = 1039;
    public static final int ZOMBIE_CONVERTS_TO_DROWNED = 1040;
    public static final int HUSK_CONVERTS_ZOMBIE_BY_DROWNING = 1041;
    public static final int GRINDSTONE_USED = 1042;
    public static final int BOOK_PAGE_TURNED = 1043;
    // Effect
    public static final int COMPOSTER_COMPOSTS = 1500;
    public static final int LAVA_CONVERTS_BLOCK = 1501;
    public static final int REDSTONE_TORCH_BURNS_OUT = 1502;
    public static final int ENDER_EYE_PLACED = 1503;
    // Misc effects 1
    public static final int SPAWN_10_SMOKE_PARTICLES = 2000;
    public static final int BLOCK_BREAK_AND_SOUND = 2001;
    public static final int SPLASH_POTION_PARTICLE_AND_SOUND = 2002;
    public static final int EYE_OF_ENDER_BREAK_AND_SOUND = 2003;
    public static final int MOB_SPAWNER_PARTICLES_AND_FLAMES = 2004;
    public static final int BONE_MEAL_PARTICLES = 2005;
    public static final int DRAGON_BREATH = 2006;
    public static final int INSTANT_SPLASH_POTION = 2007;
    public static final int ENDER_DRAGON_DESTROYS_BLOCK = 2008;
    public static final int WET_SPONGE_VAPORIZES_IN_NETHER = 2009;
    // Misc effects 2
    public static final int END_GATEWAY_SPAWN = 3000;
    public static final int ENDER_DRAGON_GROWL = 3001;

    private int effectId;
    private Location position;
    private int data;
    private boolean disableRelativeVolume;

    public PacketOutEffect() {
    }

    public PacketOutEffect(int effectId, Location position) {
        this.effectId = effectId;
        this.position = position;
    }

    public PacketOutEffect(int effectId, Location position, int data) {
        this.effectId = effectId;
        this.position = position;
        this.data = data;
    }

    public PacketOutEffect(int effectId, Location position, int data, boolean disableRelativeVolume) {
        this.effectId = effectId;
        this.position = position;
        this.data = data;
        this.disableRelativeVolume = disableRelativeVolume;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeInt(this.effectId);
        dataBuffer.writeLong(LocationUtil.locationToLong(this.position));
        dataBuffer.writeInt(this.data);
        dataBuffer.writeBoolean(this.disableRelativeVolume);
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutEffect.class);
    }

    public int getEffectId() {
        return this.effectId;
    }

    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }

    public Location getPosition() {
        return this.position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public int getData() {
        return this.data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public boolean isDisableRelativeVolume() {
        return this.disableRelativeVolume;
    }

    public void setDisableRelativeVolume(boolean disableRelativeVolume) {
        this.disableRelativeVolume = disableRelativeVolume;
    }
}

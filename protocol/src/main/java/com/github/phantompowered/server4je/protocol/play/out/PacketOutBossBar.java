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

import com.github.phantompowered.server4je.api.boss.PhantomBossBar;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public class PacketOutBossBar implements Packet {

    // all
    private UUID uniqueId;
    private Action action;

    // add, update title
    private TextComponent title;

    // add, update health
    private float health;

    // add, update style
    private BarColor color;
    private BarStyle barStyle;

    // add, update flags
    private boolean shouldDarkenSky;
    private boolean dragonBar;
    private boolean createFog;

    public PacketOutBossBar() {
    }

    public PacketOutBossBar(Action action, PhantomBossBar bossBar) {
        this.action = action;
        this.uniqueId = bossBar.getUniqueId();
        this.title = bossBar.getTitleComponent();
        this.health = (float) bossBar.getProgress();
        this.color = bossBar.getColor();
        this.barStyle = bossBar.getStyle();
        this.shouldDarkenSky = bossBar.hasFlag(BarFlag.DARKEN_SKY);
        this.dragonBar = bossBar.hasFlag(BarFlag.PLAY_BOSS_MUSIC);
        this.createFog = bossBar.hasFlag(BarFlag.CREATE_FOG);
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeUniqueId(this.uniqueId);
        dataBuffer.writeVarInt(this.action.ordinal());
        switch (this.action) {
            case ADD:
                dataBuffer.writeString(ComponentSerializer.toString(this.title));
                dataBuffer.writeFloat(this.health);
                dataBuffer.writeVarInt(this.color.ordinal());
                dataBuffer.writeVarInt(this.barStyle.ordinal());
                dataBuffer.writeByte(this.getFlagsAsByte());
                break;
            case REMOVE:
                break;
            case UPDATE_HEALTH:
                dataBuffer.writeFloat(this.health);
                break;
            case UPDATE_TITLE:
                dataBuffer.writeString(ComponentSerializer.toString(this.title));
                break;
            case UPDATE_STYLE:
                dataBuffer.writeVarInt(this.color.ordinal());
                dataBuffer.writeVarInt(this.barStyle.ordinal());
                break;
            case UPDATE_FLAGS:
                dataBuffer.writeByte(this.getFlagsAsByte());
                break;
            default:
                ReportedException.throwWrapped("Unhandled boss bar action type " + this.action);
        }
    }

    @Override
    public void releaseData() {
        this.uniqueId = null;
        this.action = null;
        this.barStyle = null;
        this.color = null;
        this.title = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutBossBar.class);
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public TextComponent getTitle() {
        return this.title;
    }

    public void setTitle(TextComponent title) {
        this.title = title;
    }

    public float getHealth() {
        return this.health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public BarColor getColor() {
        return this.color;
    }

    public void setColor(BarColor color) {
        this.color = color;
    }

    public BarStyle getBarStyle() {
        return this.barStyle;
    }

    public void setBarStyle(BarStyle barStyle) {
        this.barStyle = barStyle;
    }

    public boolean isShouldDarkenSky() {
        return this.shouldDarkenSky;
    }

    public void setShouldDarkenSky(boolean shouldDarkenSky) {
        this.shouldDarkenSky = shouldDarkenSky;
    }

    public boolean isDragonBar() {
        return this.dragonBar;
    }

    public void setDragonBar(boolean dragonBar) {
        this.dragonBar = dragonBar;
    }

    public boolean isCreateFog() {
        return this.createFog;
    }

    public void setCreateFog(boolean createFog) {
        this.createFog = createFog;
    }

    private int getFlagsAsByte() {
        int flags = 0;
        if (this.shouldDarkenSky) {
            flags |= 1;
        }

        if (this.dragonBar) {
            flags |= 2;
        }

        if (this.createFog) {
            flags |= 4;
        }

        return flags;
    }

    public enum Action {

        ADD,
        REMOVE,
        UPDATE_HEALTH,
        UPDATE_TITLE,
        UPDATE_STYLE,
        UPDATE_FLAGS
    }
}

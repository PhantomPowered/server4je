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

import com.github.phantompowered.server4je.common.annotation.Note;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.defaults.PrimitivePacket;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.bukkit.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutWorldBorder extends PrimitivePacket {

    public static final int MAX_WORLD_SIZE = 29999984;

    // all
    private Action action;
    // only set size, lerp size, initialize
    private double size;
    // only lerp size, initialize
    private double oldSize;
    // only lerp size
    private long speed;
    // only set center, initialize
    private double centerX;
    private double centerZ;
    // only initialize
    @Note("By default the max world size (29999984)")
    private int portalBoundary;
    // only initialize, set warning time
    private int warningTime;
    // only initialize, set warning blocks
    private int warningBlocks;

    public PacketOutWorldBorder() {
    }

    public PacketOutWorldBorder(double size) {
        this.action = Action.SET_SIZE;
        this.size = size;
    }

    public PacketOutWorldBorder(double size, double oldSize, long speed) {
        this.action = Action.LERP_SIZE;
        this.size = size;
        this.oldSize = oldSize;
        this.speed = speed;
    }

    public PacketOutWorldBorder(double centerX, double centerZ) {
        this.action = Action.SET_CENTER;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public PacketOutWorldBorder(double size, double oldSize, long speed, double centerX, double centerZ,
                                @Note("By default the max world size (29999984)") int portalBoundary, int warningTime, int warningBlocks) {
        this.action = Action.INITIALIZE;
        this.size = size;
        this.oldSize = oldSize;
        this.speed = speed;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.portalBoundary = portalBoundary;
        this.warningTime = warningTime;
        this.warningBlocks = warningBlocks;
    }

    public PacketOutWorldBorder(@NotNull Action action, int warningTimeOrBlocks) {
        switch (action) {
            case SET_WARNING_TIME:
                this.warningTime = warningTimeOrBlocks;
                break;
            case SET_WARNING_BLOCKS:
                this.warningBlocks = warningTimeOrBlocks;
                break;
            default:
                ReportedException.throwWrapped("This constructor can only be used with SET_WARNING_TIME or SET_WARNING_BLOCKS");
        }
    }

    public PacketOutWorldBorder(Action action, @NotNull WorldBorder worldBorder) {
        this.action = action;
        this.size = this.oldSize = worldBorder.getSize(); // PAIL: TODO: no proper api for that, usually newSize = lerpTarget
        this.speed = 0L; // PAIL: TODO: no proper api for that, usually speed = lerpTime
        this.centerX = worldBorder.getCenter().getX();
        this.centerZ = worldBorder.getCenter().getZ();
        this.warningTime = worldBorder.getWarningTime();
        this.portalBoundary = PacketOutWorldBorder.MAX_WORLD_SIZE;
        this.warningBlocks = worldBorder.getWarningDistance();
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.action.ordinal());
        switch (this.action) {
            case SET_SIZE:
                dataBuffer.writeDouble(this.size);
                break;
            case LERP_SIZE:
                dataBuffer.writeDouble(this.oldSize);
                dataBuffer.writeDouble(this.size);
                dataBuffer.writeVarLong(this.speed);
                break;
            case SET_CENTER:
                dataBuffer.writeDouble(this.centerX);
                dataBuffer.writeDouble(this.centerZ);
                break;
            case INITIALIZE:
                dataBuffer.writeDouble(this.centerX);
                dataBuffer.writeDouble(this.centerZ);
                dataBuffer.writeDouble(this.oldSize);
                dataBuffer.writeDouble(this.size);
                dataBuffer.writeVarLong(this.speed);
                dataBuffer.writeVarInt(this.portalBoundary);
                dataBuffer.writeVarInt(this.warningTime);
                dataBuffer.writeVarInt(this.warningBlocks);
                break;
            case SET_WARNING_TIME:
                dataBuffer.writeVarInt(this.warningTime);
                break;
            case SET_WARNING_BLOCKS:
                dataBuffer.writeVarInt(this.warningBlocks);
                break;
            default:
                ReportedException.throwWrapped("Unhandled world boarder action " + this.action);
        }
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutWorldBorder.class);
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public double getSize() {
        return this.size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getOldSize() {
        return this.oldSize;
    }

    public void setOldSize(double oldSize) {
        this.oldSize = oldSize;
    }

    public long getSpeed() {
        return this.speed;
    }

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public double getCenterX() {
        return this.centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenterZ(double centerZ) {
        this.centerZ = centerZ;
    }

    public int getPortalBoundary() {
        return this.portalBoundary;
    }

    public void setPortalBoundary(int portalBoundary) {
        this.portalBoundary = portalBoundary;
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int warningTime) {
        this.warningTime = warningTime;
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int warningBlocks) {
        this.warningBlocks = warningBlocks;
    }

    public enum Action {

        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS
    }
}

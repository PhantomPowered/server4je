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

import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutTitle implements Packet {

    public static final PacketOutTitle HIDE_TITLE = new PacketOutTitle(Action.HIDE);
    public static final PacketOutTitle RESET_TITLE = new PacketOutTitle(Action.RESET);

    // unmodifiable
    private final Action action;
    // only sub title, title, action bar
    private BaseComponent[] text;
    // only times
    private int fadeIn;
    private int stay;
    private int fadeOut;

    private PacketOutTitle(Action action) {
        this.action = action;
    }

    public PacketOutTitle(Action action, BaseComponent[] text) {
        Preconditions.checkArgument(action == Action.TITLE || action == Action.SUBTITLE || action == Action.ACTIONBAR);
        this.action = action;
        this.text = text;
    }

    public PacketOutTitle(int fadeIn, int stay, int fadeOut) {
        this.action = Action.TIMES;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.action.ordinal());
        switch (this.action) {
            case HIDE:
            case RESET:
                break;
            case TIMES:
                dataBuffer.writeInt(this.fadeIn);
                dataBuffer.writeInt(this.stay);
                dataBuffer.writeInt(this.fadeOut);
                break;
            case TITLE:
            case SUBTITLE:
            case ACTIONBAR:
                dataBuffer.writeString(ComponentSerializer.toString(this.text));
                break;
            default:
                ReportedException.throwWrapped("Unhandled title packet action " + this.action);
        }
    }

    @Override
    public void releaseData() {
        this.text = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutTitle.class);
    }

    public Action getAction() {
        return this.action;
    }

    public BaseComponent[] getText() {
        return this.text;
    }

    public void setText(BaseComponent[] text) {
        this.text = text;
    }

    public int getFadeIn() {
        return this.fadeIn;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public int getStay() {
        return this.stay;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public int getFadeOut() {
        return this.fadeOut;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public enum Action {

        TITLE,
        SUBTITLE,
        ACTIONBAR,
        TIMES,
        HIDE,
        RESET
    }
}

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
package com.github.phantompowered.server4je.protocol.legacy.out;

import com.github.phantompowered.server4je.api.PhantomServer;
import com.github.phantompowered.server4je.protocol.legacy.in.PacketInLegacyPing;
import io.netty.buffer.ByteBuf;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class PacketOutLegacyDisconnect {

    private static final String PROTOCOL = Integer.toString(PhantomServer.getInstance().getServerVersion().getProtocolVersion());
    private static final String NAME = PhantomServer.getInstance().getServerVersion().getName();
    private static final String MC_1_3_PING_SPLIT = "§";

    @NotNull
    @Contract("_, _ -> new")
    public static PacketOutLegacyDisconnect fromServerListPingEvent(@NotNull ServerListPingEvent event, @NotNull PacketInLegacyPing.Version version) {
        switch (version) {
            case MC_1_3: {
                String reason = String.join(MC_1_3_PING_SPLIT,
                    allBeforeLineSeparator(event.getMotd()).replaceAll(MC_1_3_PING_SPLIT, ""),
                    Integer.toString(event.getNumPlayers()),
                    Integer.toString(event.getMaxPlayers())
                );
                return new PacketOutLegacyDisconnect(reason);
            }
            case MC_1_4:
            case MC_1_6: {
                String reason = String.join("\0",
                    MC_1_3_PING_SPLIT + "1",
                    PROTOCOL,
                    NAME,
                    allBeforeLineSeparator(event.getMotd()),
                    Integer.toString(event.getNumPlayers()),
                    Integer.toString(event.getMaxPlayers())
                );
                return new PacketOutLegacyDisconnect(reason);
            }
            default:
                throw new IllegalStateException("Unhandled legacy protocol version " + version);
        }
    }

    private static @NotNull String allBeforeLineSeparator(@NotNull String string) {
        int index = string.indexOf('\n');
        return index == -1 ? string : string.substring(0, index);
    }

    private final String reason;

    public PacketOutLegacyDisconnect(String reason) {
        this.reason = reason;
    }

    public void writeData(@NotNull ByteBuf dataBuffer) {
        dataBuffer.writeByte(0xff);
        dataBuffer.writeShort(this.reason.length());
        dataBuffer.writeCharSequence(this.reason, StandardCharsets.UTF_16BE);
    }
}

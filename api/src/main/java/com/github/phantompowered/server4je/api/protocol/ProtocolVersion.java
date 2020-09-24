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
package com.github.phantompowered.server4je.api.protocol;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public enum ProtocolVersion {

    UNKNOWN(-1, "Unknown"),
    LEGACY(-2, "Legacy"),
    MINECRAFT_1_7_2(4, "1.7.2"),
    MINECRAFT_1_7_6(5, "1.7.6"),
    MINECRAFT_1_8(47, "1.8"),
    MINECRAFT_1_9(107, "1.9"),
    MINECRAFT_1_9_1(108, "1.9.1"),
    MINECRAFT_1_9_2(109, "1.9.2"),
    MINECRAFT_1_9_4(110, "1.9.4"),
    MINECRAFT_1_10(210, "1.10"),
    MINECRAFT_1_11(315, "1.11"),
    MINECRAFT_1_11_1(316, "1.11.1"),
    MINECRAFT_1_12(335, "1.12"),
    MINECRAFT_1_12_1(338, "1.12.1"),
    MINECRAFT_1_12_2(340, "1.12.2"),
    MINECRAFT_1_13(393, "1.13"),
    MINECRAFT_1_13_1(401, "1.13.1"),
    MINECRAFT_1_13_2(404, "1.13.2"),
    MINECRAFT_1_14(477, "1.14"),
    MINECRAFT_1_14_1(480, "1.14.1"),
    MINECRAFT_1_14_2(485, "1.14.2"),
    MINECRAFT_1_14_3(490, "1.14.3"),
    MINECRAFT_1_14_4(498, "1.14.4"),
    MINECRAFT_1_15(573, "1.15"),
    MINECRAFT_1_15_1(575, "1.15.1"),
    MINECRAFT_1_15_2(578, "1.15.2"),
    MINECRAFT_1_16(735, "1.16"),
    MINECRAFT_1_16_1(736, "1.16.1"),
    MINECRAFT_1_16_2(751, "1.16.2"),
    MINECRAFT_1_16_3(753, "1.16.3");

    public static final ProtocolVersion[] VALUES = values(); // prevent copy
    private static final ProtocolVersion SUPPORTED_VERSION = VALUES[VALUES.length - 1];
    private static final ImmutableMap<Integer, ProtocolVersion> ID_TO_PROTOCOL_CONSTANT;

    static {
        ImmutableMap.Builder<Integer, ProtocolVersion> builder = ImmutableMap.builder();
        for (ProtocolVersion version : values()) {
            builder.put(version.protocol, version);
        }

        ID_TO_PROTOCOL_CONSTANT = builder.build();
    }

    private final int protocol;
    private final String name;

    ProtocolVersion(int protocol, String name) {
        this.protocol = protocol;
        this.name = name;
    }

    @Range(from = -2, to = Short.MAX_VALUE)
    public int getProtocol() {
        return this.protocol;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public boolean isLegacy() {
        return this == LEGACY;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @NotNull
    public static ProtocolVersion getProtocolVersion(int protocol) {
        return ID_TO_PROTOCOL_CONSTANT.getOrDefault(protocol, UNKNOWN);
    }

    @NotNull
    public static ProtocolVersion getSupportedProtocolVersion() {
        return SUPPORTED_VERSION;
    }

    public static boolean isSupported(int protocol) {
        return protocol == ProtocolVersion.SUPPORTED_VERSION.getProtocol();
    }

    public static boolean isSupported(ProtocolVersion version) {
        return version != null && version == ProtocolVersion.SUPPORTED_VERSION;
    }
}

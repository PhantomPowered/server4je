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
package com.github.phantompowered.server4je.protocol.id;

import com.github.phantompowered.server4je.common.CommonConstants;
import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketIdUtil {

    private PacketIdUtil() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    private static final Map<ProtocolState, Map<String, Short>> IDS = new ConcurrentHashMap<>();
    private static final Type TYPE = TypeToken.getParameterized(Map.class, String.class, Short.class).getType();

    public static short getPacketId(@NotNull ProtocolState state, @NotNull Class<?> clazz) {
        if (IDS.isEmpty()) {
            loadIds();
        }

        var ids = IDS.get(state);
        if (ids == null) {
            ReportedException.throwWrapped("No protocol ids loaded for state " + state);
        }

        Short id = ids.get(clazz.getName());
        if (id == null) {
            ReportedException.throwWrapped("Missing protocol id for packet " + clazz.getName());
        }

        return id;
    }

    private static void loadIds() {
        var stream = PacketIdUtil.class.getClassLoader().getResourceAsStream("packet-ids.json");
        if (stream == null) {
            ReportedException.throwWrapped("Unable to find packet-ids.json file");
        }

        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var element = JsonParser.parseReader(reader).getAsJsonObject();
            for (var state : ProtocolState.values()) {
                loadIds(element, state);
            }
        } catch (IOException exception) {
            ReportedException.throwWrapped(exception, "Exception loading packet ids");
        }
    }

    private static void loadIds(@NotNull JsonObject dataHolder, @NotNull ProtocolState state) {
        var ids = dataHolder.get(state.name());
        if (ids == null || ids instanceof JsonNull) {
            ReportedException.throwWrapped("No packet ids found for " + state);
        }

        Map<String, Short> idsMapped = CommonConstants.getGson().fromJson(ids, PacketIdUtil.TYPE);
        if (idsMapped == null) {
            ReportedException.throwWrapped("Packet ids not formatted correctly " + state);
        }

        IDS.put(state, idsMapped);
    }
}

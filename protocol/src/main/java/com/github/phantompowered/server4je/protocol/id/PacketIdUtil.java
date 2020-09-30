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
import com.github.phantompowered.server4je.common.collect.Iterables;
import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PacketIdUtil {

    private PacketIdUtil() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    private static final Map<ProtocolState, Set<Map.Entry<String, Short>>> SERVER_IDS = new ConcurrentHashMap<>();
    private static final Map<ProtocolState, Set<Map.Entry<String, Short>>> CLIENT_IDS = new ConcurrentHashMap<>();
    private static final Type TYPE = TypeToken.getParameterized(Map.class, String.class, Short.class).getType();

    public static short getServerPacketId(@NotNull ProtocolState state, @NotNull Class<?> clazz) {
        if (SERVER_IDS.isEmpty()) {
            loadIds("server");
        }

        var ids = SERVER_IDS.get(state);
        if (ids == null) {
            ReportedException.throwWrapped("No protocol ids loaded for state " + state);
        }

        return Iterables.first(ids, entry -> entry.getKey().equals(clazz.getName())).orElseThrow().getValue();
    }

    public static short getClientPacketId(@NotNull ProtocolState state, @NotNull Class<?> clazz) {
        if (CLIENT_IDS.isEmpty()) {
            loadIds("client");
        }

        var ids = CLIENT_IDS.get(state);
        if (ids == null) {
            ReportedException.throwWrapped("No protocol ids loaded for state " + state);
        }

        return Iterables.first(ids, entry -> entry.getKey().equals(clazz.getName())).orElseThrow().getValue();
    }

    @NotNull
    public static Packet getClientPacket(@NotNull ProtocolState state, short packetId) {
        if (CLIENT_IDS.isEmpty()) {
            loadIds("client");
        }

        var ids = CLIENT_IDS.get(state);
        if (ids == null) {
            ReportedException.throwWrapped("No protocol ids loaded for state " + state);
        }

        return Iterables.first(ids, entry -> entry.getValue() == packetId).map(entry -> {
            try {
                //noinspection unchecked
                Class<? extends Packet> clazz = (Class<? extends Packet>) Class.forName(entry.getKey());
                return clazz.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                return null;
            }
        }).orElseThrow();
    }

    private static void loadIds(@NotNull String type) {
        var stream = PacketIdUtil.class.getClassLoader().getResourceAsStream("packet-ids-" + type + ".json");
        if (stream == null) {
            ReportedException.throwWrapped("Unable to find packet-ids-" + type + ".json file");
        }

        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var element = JsonParser.parseReader(reader).getAsJsonObject();
            for (var state : ProtocolState.values()) {
                loadIds(element, state, type);
            }
        } catch (IOException exception) {
            ReportedException.throwWrapped(exception, "Exception loading packet " + type + " ids");
        }
    }

    private static void loadIds(@NotNull JsonObject dataHolder, @NotNull ProtocolState state, @NotNull String type) {
        var ids = dataHolder.get(state.name());
        if (ids == null || ids instanceof JsonNull) {
            ReportedException.throwWrapped("No packet ids found for " + state);
        }

        Map<String, Short> idsMapped = CommonConstants.getGson().fromJson(ids, PacketIdUtil.TYPE);
        if (idsMapped == null) {
            ReportedException.throwWrapped("Packet ids not formatted correctly " + state);
        }

        switch (type.toLowerCase()) {
            case "server":
                SERVER_IDS.put(state, idsMapped.entrySet());
                break;
            case "client":
                CLIENT_IDS.put(state, idsMapped.entrySet());
                break;
            default:
                ReportedException.throwWrapped("No default id state for " + type);
        }
    }
}

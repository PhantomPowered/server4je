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
package com.github.phantompowered.server4je.config;

import com.github.phantompowered.server4je.api.config.Messages;
import com.github.phantompowered.server4je.api.config.ServerConfig;
import com.github.phantompowered.server4je.api.network.NetworkListener;
import com.github.phantompowered.server4je.network.listener.ServerNetworkListener;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class JsonServerConfig implements ServerConfig {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(TypeToken.getParameterized(Collection.class, NetworkListener.class).getType(), new NetworkListenerCollectionSerializer())
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .serializeNulls()
        .create();

    @NotNull
    public static ServerConfig load(@NotNull Path path) {
        if (Files.notExists(path)) {
            try (var writer = new OutputStreamWriter(Files.newOutputStream(path))) {
                GSON.toJson(new JsonServerConfig(), writer);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to write server config file", exception);
            }
        }

        try (var reader = new InputStreamReader(Files.newInputStream(path))) {
            return GSON.fromJson(reader, JsonServerConfig.class);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read server config file", exception);
        }
    }

    private Collection<NetworkListener> networkListeners;
    private Collection<String> worldsToLoad;
    private Messages messages;
    private IpForwardingMode ipForwardingMode;
    private @Nullable String velocityForwardingSecret;
    private int maxPlayers;
    private int compressionThreshold;

    private JsonServerConfig() {
        this.networkListeners = Collections.singletonList(new ServerNetworkListener("0.0.0.0", 25565));
        this.worldsToLoad = Arrays.asList("world", "world_nether", "world_the_end");
        this.messages = new Messages();
        this.ipForwardingMode = IpForwardingMode.DISABLED;
        this.maxPlayers = 20;
        this.compressionThreshold = 256;
    }

    @Override
    public @NotNull Collection<NetworkListener> getNetworkListeners() {
        return this.networkListeners;
    }

    @Override
    public void setNetworkListeners(@NotNull Collection<NetworkListener> networkListeners) {
        this.networkListeners = networkListeners;
    }

    @Override
    public void addNetworkListeners(@NotNull NetworkListener... networkListener) {
        this.networkListeners.addAll(Arrays.asList(networkListener));
    }

    @Override
    public void removeNetworkListeners(@NotNull NetworkListener... networkListeners) {
        this.networkListeners.removeAll(Arrays.asList(networkListeners));
    }

    @Override
    public void clearNetworkListeners() {
        this.networkListeners.clear();
    }

    @Override
    @NotNull
    public Collection<String> getWorldsToLoad() {
        return this.worldsToLoad;
    }

    @Override
    public void setWorldsToLoad(@NotNull Collection<String> worlds) {
        this.worldsToLoad = worlds;
    }

    @Override
    public void addWorldsToLoad(@NotNull String... worlds) {
        this.worldsToLoad.addAll(Arrays.asList(worlds));
    }

    @Override
    public void removeWorldsToLoad(@NotNull String... worlds) {
        this.worldsToLoad.removeAll(Arrays.asList(worlds));
    }

    @Override
    public void clearWorldsToLoad() {
        this.worldsToLoad.clear();
    }

    @Override
    @NotNull
    public Messages getMessages() {
        return this.messages;
    }

    @Override
    public void setMessages(@NotNull Messages messages) {
        this.messages = messages;
    }

    @Override
    @NotNull
    public IpForwardingMode getIpForwardingMode() {
        return this.ipForwardingMode;
    }

    @Override
    public void setIpForwardingMode(@NotNull IpForwardingMode mode, @Nullable String velocityForwardSecret) {
        Preconditions.checkArgument(
            mode != IpForwardingMode.VELOCITY || velocityForwardSecret != null,
            "If ip forward mode is set to modern velocity forwarding please ensure you set the velocity forwarding secret"
        );

        this.ipForwardingMode = mode;
        this.velocityForwardingSecret = velocityForwardSecret;
    }

    @Override
    @Nullable
    public String getVelocityForwardSecret() {
        return this.velocityForwardingSecret;
    }

    @Override
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Override
    public void setMaxPlayers(@Range(from = 0, to = Integer.MAX_VALUE) int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    @Override
    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }

    @Override
    public void setCompressionThreshold(@Range(from = 0, to = Integer.MAX_VALUE) int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }

    @Override
    public boolean isCompressionEnabled() {
        return this.compressionThreshold > 0;
    }

    private static final class NetworkListenerCollectionSerializer
        implements JsonSerializer<Collection<NetworkListener>>, JsonDeserializer<Collection<NetworkListener>> {

        @Override
        public Collection<NetworkListener> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Collection<NetworkListener> networkListeners = new ArrayList<>();
            JsonArray jsonElements = json.getAsJsonArray();

            for (JsonElement jsonElement : jsonElements) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                networkListeners.add(new ServerNetworkListener(
                    jsonObject.get("host").getAsString(),
                    jsonObject.get("port").getAsInt(),
                    jsonObject.get("preventProxyConnections").getAsBoolean()
                ));
            }

            return networkListeners;
        }

        @Override
        public JsonElement serialize(Collection<NetworkListener> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray jsonElements = new JsonArray();
            for (NetworkListener networkListener : src) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("host", networkListener.getHostString());
                jsonObject.addProperty("port", networkListener.getPort());
                jsonObject.addProperty("preventProxyConnections", networkListener.isPreventProxyConnections());
                jsonElements.add(jsonObject);
            }

            return jsonElements;
        }
    }
}

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
package com.github.phantompowered.server4je.authlib.session;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.phantompowered.server4je.authlib.SessionService;
import com.github.phantompowered.server4je.authlib.exception.AuthenticationFailedException;
import com.github.phantompowered.server4je.authlib.gson.PropertyMultimapSerializer;
import com.github.phantompowered.server4je.authlib.gson.UniqueIdTypeAdapter;
import com.github.phantompowered.server4je.authlib.profile.GameProfile;
import com.github.phantompowered.server4je.common.CommonConstants;
import com.github.phantompowered.server4je.common.concurrent.Callback;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseSessionService implements SessionService {

    protected static final String BASE_URL = "https://sessionserver.mojang.com/session/minecraft/";
    protected static final String HAS_JOINED_URL = BASE_URL + "hasJoined?username=%s&serverId=%s";

    protected final Gson gson = new GsonBuilder()
        .registerTypeAdapter(TypeToken.getParameterized(Multimap.class, String.class, ProfileProperty.class).getType(), new PropertyMultimapSerializer())
        .registerTypeAdapter(UUID.class, new UniqueIdTypeAdapter())
        .create();

    private Proxy proxy;

    @Override
    public @NotNull Optional<GameProfile> hasJoinedServer(@NotNull GameProfile user, @NotNull String serverId, @Nullable InetAddress proxy) throws AuthenticationFailedException {
        String payloadUrl = String.format(HAS_JOINED_URL, user.getName(), serverId);
        if (proxy != null) {
            payloadUrl += "&ip=" + proxy.getHostAddress();
        }

        try {
            GameProfile gameProfile = this.gson.fromJson(this.get(payloadUrl), GameProfile.class);
            return gameProfile != null && gameProfile.isComplete() ? Optional.of(gameProfile) : Optional.empty();
        } catch (JsonSyntaxException | AuthenticationFailedException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void fillProfile(@NotNull GameProfile gameProfile, boolean requireSecure, @NotNull Callback<GameProfile> callback) {
        CommonConstants.TASK_POOL.execute(() -> {
            try {
                GameProfile profile = this.fillProfile(gameProfile, requireSecure);
                callback.done(profile, null);
            } catch (Throwable throwable) {
                callback.done(gameProfile, throwable);
            }
        });
    }

    @Override
    public @NotNull Optional<Proxy> getUsedProxy() {
        return Optional.ofNullable(this.proxy);
    }

    @Override
    public void setUsedProxy(@Nullable Proxy proxy) {
        this.proxy = proxy;
    }

    @NotNull
    protected String get(@NotNull String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(this.proxy == null ? Proxy.NO_PROXY : this.proxy);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setUseCaches(false);
            connection.connect(); // ensure

            try (var stream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream()) {
                if (stream == null) {
                    throw new IOException("No data stream available");
                }

                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } finally {
                connection.disconnect();
            }
        } catch (IOException exception) {
            throw new AuthenticationFailedException(exception);
        }
    }
}

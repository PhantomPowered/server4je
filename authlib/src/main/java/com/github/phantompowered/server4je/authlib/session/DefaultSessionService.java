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

import com.github.phantompowered.server4je.authlib.profile.GameProfile;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultSessionService extends BaseSessionService {

    private static final String PROFILE_LOOKUP_URL = BaseSessionService.BASE_URL + "profile/%s?unsigned=%b";

    private final LoadingCache<GameProfile, GameProfile> insecureProfileCache = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.HOURS)
        .build(new CacheLoader<>() {
            @Override
            public GameProfile load(@NotNull GameProfile key) {
                return DefaultSessionService.this.fillProfile0(key, false);
            }
        });

    @Override
    public @NotNull GameProfile fillProfile(@NotNull GameProfile gameProfile, boolean requireSecure) {
        if (gameProfile.getId() == null) {
            return gameProfile;
        }

        return requireSecure ? this.fillProfile0(gameProfile, true) : this.insecureProfileCache.getUnchecked(gameProfile);
    }

    @NotNull
    protected GameProfile fillProfile0(@NotNull GameProfile gameProfile, boolean requireSecure) {
        Preconditions.checkNotNull(gameProfile.getId(), "Game profile needs non-null unique id");

        GameProfile profile = this.getGameProfile(gameProfile.getId(), requireSecure);
        if (profile != null) {
            gameProfile.override(profile);
        }

        return gameProfile;
    }

    protected @Nullable GameProfile getGameProfile(@NotNull UUID uniqueId, boolean requireSecure) {
        String payloadUrl = String.format(PROFILE_LOOKUP_URL, uniqueId.toString().replace("-", ""), !requireSecure);
        return this.gson.fromJson(this.get(payloadUrl), GameProfile.class);
    }
}

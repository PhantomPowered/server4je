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
package com.github.phantompowered.server4je.authlib.profile;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.phantompowered.server4je.api.profile.PhantomPlayerProfile;
import com.github.phantompowered.server4je.authlib.SessionService;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class GameProfile implements PhantomPlayerProfile {

    public static final AtomicReference<SessionService> DEFAULT_SESSION_SERVICE = new AtomicReference<>(); // Set by the server
    private static final Cache<GameProfile, GameProfile> CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.HOURS)
        .ticker(Ticker.systemTicker())
        .build();

    private final Set<ProfileProperty> properties = Sets.newConcurrentHashSet();

    private UUID uniqueId;
    private String name;
    private boolean legacy;

    public GameProfile(UUID uniqueId, String name) {
        Preconditions.checkArgument(uniqueId != null || (name != null && !name.isBlank()), "Either name or unique-id has to be given");

        this.uniqueId = uniqueId;
        this.name = name;
    }

    @Override
    public @Nullable String getName() {
        return this.name;
    }

    @Override
    public @NotNull String setName(@Nullable String name) {
        final String prev = this.name;
        this.name = name;
        return prev;
    }

    @Override
    public @Nullable UUID getId() {
        return this.uniqueId;
    }

    @Override
    public @Nullable UUID setId(@Nullable UUID uuid) {
        final UUID prev = this.uniqueId;
        this.uniqueId = uuid;
        return prev;
    }

    @NotNull
    public Set<ProfileProperty> getProperties() {
        return this.properties;
    }

    @Override
    public boolean hasProperty(@Nullable String property) {
        for (ProfileProperty profileProperty : this.properties) {
            if (profileProperty.getName().equals(property)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setProperty(@NotNull ProfileProperty property) {
        this.properties.removeIf(profileProperty -> profileProperty.getName().equals(property.getName()));
        this.properties.add(property);
    }

    @Override
    public void setProperties(@NotNull Collection<ProfileProperty> properties) {
        for (ProfileProperty property : properties) {
            this.properties.removeIf(profileProperty -> profileProperty.getName().equals(property.getName()));
            this.properties.add(property);
        }
    }

    @Override
    public boolean removeProperty(@Nullable String property) {
        return this.properties.removeIf(profileProperty -> profileProperty.getName().equals(property));
    }

    @Override
    public void clearProperties() {
        this.properties.clear();
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public boolean isComplete() {
        return this.uniqueId != null && this.name != null && !this.name.isBlank();
    }

    @Override
    public boolean completeFromCache() {
        return this.completeFromCache(Bukkit.getOnlineMode());
    }

    @Override
    public boolean completeFromCache(boolean onlineMode) {
        return this.completeFromCache(true, onlineMode);
    }

    @Override
    public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) {
        if (lookupUUID && onlineMode && this.uniqueId == null) {
            DEFAULT_SESSION_SERVICE.get().fillProfile(this, true);
            if (this.isComplete()) {
                CACHE.put(this, this);
            }
        }

        GameProfile other = CACHE.getIfPresent(this);
        if (other != null) {
            this.override(other);
        }

        return other != null;
    }

    @Override
    public boolean complete(boolean textures) {
        return this.complete(textures, Bukkit.getOnlineMode());
    }

    @Override
    public boolean complete(boolean textures, boolean onlineMode) {
        boolean completedUsingCache = this.completeFromCache(true, onlineMode);
        if (onlineMode && (!completedUsingCache || (textures && !this.hasTextures()))) {
            DEFAULT_SESSION_SERVICE.get().fillProfile(this, true);
            if (this.isComplete()) {
                CACHE.put(this, this);
            }
        }

        return this.isComplete() && (!textures || !onlineMode || this.hasTextures());
    }

    @Override
    public boolean hasTextures() {
        return this.hasProperty("textures");
    }

    public void override(@NotNull GameProfile gameProfile) {
        if (gameProfile.getId() != null) {
            this.uniqueId = gameProfile.getId();
        }

        if (gameProfile.getName() != null) {
            this.name = gameProfile.getName();
        }

        this.legacy = gameProfile.isLegacy();
        this.properties.addAll(gameProfile.getProperties());
    }

    @Override
    public @NotNull CompletableFuture<Boolean> completeFromCacheAsync() {
        return CompletableFuture.supplyAsync(this::completeFromCache);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> completeFromCacheAsync(boolean onlineMode) {
        return CompletableFuture.supplyAsync(() -> this.completeFromCache(onlineMode));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> completeFromCacheAsync(boolean lookupUUID, boolean onlineMode) {
        return CompletableFuture.supplyAsync(() -> this.completeFromCache(lookupUUID, onlineMode));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> completeAsync() {
        return CompletableFuture.supplyAsync(this::complete);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> completeAsync(boolean textures) {
        return CompletableFuture.supplyAsync(() -> this.complete(textures));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> completeAsync(boolean textures, boolean onlineMode) {
        return CompletableFuture.supplyAsync(() -> this.complete(textures, onlineMode));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        var that = (GameProfile) o;
        return Objects.equals(this.uniqueId, that.uniqueId) && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId, this.name);
    }

    @Override
    public String toString() {
        return "GameProfile{"
            + "id=" + this.uniqueId
            + ", name='" + this.name + '\''
            + ", properties=" + this.properties
            + ", legacy=" + this.legacy
            + '}';
    }
}

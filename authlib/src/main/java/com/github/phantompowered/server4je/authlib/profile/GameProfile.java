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
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class GameProfile {

    private final Multimap<String, ProfileProperty> properties = LinkedHashMultimap.create();

    private UUID uniqueId;
    private String name;
    private boolean legacy;

    public GameProfile(UUID uniqueId, String name) {
        Preconditions.checkArgument(uniqueId != null || (name != null && !name.isBlank()), "Either name or unique-id has to be given");

        this.uniqueId = uniqueId;
        this.name = name;
    }

    @Nullable
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @NotNull
    public Multimap<String, ProfileProperty> getProperties() {
        return this.properties;
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public boolean isComplete() {
        return this.uniqueId != null && this.name != null && !this.name.isBlank();
    }

    public boolean hasTextures() {
        return !this.properties.get("textures").isEmpty();
    }

    public void override(@NotNull GameProfile gameProfile) {
        if (gameProfile.getUniqueId() != null) {
            this.uniqueId = gameProfile.getUniqueId();
        }

        if (gameProfile.getName() != null) {
            this.name = gameProfile.getName();
        }

        this.legacy = gameProfile.isLegacy();
        this.properties.putAll(gameProfile.getProperties());
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

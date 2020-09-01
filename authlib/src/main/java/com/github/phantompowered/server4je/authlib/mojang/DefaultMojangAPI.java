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
package com.github.phantompowered.server4je.authlib.mojang;

import com.github.phantompowered.server4je.authlib.MojangAPI;
import com.github.phantompowered.server4je.authlib.gson.UniqueIdTypeAdapter;
import com.github.phantompowered.server4je.authlib.profile.GameProfile;
import com.github.phantompowered.server4je.authlib.session.DefaultSessionService;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Optional;
import java.util.UUID;

public class DefaultMojangAPI extends DefaultSessionService implements MojangAPI {

    private static final String BASE_URL = "https://api.mojang.com/";
    private static final String NAME_TO_UNIQUE_ID_URL = BASE_URL + "users/profiles/minecraft/%s";

    @Override
    public @NotNull Optional<UUID> getPlayerUniqueId(@NotNull String username) {
        return this.getPlayerUniqueIdAt0(username, -1);
    }

    @Override
    public @NotNull Optional<UUID> getPlayerUniqueIdAt(@NotNull String username, @Range(from = 0, to = Long.MAX_VALUE) long timestamp) {
        return this.getPlayerUniqueIdAt0(username, timestamp);
    }

    private @NotNull Optional<UUID> getPlayerUniqueIdAt0(@NotNull String username, long timestamp) {
        String payloadUrl = String.format(NAME_TO_UNIQUE_ID_URL, username);
        if (timestamp > -1) {
            payloadUrl += "?at=" + timestamp;
        }

        try {
            var object = JsonParser.parseString(this.get(payloadUrl)).getAsJsonObject();
            if (object.has("id") && !object.get("id").isJsonNull()) {
                return Optional.of(UniqueIdTypeAdapter.fromString(object.get("id").getAsString()));
            }

            return Optional.empty();
        } catch (Throwable throwable) {
            return Optional.empty();
        }
    }

    @Override
    public @NotNull Optional<String> getPlayerName(@NotNull UUID uniqueId) {
        GameProfile profile = this.getGameProfile(uniqueId, false);
        return profile != null && profile.getName() != null ? Optional.of(profile.getName()) : Optional.empty();
    }
}

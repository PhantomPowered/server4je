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
package com.github.phantompowered.server4je.authlib;

import com.github.phantompowered.server4je.authlib.exception.AuthenticationFailedException;
import com.github.phantompowered.server4je.authlib.profile.GameProfile;
import com.github.phantompowered.server4je.common.concurrent.Callback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.Proxy;
import java.util.Optional;

public interface SessionService {

    @NotNull
    Optional<GameProfile> hasJoinedServer(@NotNull GameProfile user, @NotNull String serverId, @Nullable InetAddress proxy) throws AuthenticationFailedException;

    @NotNull
    GameProfile fillProfile(@NotNull GameProfile gameProfile, boolean requireSecure);

    void fillProfile(@NotNull GameProfile gameProfile, boolean requireSecure, @NotNull Callback<GameProfile> callback);

    @NotNull
    Optional<Proxy> getUsedProxy();

    void setUsedProxy(@Nullable Proxy proxy);
}

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
package com.github.phantompowered.server4je.version;

import com.github.phantompowered.server4je.api.version.ServerVersion;
import org.jetbrains.annotations.NotNull;

public class PhantomServerVersion implements ServerVersion {

    private PhantomServerVersion(String id, String name, String releaseTarget, String buildTime, int worldVersion, int protocolVersion, int packVersion, boolean stable) {
        this.id = id;
        this.name = name;
        this.releaseTarget = releaseTarget;
        this.buildTime = buildTime;
        this.worldVersion = worldVersion;
        this.protocolVersion = protocolVersion;
        this.packVersion = packVersion;
        this.stable = stable;
    }

    private final String id;
    private final String name;
    private final String releaseTarget;
    private final String buildTime;
    private final int worldVersion;
    private final int protocolVersion;
    private final int packVersion;
    private final boolean stable;

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull String getReleaseTarget() {
        return this.releaseTarget;
    }

    @Override
    public @NotNull String getBuildTime() {
        return this.buildTime;
    }

    @Override
    public int getWorldVersion() {
        return this.worldVersion;
    }

    @Override
    public int getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public int getPackVersion() {
        return this.packVersion;
    }

    @Override
    public boolean isStable() {
        return this.stable;
    }
}

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
package com.github.phantompowered.server4je.protocol.utils;

import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import com.github.phantompowered.server4je.common.math.MathHelper;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class LocationUtil {

    private LocationUtil() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    private static final int NUM_X_BITS = 1 + MathHelper.calculateLogBaseTwo(MathHelper.roundUpToPowerOfTwo(30_000_000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    public static long locationToLong(@NotNull Location location) {
        return ((long) location.getBlockX() & X_MASK) << X_SHIFT | ((long) location.getBlockY() & Y_MASK) << Y_SHIFT | ((long) location.getBlockZ() & Z_MASK);
    }

    @NotNull
    public static Location locationFromLong(long hash) {
        int x = (int) (hash << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
        int y = (int) (hash << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
        int z = (int) (hash << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
        return new Location(null, x, y, z);
    }
}

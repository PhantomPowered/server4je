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
package com.github.phantompowered.server4je.common.misc;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class KeyValueHolder<L, R> implements Map.Entry<L, R> {

    private final L key;
    private final R value;

    private KeyValueHolder(L key, R value) {
        this.key = key;
        this.value = value;
    }

    public static <L, R> @NotNull KeyValueHolder<L, R> key(L key) {
        return of(key, null);
    }

    public static <L, R> @NotNull KeyValueHolder<L, R> value(R value) {
        return of(null, value);
    }

    public static <L, R> @NotNull KeyValueHolder<L, R> of(L key, R value) {
        return new KeyValueHolder<>(key, value);
    }

    @Override
    public L getKey() {
        return this.key;
    }

    @Override
    public R getValue() {
        return this.value;
    }

    @Override
    public R setValue(R value) {
        throw new UnsupportedOperationException("Not supported in this implementation");
    }
}

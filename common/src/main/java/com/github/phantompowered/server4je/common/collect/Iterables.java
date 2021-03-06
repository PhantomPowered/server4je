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
package com.github.phantompowered.server4je.common.collect;

import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class Iterables {

    private Iterables() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    public static <T> Optional<T> first(@NotNull Collection<T> collection, @NotNull Predicate<T> filter) {
        for (T t : collection) {
            if (filter.test(t)) {
                return Optional.of(t);
            }
        }

        return Optional.empty();
    }

    @NotNull
    public static <T> Collection<T> all(@NotNull Collection<T> collection, @NotNull Predicate<T> filter) {
        collection.removeIf(filter.negate());
        return collection;
    }

    @NotNull
    public static <K, V> Collection<Map.Entry<K, V>> allEntries(@NotNull Set<Map.Entry<K, V>> entries, @NotNull Predicate<K> filter) {
        entries.removeIf(entry -> !filter.test(entry.getKey()));
        return entries;
    }

    public static <T> boolean anyMatch(@NotNull Collection<T> collection, @NotNull Predicate<T> predicate) {
        for (T t : collection) {
            if (predicate.test(t)) {
                return true;
            }
        }

        return false;
    }
}

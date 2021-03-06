/*
 * MIT License
 *
 * Copyright (c) ReformCloud-Team
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.phantompowered.server4je.common.enums;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class EnumUtil {

    private EnumUtil() {
        throw new UnsupportedOperationException();
    }

    private static final Map<Class<? extends Enum<?>>, Entry> CACHE = new ConcurrentHashMap<>();
    private static final IllegalStateException BASE_NULL = new IllegalStateException("Entry base evaluated null instead of enum set");

    /**
     * Tries to find an enum field by the given name, using a weak cache.
     *
     * @param enumClass The class to find the constant in
     * @param field     The name of the field to find
     * @param <T>       The type of the enum class
     * @return An optional which is empty if no field by the given value was found or containing the field associated with the name
     */
    @NotNull
    public static <T extends Enum<T>> Optional<T> findEnumFieldByName(@NotNull Class<T> enumClass, @NotNull String field) {
        for (Enum<T> anEnum : EnumUtil.getEnumEntries(enumClass)) {
            if (anEnum.name().equalsIgnoreCase(field)) {
                return Optional.of(enumClass.cast(anEnum));
            }
        }

        return Optional.empty();
    }

    /**
     * Tries to find an enum field by the given ordinal index.
     *
     * @param enumClass The class to find the constant in
     * @param ordinal   The ordinal field of the enum constant
     * @param <T>       The type of the enum class
     * @return An optional which is empty if no field by the given value was found or containing the field associated with the ordinal index
     */
    @NotNull
    public static <T extends Enum<T>> Optional<T> findEnumFieldByIndex(@NotNull Class<T> enumClass, int ordinal) {
        for (Enum<T> anEnum : EnumUtil.getEnumEntries(enumClass)) {
            if (anEnum.ordinal() == ordinal) {
                return Optional.of(enumClass.cast(anEnum));
            }
        }

        return Optional.empty();
    }

    /**
     * Gets all values of an enum class using a weak cache.
     *
     * @param enumClass The class to get all values of
     * @param <T>       The Type of the enum class
     * @return A set of all fields in the enum class
     * @throws IllegalStateException if the enum set is not present in the cache
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> EnumSet<T> getEnumEntries(@NotNull Class<T> enumClass) {
        if (!CACHE.containsKey(enumClass)) {
            CACHE.put(enumClass, EnumUtil.compute(enumClass));
        }

        EnumSet<T> enumSet = (EnumSet<T>) CACHE.get(enumClass).base.get();
        if (enumSet != null) {
            return enumSet;
        }

        throw EnumUtil.BASE_NULL;
    }

    @NotNull
    private static <T extends Enum<T>> Entry compute(@NotNull Class<T> enumClass) {
        try {
            return new Entry(EnumSet.allOf(enumClass));
        } catch (Throwable ignored) {
        }

        return Entry.EMPTY;
    }

    private static final class Entry {

        public static final Entry EMPTY = new Entry();

        private final WeakReference<EnumSet<?>> base;

        private Entry() {
            this.base = new WeakReference<>(null);
        }

        private Entry(@NotNull EnumSet<?> set) {
            this.base = new WeakReference<>(set);
        }
    }
}

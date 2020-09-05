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
package com.github.phantompowered.server4je.api.data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Optional;

public interface DataWatcher {

    @NotNull
    @UnmodifiableView
    Collection<DataWatcherEntry> getEntries();

    @NotNull
    DataWatcher setObject(@Range(from = 0, to = 31) int id, @NotNull Object object);

    @NotNull
    DataWatcher addEmptyObjectByDataType(@Range(from = 0, to = 31) int id, @Range(from = 0, to = 7) int type);

    @NotNull
    DataWatcher removeObject(@Range(from = 0, to = 31) int id);

    byte getByte(int id);

    @NotNull
    DataWatcher setByte(@Range(from = 0, to = 31) int id, int value);

    boolean getBoolean(@Range(from = 0, to = 31) int id);

    @NotNull
    DataWatcher setBoolean(@Range(from = 0, to = 31) int id, boolean value);

    short getShort(@Range(from = 0, to = 31) int id);

    @NotNull
    DataWatcher setShort(@Range(from = 0, to = 31) int id, int value);

    int getInt(@Range(from = 0, to = 31) int id);

    @NotNull
    DataWatcher setInt(@Range(from = 0, to = 31) int id, int value);

    float getFloat(int id);

    @NotNull
    DataWatcher setFloat(@Range(from = 0, to = 31) int id, double value);

    @NotNull
    Optional<String> getString(@Range(from = 0, to = 31) int id);

    @NotNull
    DataWatcher setString(@Range(from = 0, to = 31) int id, @NotNull String value);

    @NotNull
    Optional<ItemStack> getItemStack(@Range(from = 0, to = 31) int id);

    @NotNull
    DataWatcher setItemStack(@Range(from = 0, to = 31) int id, @NotNull ItemStack itemStack);

    @NotNull
    Optional<EulerAngle> getEulerAngle(@Range(from = 0, to = 31) int id);

    @NotNull
    DataWatcher setEulerAngle(@Range(from = 0, to = 31) int id, @NotNull EulerAngle angle);

    @NotNull
    Optional<DataWatcherEntry> getEntry(@Range(from = 0, to = 31) int id);

    @NotNull
    Optional<Object> getObjectValue(@Range(from = 0, to = 31) int id);

    void applyUpdate(@NotNull Collection<DataWatcherEntry> objects);
}

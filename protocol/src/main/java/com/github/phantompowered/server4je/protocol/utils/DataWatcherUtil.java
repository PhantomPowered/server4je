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

import com.github.phantompowered.server4je.api.data.DataWatcherEntry;
import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.common.math.MathHelper;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class DataWatcherUtil {

    private DataWatcherUtil() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    public static void writeList(@NotNull DataBuffer buffer, @Nullable Collection<DataWatcherEntry> objects) {
        if (objects != null) {
            for (DataWatcherEntry object : objects) {
                DataWatcherUtil.write(object, buffer);
            }
        }

        buffer.writeByte(DataWatcherEntry.END);
    }

    @NotNull
    public static Collection<DataWatcherEntry> readList(DataBuffer buffer) {
        Collection<DataWatcherEntry> objects = new ArrayList<>();
        DataWatcherEntry object;
        while ((object = DataWatcherUtil.read(buffer)) != null) {
            objects.add(object);
        }

        return objects;
    }

    private static void write(@NotNull DataWatcherEntry entry, @NotNull DataBuffer buffer) {
        Object data = entry.getValue().orElse(null);
        if (data == null) {
            return;
        }

        int i = (entry.getObjectType() << 5 | entry.getId() & 31) & 255;
        buffer.writeByte(i);

        switch (entry.getObjectType()) {
            case 0:
                buffer.writeByte((byte) data);
                break;

            case 1:
                buffer.writeShort((short) data);
                break;

            case 2:
                buffer.writeInt((int) data);
                break;

            case 3:
                buffer.writeFloat((float) data);
                break;

            case 4:
                buffer.writeString((String) data);
                break;

            case 5:
                buffer.writeItemStack((ItemStack) data);
                break;

            case 6:
                Location loc = (Location) data;
                buffer.writeInt(loc.getBlockX());
                buffer.writeInt(loc.getBlockY());
                buffer.writeInt(loc.getBlockZ());
                break;

            case 7:
                EulerAngle rotations = (EulerAngle) data;
                buffer.writeFloat(MathHelper.preventNaN(rotations.getX()));
                buffer.writeFloat(MathHelper.preventNaN(rotations.getY()));
                buffer.writeFloat(MathHelper.preventNaN(rotations.getZ()));
                break;

            default:
                ReportedException.throwWrapped(entry.getId() + " > 7");
        }
    }

    @Nullable
    private static DataWatcherEntry read(@NotNull DataBuffer buffer) {
        byte in = buffer.readByte();
        if (in == DataWatcherEntry.END) {
            return null;
        }

        int typeId = (in & 224) >> 5;
        int id = in & 31;

        Object value;
        switch (typeId) {
            case 0:
                value = buffer.readByte();
                break;

            case 1:
                value = buffer.readShort();
                break;

            case 2:
                value = buffer.readInt();
                break;

            case 3:
                value = buffer.readFloat();
                break;

            case 4:
                value = buffer.readString();
                break;

            case 5:
                value = buffer.readItemStack();
                break;

            case 6: { // hack
                int x = buffer.readInt();
                int y = buffer.readInt();
                int z = buffer.readInt();
                value = new Location(null, x, y, z);
                break;
            }

            case 7: { // hack
                float x = buffer.readFloat();
                float y = buffer.readFloat();
                float z = buffer.readFloat();
                value = new EulerAngle(x, y, z);
                break;
            }

            default:
                return null;
        }

        return new UtilDataWatcherEntry(id, typeId, value);
    }

    private static class UtilDataWatcherEntry implements DataWatcherEntry {

        private final int id;
        private final int type;
        private @Nullable Object value;

        private UtilDataWatcherEntry(int id, int type, @Nullable Object value) {
            this.id = id;
            this.type = type;
            this.value = value;
        }

        @Override
        public @Range(from = 0, to = 31) int getId() {
            return this.id;
        }

        @Override
        public @NotNull Optional<Object> getValue() {
            return Optional.ofNullable(this.value);
        }

        @Override
        public @NotNull DataWatcherEntry setValue(@Nullable Object value) {
            this.value = value;
            return this;
        }

        @Override
        public @Range(from = 0, to = 7) int getObjectType() {
            return this.type;
        }
    }
}

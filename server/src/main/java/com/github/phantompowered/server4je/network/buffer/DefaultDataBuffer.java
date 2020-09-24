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
package com.github.phantompowered.server4je.network.buffer;

import com.destroystokyo.paper.Namespaced;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DefaultDataBuffer extends DataBuffer {

    protected DefaultDataBuffer(ByteBuf dataHolder) {
        super(dataHolder);
    }

    @Override
    public void writeString(@NotNull String string) {
        this.writeString(string, Short.MAX_VALUE);
    }

    @Override
    public void writeString(@NotNull String string, @Range(from = 0, to = Short.MAX_VALUE) int maxLength) {
        Preconditions.checkArgument(string.length() <= maxLength, "string longer than %s (%s > %s)", maxLength, string.length(), maxLength);

        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        this.writeVarInt(bytes.length);
        this.writeBytes(bytes);
    }

    @Override
    @NotNull
    public String readString() {
        return this.readString(Short.MAX_VALUE);
    }

    @Override
    @NotNull
    public String readStringSilently(@Range(from = 0, to = Short.MAX_VALUE) int maxLength) {
        int length = this.readVarInt();
        byte[] bytes = new byte[length];
        this.readBytes(bytes);
        String s = new String(bytes, StandardCharsets.UTF_8);

        if (length > maxLength) {
            return s.substring(0, length);
        }

        return s;
    }

    @Override
    @NotNull
    public String readString(@Range(from = 0, to = Short.MAX_VALUE) int maxLength) {
        int length = this.readVarInt();
        Preconditions.checkArgument(length <= maxLength, "String will be longer than the maximum allowed (%s > %s)", length, maxLength);

        byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public void writeNamespaced(@NotNull Namespaced namespaced) {
        this.writeString(namespaced.getNamespace() + ':' + namespaced.getKey());
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation") // we are internal ._.
    public Namespaced readNamespaced() {
        String tag = this.readString(256);
        int indexOfSeparator = tag.indexOf(':');
        Preconditions.checkArgument(indexOfSeparator != -1, "Tag must be in format 'namespaced':'key' (%s)", tag);

        String namespaced = tag.substring(0, indexOfSeparator);
        String key = tag.substring(indexOfSeparator + 1);

        return new NamespacedKey(namespaced, key);
    }

    @Override
    public void writeByteArray(@NotNull byte[] bytes) {
        this.writeVarInt(bytes.length);
        this.writeBytes(bytes);
    }

    @Override
    public void writeByteArray(@NotNull byte[] bytes, short limit) {
        Preconditions.checkArgument(bytes.length <= limit, "Bytes are more than the maximum allowed (%s > %s)", bytes.length, limit);
        this.writeVarInt(bytes.length);
        this.writeBytes(bytes);
    }

    @Override
    @NotNull
    public byte[] readByteArray() {
        byte[] bytes = new byte[this.readVarInt()];
        this.readBytes(bytes);
        return bytes;
    }

    @Override
    public @NotNull
    byte[] readByteArray(@Range(from = 0, to = Integer.MAX_VALUE) int limit) {
        int length = this.readVarInt();
        Preconditions.checkArgument(length <= limit, "Bytes are more than the maximum allowed (%s > %s)", length, limit);

        byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return bytes;
    }

    @Override
    public void writeStringArray(@NotNull String[] strings) {
        this.writeVarInt(strings.length);
        for (String string : strings) {
            this.writeString(string);
        }
    }

    @Override
    public void writeStringArray(@NotNull String[] strings, @Range(from = 0, to = Integer.MAX_VALUE) int limit) {
        Preconditions.checkArgument(strings.length <= limit, "Strings are more than the maximum allowed (%s > %s)", strings.length, limit);
        this.writeVarInt(strings.length);
        for (String string : strings) {
            this.writeString(string);
        }
    }

    @Override
    public void writeStringArrayFixedLength(@NotNull String[] strings, @Range(from = 0, to = Integer.MAX_VALUE) int maxLengthPerString) {
        this.writeVarInt(strings.length);
        for (String string : strings) {
            this.writeString(string, maxLengthPerString);
        }
    }

    @Override
    @NotNull
    public String[] readStringArray() {
        int length = this.readVarInt();
        String[] result = new String[length];
        for (int i = 0; i < length; i++) {
            result[i] = this.readString();
        }

        return result;
    }

    @Override
    @NotNull
    public String[] readStringArray(@Range(from = 0, to = Integer.MAX_VALUE) int limit) {
        int length = this.readVarInt();
        Preconditions.checkArgument(length <= limit, "Strings are more than the maximum allowed (%s > %s)", length, limit);

        String[] result = new String[length];
        for (int i = 0; i < length; i++) {
            result[i] = this.readString();
        }

        return result;
    }

    @Override
    public void writeVarIntArray(@NotNull int[] ints) {
        this.writeVarInt(ints.length);
        for (int anInt : ints) {
            this.writeVarInt(anInt);
        }
    }

    @Override
    public int[] readVarIntArray() {
        int length = this.readVarInt();
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = this.readVarInt();
        }

        return result;
    }

    @Override
    @NotNull
    public byte[] toBytes() {
        byte[] bytes = new byte[this.readableBytes()];
        this.readBytes(bytes);
        return bytes;
    }

    @Override
    public void writeStringCollection(@NotNull Collection<String> list) {
        this.writeVarInt(list.size());
        for (String s : list) {
            this.writeString(s);
        }
    }

    @Override
    @NotNull
    public ItemStack readItemStack() {
        return null; // TODO
    }

    @Override
    public void writeItemStack(@NotNull ItemStack itemStack) {

    }

    @Override
    @NotNull
    public List<String> readStringCollection() {
        int length = this.readVarInt();
        List<String> out = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            out.add(this.readString());
        }

        return out;
    }

    @Override
    public int readVarInt() {
        return ByteBufUtil.readUnsignedVarInt(this);
    }

    @Override
    public void writeVarInt(int value) {
        ByteBufUtil.writeUnsignedVarInt(this, value);
    }

    @Override
    public long readVarLong() {
        return ByteBufUtil.readUnsignedVarLong(this);
    }

    @Override
    public void writeVarLong(long value) {
        ByteBufUtil.writeUnsignedVarLong(this, value);
    }

    @Override
    @NotNull
    public UUID readUniqueId() {
        return new UUID(this.readLong(), this.readLong());
    }

    @Override
    public void writeUniqueId(@NotNull UUID uniqueId) {
        this.writeLong(uniqueId.getMostSignificantBits());
        this.writeLong(uniqueId.getLeastSignificantBits());
    }

    @Override
    @NotNull
    public UUID readModernUniqueId() {
        return new UUID(
            (long) this.readInt() << 0x20 | (long) this.readInt() & 0xffffffffL,
            (long) this.readInt() << 0x20 | (long) this.readInt() & 0xffffffffL
        );
    }

    @Override
    public void writeModernUniqueId(@NotNull UUID uniqueId) {
        this.writeInt((int) (uniqueId.getMostSignificantBits() >> 0x20));
        this.writeInt((int) uniqueId.getMostSignificantBits());
        this.writeInt((int) (uniqueId.getLeastSignificantBits() >> 0x20));
        this.writeInt((int) uniqueId.getLeastSignificantBits());
    }
}

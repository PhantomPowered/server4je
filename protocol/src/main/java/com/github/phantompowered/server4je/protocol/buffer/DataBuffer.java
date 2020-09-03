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
package com.github.phantompowered.server4je.protocol.buffer;

import com.destroystokyo.paper.Namespaced;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class DataBuffer extends ByteBuf {

    private final ByteBuf dataHolder;

    private DataBuffer(ByteBuf dataHolder) {
        this.dataHolder = dataHolder;
    }

    public abstract void writeString(@NotNull String string);

    public abstract void writeString(@NotNull String string, @Range(from = 0, to = Short.MAX_VALUE) int maxLength);

    @NotNull
    public abstract String readString();

    @NotNull
    public abstract String readStringSilently(@Range(from = 0, to = Short.MAX_VALUE) int maxLength);

    @NotNull
    public abstract String readString(@Range(from = 0, to = Short.MAX_VALUE) int maxLength);

    public abstract void writeNamespaced(@NotNull Namespaced namespaced);

    @NotNull
    public abstract Namespaced readNamespaced();

    public abstract void writeByteArray(@NotNull byte[] bytes);

    public abstract void writeByteArray(@NotNull byte[] bytes, short limit);

    @NotNull
    public abstract byte[] readByteArray();

    @NotNull
    public abstract byte[] readByteArray(@Range(from = 0, to = Integer.MAX_VALUE) int limit);

    public abstract void writeStringArray(@NotNull String[] strings);

    public abstract void writeStringArray(@NotNull String[] strings, short limit);

    @NotNull
    public abstract String[] readStringArray();

    @NotNull
    public abstract String[] readStringArray(@Range(from = 0, to = Integer.MAX_VALUE) int limit);

    public abstract void writeVarIntArray(@NotNull int[] ints);

    public abstract int[] readVarIntArray();

    @NotNull
    public abstract byte[] toBytes();

    public abstract void writeStringCollection(@NotNull Collection<String> list);

    @NotNull
    public abstract ItemStack readItemStack();

    public abstract void writeItemStack(@NotNull ItemStack itemStack);

    @NotNull
    public abstract List<String> readStringCollection();

    public abstract int readVarInt();

    public abstract void writeVarInt(int value);

    public abstract long readVarLong();

    public abstract void writeVarLong(long value);

    @NotNull
    public abstract UUID readUniqueId();

    public abstract void writeUniqueId(@NotNull UUID uniqueId);

    @NotNull
    public abstract UUID readModernUniqueId();

    public abstract void writeModernUniqueId(@NotNull UUID uniqueId);

    @Override
    public int capacity() {
        return this.dataHolder.capacity();
    }

    @Override
    public ByteBuf capacity(int i) {
        return this.dataHolder.capacity(i);
    }

    @Override
    public int maxCapacity() {
        return this.dataHolder.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.dataHolder.alloc();
    }

    @Override
    @Deprecated
    public ByteOrder order() {
        return this.dataHolder.order();
    }

    @Override
    @Deprecated
    public ByteBuf order(ByteOrder byteOrder) {
        return this.dataHolder.order(byteOrder);
    }

    @Override
    public ByteBuf unwrap() {
        return this.dataHolder.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.dataHolder.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.dataHolder.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.dataHolder.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.dataHolder.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int i) {
        return this.dataHolder.readerIndex(i);
    }

    @Override
    public int writerIndex() {
        return this.dataHolder.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int i) {
        return this.dataHolder.writerIndex(i);
    }

    @Override
    public ByteBuf setIndex(int i, int i1) {
        return this.dataHolder.setIndex(i, i1);
    }

    @Override
    public int readableBytes() {
        return this.dataHolder.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.dataHolder.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.dataHolder.maxWritableBytes();
    }

    @Override
    public int maxFastWritableBytes() {
        return this.dataHolder.maxFastWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.dataHolder.isReadable();
    }

    @Override
    public boolean isReadable(int i) {
        return this.dataHolder.isReadable(i);
    }

    @Override
    public boolean isWritable() {
        return this.dataHolder.isWritable();
    }

    @Override
    public boolean isWritable(int i) {
        return this.dataHolder.isWritable(i);
    }

    @Override
    public ByteBuf clear() {
        return this.dataHolder.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.dataHolder.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.dataHolder.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.dataHolder.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.dataHolder.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.dataHolder.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.dataHolder.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int i) {
        return this.dataHolder.ensureWritable(i);
    }

    @Override
    public int ensureWritable(int i, boolean b) {
        return this.dataHolder.ensureWritable(i, b);
    }

    @Override
    public boolean getBoolean(int i) {
        return this.dataHolder.getBoolean(i);
    }

    @Override
    public byte getByte(int i) {
        return this.dataHolder.getByte(i);
    }

    @Override
    public short getUnsignedByte(int i) {
        return this.dataHolder.getUnsignedByte(i);
    }

    @Override
    public short getShort(int i) {
        return this.dataHolder.getShort(i);
    }

    @Override
    public short getShortLE(int i) {
        return this.dataHolder.getShortLE(i);
    }

    @Override
    public int getUnsignedShort(int i) {
        return this.dataHolder.getUnsignedShort(i);
    }

    @Override
    public int getUnsignedShortLE(int i) {
        return this.dataHolder.getUnsignedShortLE(i);
    }

    @Override
    public int getMedium(int i) {
        return this.dataHolder.getMedium(i);
    }

    @Override
    public int getMediumLE(int i) {
        return this.dataHolder.getMediumLE(i);
    }

    @Override
    public int getUnsignedMedium(int i) {
        return this.dataHolder.getUnsignedMedium(i);
    }

    @Override
    public int getUnsignedMediumLE(int i) {
        return this.dataHolder.getUnsignedMediumLE(i);
    }

    @Override
    public int getInt(int i) {
        return this.dataHolder.getInt(i);
    }

    @Override
    public int getIntLE(int i) {
        return this.dataHolder.getIntLE(i);
    }

    @Override
    public long getUnsignedInt(int i) {
        return this.dataHolder.getUnsignedInt(i);
    }

    @Override
    public long getUnsignedIntLE(int i) {
        return this.dataHolder.getUnsignedIntLE(i);
    }

    @Override
    public long getLong(int i) {
        return this.dataHolder.getLong(i);
    }

    @Override
    public long getLongLE(int i) {
        return this.dataHolder.getLongLE(i);
    }

    @Override
    public char getChar(int i) {
        return this.dataHolder.getChar(i);
    }

    @Override
    public float getFloat(int i) {
        return this.dataHolder.getFloat(i);
    }

    @Override
    public float getFloatLE(int index) {
        return this.dataHolder.getFloatLE(index);
    }

    @Override
    public double getDouble(int i) {
        return this.dataHolder.getDouble(i);
    }

    @Override
    public double getDoubleLE(int index) {
        return this.dataHolder.getDoubleLE(index);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf byteBuf) {
        return this.dataHolder.getBytes(i, byteBuf);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf byteBuf, int i1) {
        return this.dataHolder.getBytes(i, byteBuf, i1);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuf byteBuf, int i1, int i2) {
        return this.dataHolder.getBytes(i, byteBuf, i1, i2);
    }

    @Override
    public ByteBuf getBytes(int i, byte[] bytes) {
        return this.dataHolder.getBytes(i, bytes);
    }

    @Override
    public ByteBuf getBytes(int i, byte[] bytes, int i1, int i2) {
        return this.dataHolder.getBytes(i, bytes, i1, i2);
    }

    @Override
    public ByteBuf getBytes(int i, ByteBuffer byteBuffer) {
        return this.dataHolder.getBytes(i, byteBuffer);
    }

    @Override
    public ByteBuf getBytes(int i, OutputStream outputStream, int i1) throws IOException {
        return this.dataHolder.getBytes(i, outputStream, i1);
    }

    @Override
    public int getBytes(int i, GatheringByteChannel gatheringByteChannel, int i1) throws IOException {
        return this.dataHolder.getBytes(i, gatheringByteChannel, i1);
    }

    @Override
    public int getBytes(int i, FileChannel fileChannel, long l, int i1) throws IOException {
        return this.dataHolder.getBytes(i, fileChannel, l, i1);
    }

    @Override
    public CharSequence getCharSequence(int i, int i1, Charset charset) {
        return this.dataHolder.getCharSequence(i, i1, charset);
    }

    @Override
    public ByteBuf setBoolean(int i, boolean b) {
        return this.dataHolder.setBoolean(i, b);
    }

    @Override
    public ByteBuf setByte(int i, int i1) {
        return this.dataHolder.setByte(i, i1);
    }

    @Override
    public ByteBuf setShort(int i, int i1) {
        return this.dataHolder.setShort(i, i1);
    }

    @Override
    public ByteBuf setShortLE(int i, int i1) {
        return this.dataHolder.setShortLE(i, i1);
    }

    @Override
    public ByteBuf setMedium(int i, int i1) {
        return this.dataHolder.setMedium(i, i1);
    }

    @Override
    public ByteBuf setMediumLE(int i, int i1) {
        return this.dataHolder.setMediumLE(i, i1);
    }

    @Override
    public ByteBuf setInt(int i, int i1) {
        return this.dataHolder.setInt(i, i1);
    }

    @Override
    public ByteBuf setIntLE(int i, int i1) {
        return this.dataHolder.setIntLE(i, i1);
    }

    @Override
    public ByteBuf setLong(int i, long l) {
        return this.dataHolder.setLong(i, l);
    }

    @Override
    public ByteBuf setLongLE(int i, long l) {
        return this.dataHolder.setLongLE(i, l);
    }

    @Override
    public ByteBuf setChar(int i, int i1) {
        return this.dataHolder.setChar(i, i1);
    }

    @Override
    public ByteBuf setFloat(int i, float v) {
        return this.dataHolder.setFloat(i, v);
    }

    @Override
    public ByteBuf setFloatLE(int index, float value) {
        return this.dataHolder.setFloatLE(index, value);
    }

    @Override
    public ByteBuf setDouble(int i, double v) {
        return this.dataHolder.setDouble(i, v);
    }

    @Override
    public ByteBuf setDoubleLE(int index, double value) {
        return this.dataHolder.setDoubleLE(index, value);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf byteBuf) {
        return this.dataHolder.setBytes(i, byteBuf);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf byteBuf, int i1) {
        return this.dataHolder.setBytes(i, byteBuf, i1);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuf byteBuf, int i1, int i2) {
        return this.dataHolder.setBytes(i, byteBuf, i1, i2);
    }

    @Override
    public ByteBuf setBytes(int i, byte[] bytes) {
        return this.dataHolder.setBytes(i, bytes);
    }

    @Override
    public ByteBuf setBytes(int i, byte[] bytes, int i1, int i2) {
        return this.dataHolder.setBytes(i, bytes, i1, i2);
    }

    @Override
    public ByteBuf setBytes(int i, ByteBuffer byteBuffer) {
        return this.dataHolder.setBytes(i, byteBuffer);
    }

    @Override
    public int setBytes(int i, InputStream inputStream, int i1) throws IOException {
        return this.dataHolder.setBytes(i, inputStream, i1);
    }

    @Override
    public int setBytes(int i, ScatteringByteChannel scatteringByteChannel, int i1) throws IOException {
        return this.dataHolder.setBytes(i, scatteringByteChannel, i1);
    }

    @Override
    public int setBytes(int i, FileChannel fileChannel, long l, int i1) throws IOException {
        return this.dataHolder.setBytes(i, fileChannel, l, i1);
    }

    @Override
    public ByteBuf setZero(int i, int i1) {
        return this.dataHolder.setZero(i, i1);
    }

    @Override
    public int setCharSequence(int i, CharSequence charSequence, Charset charset) {
        return this.dataHolder.setCharSequence(i, charSequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return this.dataHolder.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.dataHolder.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.dataHolder.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.dataHolder.readShort();
    }

    @Override
    public short readShortLE() {
        return this.dataHolder.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.dataHolder.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.dataHolder.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.dataHolder.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.dataHolder.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.dataHolder.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.dataHolder.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.dataHolder.readInt();
    }

    @Override
    public int readIntLE() {
        return this.dataHolder.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.dataHolder.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.dataHolder.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.dataHolder.readLong();
    }

    @Override
    public long readLongLE() {
        return this.dataHolder.readLongLE();
    }

    @Override
    public char readChar() {
        return this.dataHolder.readChar();
    }

    @Override
    public float readFloat() {
        return this.dataHolder.readFloat();
    }

    @Override
    public float readFloatLE() {
        return this.dataHolder.readFloatLE();
    }

    @Override
    public double readDouble() {
        return this.dataHolder.readDouble();
    }

    @Override
    public double readDoubleLE() {
        return this.dataHolder.readDoubleLE();
    }

    @Override
    public ByteBuf readBytes(int i) {
        return this.dataHolder.readBytes(i);
    }

    @Override
    public ByteBuf readSlice(int i) {
        return this.dataHolder.readSlice(i);
    }

    @Override
    public ByteBuf readRetainedSlice(int i) {
        return this.dataHolder.readRetainedSlice(i);
    }

    @Override
    public ByteBuf readBytes(ByteBuf byteBuf) {
        return this.dataHolder.readBytes(byteBuf);
    }

    @Override
    public ByteBuf readBytes(ByteBuf byteBuf, int i) {
        return this.dataHolder.readBytes(byteBuf, i);
    }

    @Override
    public ByteBuf readBytes(ByteBuf byteBuf, int i, int i1) {
        return this.dataHolder.readBytes(byteBuf, i, i1);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes) {
        return this.dataHolder.readBytes(bytes);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes, int i, int i1) {
        return this.dataHolder.readBytes(bytes, i, i1);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer byteBuffer) {
        return this.dataHolder.readBytes(byteBuffer);
    }

    @Override
    public ByteBuf readBytes(OutputStream outputStream, int i) throws IOException {
        return this.dataHolder.readBytes(outputStream, i);
    }

    @Override
    public int readBytes(GatheringByteChannel gatheringByteChannel, int i) throws IOException {
        return this.dataHolder.readBytes(gatheringByteChannel, i);
    }

    @Override
    public CharSequence readCharSequence(int i, Charset charset) {
        return this.dataHolder.readCharSequence(i, charset);
    }

    @Override
    public int readBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return this.dataHolder.readBytes(fileChannel, l, i);
    }

    @Override
    public ByteBuf skipBytes(int i) {
        return this.dataHolder.skipBytes(i);
    }

    @Override
    public ByteBuf writeBoolean(boolean b) {
        return this.dataHolder.writeBoolean(b);
    }

    @Override
    public ByteBuf writeByte(int i) {
        return this.dataHolder.writeByte(i);
    }

    @Override
    public ByteBuf writeShort(int i) {
        return this.dataHolder.writeShort(i);
    }

    @Override
    public ByteBuf writeShortLE(int i) {
        return this.dataHolder.writeShortLE(i);
    }

    @Override
    public ByteBuf writeMedium(int i) {
        return this.dataHolder.writeMedium(i);
    }

    @Override
    public ByteBuf writeMediumLE(int i) {
        return this.dataHolder.writeMediumLE(i);
    }

    @Override
    public ByteBuf writeInt(int i) {
        return this.dataHolder.writeInt(i);
    }

    @Override
    public ByteBuf writeIntLE(int i) {
        return this.dataHolder.writeIntLE(i);
    }

    @Override
    public ByteBuf writeLong(long l) {
        return this.dataHolder.writeLong(l);
    }

    @Override
    public ByteBuf writeLongLE(long l) {
        return this.dataHolder.writeLongLE(l);
    }

    @Override
    public ByteBuf writeChar(int i) {
        return this.dataHolder.writeChar(i);
    }

    @Override
    public ByteBuf writeFloat(float v) {
        return this.dataHolder.writeFloat(v);
    }

    @Override
    public ByteBuf writeFloatLE(float value) {
        return this.dataHolder.writeFloatLE(value);
    }

    @Override
    public ByteBuf writeDouble(double v) {
        return this.dataHolder.writeDouble(v);
    }

    @Override
    public ByteBuf writeDoubleLE(double value) {
        return this.dataHolder.writeDoubleLE(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf byteBuf) {
        return this.dataHolder.writeBytes(byteBuf);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf byteBuf, int i) {
        return this.dataHolder.writeBytes(byteBuf, i);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf byteBuf, int i, int i1) {
        return this.dataHolder.writeBytes(byteBuf, i, i1);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes) {
        return this.dataHolder.writeBytes(bytes);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes, int i, int i1) {
        return this.dataHolder.writeBytes(bytes, i, i1);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer byteBuffer) {
        return this.dataHolder.writeBytes(byteBuffer);
    }

    @Override
    public int writeBytes(InputStream inputStream, int i) throws IOException {
        return this.dataHolder.writeBytes(inputStream, i);
    }

    @Override
    public int writeBytes(ScatteringByteChannel scatteringByteChannel, int i) throws IOException {
        return this.dataHolder.writeBytes(scatteringByteChannel, i);
    }

    @Override
    public int writeBytes(FileChannel fileChannel, long l, int i) throws IOException {
        return this.dataHolder.writeBytes(fileChannel, l, i);
    }

    @Override
    public ByteBuf writeZero(int i) {
        return this.dataHolder.writeZero(i);
    }

    @Override
    public int writeCharSequence(CharSequence charSequence, Charset charset) {
        return this.dataHolder.writeCharSequence(charSequence, charset);
    }

    @Override
    public int indexOf(int i, int i1, byte b) {
        return this.dataHolder.indexOf(i, i1, b);
    }

    @Override
    public int bytesBefore(byte b) {
        return this.dataHolder.bytesBefore(b);
    }

    @Override
    public int bytesBefore(int i, byte b) {
        return this.dataHolder.bytesBefore(i, b);
    }

    @Override
    public int bytesBefore(int i, int i1, byte b) {
        return this.dataHolder.bytesBefore(i, i1, b);
    }

    @Override
    public int forEachByte(ByteProcessor byteProcessor) {
        return this.dataHolder.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int i, int i1, ByteProcessor byteProcessor) {
        return this.dataHolder.forEachByte(i, i1, byteProcessor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.dataHolder.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int i, int i1, ByteProcessor byteProcessor) {
        return this.dataHolder.forEachByteDesc(i, i1, byteProcessor);
    }

    @Override
    public ByteBuf copy() {
        return this.dataHolder.copy();
    }

    @Override
    public ByteBuf copy(int i, int i1) {
        return this.dataHolder.copy(i, i1);
    }

    @Override
    public ByteBuf slice() {
        return this.dataHolder.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.dataHolder.retainedSlice();
    }

    @Override
    public ByteBuf slice(int i, int i1) {
        return this.dataHolder.slice(i, i1);
    }

    @Override
    public ByteBuf retainedSlice(int i, int i1) {
        return this.dataHolder.retainedSlice(i, i1);
    }

    @Override
    public ByteBuf duplicate() {
        return this.dataHolder.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.dataHolder.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.dataHolder.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.dataHolder.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int i, int i1) {
        return this.dataHolder.nioBuffer(i, i1);
    }

    @Override
    public ByteBuffer internalNioBuffer(int i, int i1) {
        return this.dataHolder.internalNioBuffer(i, i1);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.dataHolder.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int i, int i1) {
        return this.dataHolder.nioBuffers(i, i1);
    }

    @Override
    public boolean hasArray() {
        return this.dataHolder.hasArray();
    }

    @Override
    public byte[] array() {
        return this.dataHolder.array();
    }

    @Override
    public int arrayOffset() {
        return this.dataHolder.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.dataHolder.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.dataHolder.memoryAddress();
    }

    @Override
    public boolean isContiguous() {
        return this.dataHolder.isContiguous();
    }

    @Override
    public String toString(Charset charset) {
        return this.dataHolder.toString(charset);
    }

    @Override
    public String toString(int i, int i1, Charset charset) {
        return this.dataHolder.toString(i, i1, charset);
    }

    @Override
    public int hashCode() {
        return this.dataHolder.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof DataBuffer)) {
            return false;
        }

        var buffer = (DataBuffer) o;
        return buffer.dataHolder.equals(this.dataHolder);
    }

    @Override
    public int compareTo(ByteBuf byteBuf) {
        return this.dataHolder.compareTo(byteBuf);
    }

    @Override
    public String toString() {
        return this.dataHolder.toString();
    }

    @Override
    public ByteBuf retain(int i) {
        return this.dataHolder.retain(i);
    }

    @Override
    public ByteBuf retain() {
        return this.dataHolder.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.dataHolder.touch();
    }

    @Override
    public ByteBuf touch(Object o) {
        return this.dataHolder.touch(o);
    }

    @Override
    public int refCnt() {
        return this.dataHolder.refCnt();
    }

    @Override
    public boolean release() {
        return this.dataHolder.release();
    }

    @Override
    public boolean release(int i) {
        return this.dataHolder.release(i);
    }
}

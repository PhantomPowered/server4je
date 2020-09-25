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
package com.github.phantompowered.server4je.compression;

import com.github.phantompowered.server4je.util.DefaultCloseable;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaCompressor extends DefaultCloseable implements Compressor {

    private final Inflater inflater;
    private final Deflater deflater;

    public JavaCompressor(int level) {
        this.inflater = new Inflater();
        this.deflater = new Deflater(level);
    }

    @Override
    public void inflate(@NotNull ByteBuf source, @NotNull ByteBuf target, int size) throws DataFormatException {
        this.ensureNotClosed();

        Preconditions.checkArgument(source.nioBufferCount() == 1, "Cannot deal with multiple nio buffers");
        Preconditions.checkArgument(target.nioBufferCount() == 1, "Cannot deal with multiple nio buffers");

        try {
            final int originalReaderIndex = source.readerIndex();
            this.inflater.setInput(source.nioBuffer());

            while (!this.inflater.finished() && this.inflater.getBytesRead() < source.readableBytes()) {
                if (!target.isWritable()) {
                    if (target.readableBytes() > size) {
                        throw new DataFormatException("Too much data input for max size " + size);
                    }

                    target.ensureWritable(8192); // magic value
                }

                ByteBuffer buffer = target.nioBuffer(target.writerIndex(), target.writableBytes());
                final int uncompressedBytes = this.inflater.inflate(buffer);

                source.readerIndex(originalReaderIndex + this.inflater.getTotalIn());
                target.writerIndex(target.writerIndex() + uncompressedBytes);
            }

            this.inflater.reset();
        } catch (Throwable throwable) {
            if (throwable instanceof DataFormatException) {
                // We should explicitly rethrow this to let the caller now that some of the
                // compressed shit he gave us is wrong
                throw (DataFormatException) throwable;
            }

            throw new RuntimeException(throwable);
        }
    }

    @Override
    public void deflate(@NotNull ByteBuf source, @NotNull ByteBuf target) throws DataFormatException {
        this.ensureNotClosed();

        Preconditions.checkArgument(source.nioBufferCount() == 1, "Cannot deal with multiple nio buffers");
        Preconditions.checkArgument(target.nioBufferCount() == 1, "Cannot deal with multiple nio buffers");

        try {
            final int originalReaderIndex = source.readerIndex();
            this.deflater.setInput(source.nioBuffer());

            while (!this.deflater.finished()) {
                if (!target.isWritable()) {
                    target.ensureWritable(8192); // magic value
                }

                ByteBuffer buffer = target.nioBuffer(target.writerIndex(), target.writableBytes());
                final int compressedBytes = this.deflater.deflate(buffer);

                source.readerIndex(originalReaderIndex + this.deflater.getTotalIn());
                target.writerIndex(target.writerIndex() + compressedBytes);
            }

            this.deflater.reset();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    protected void close0() {
        this.inflater.end();
        this.deflater.end();
    }

    @Override
    public boolean isNative() {
        return false;
    }
}

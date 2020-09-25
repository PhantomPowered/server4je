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

import java.util.zip.DataFormatException;

public class NativeCompressorWrapper extends DefaultCloseable implements Compressor {

    private final NativeCompressor inflater;
    private final long inflaterCtx;
    private final NativeCompressor deflater;
    private final long deflaterCtx;

    public NativeCompressorWrapper(int level) {
        level = level == -1 ? 6 : level;
        Preconditions.checkArgument(level <= 12 && level >= 1, "Invalid native compression level " + level);

        this.inflater = new NativeCompressor();
        this.deflater = new NativeCompressor();

        this.inflaterCtx = this.inflater.init(true, -1);
        this.deflaterCtx = this.deflater.init(false, level);
    }

    @Override
    public void inflate(@NotNull ByteBuf source, @NotNull ByteBuf target, int size) throws DataFormatException {
        this.ensureNotClosed();

        while (!this.inflater.isFinished()) {
            this.process(source, target, this.inflater);
        }

        this.inflater.reset(true, this.inflaterCtx);
    }

    @Override
    public void deflate(@NotNull ByteBuf source, @NotNull ByteBuf target) throws DataFormatException {
        this.ensureNotClosed();

        while (!this.deflater.isFinished() && source.isReadable()) {
            this.process(source, target, this.deflater);
        }

        this.deflater.reset(false, this.deflaterCtx);
    }

    private void process(@NotNull ByteBuf source, @NotNull ByteBuf target, @NotNull NativeCompressor compressor) throws DataFormatException {
        final long sourceAddress = source.memoryAddress() + source.readerIndex();
        final long targetAddress = target.memoryAddress() + target.writerIndex();

        target.ensureWritable(8192);
        int decompressed = compressor.process(false, this.deflaterCtx, sourceAddress, source.readableBytes(), targetAddress, target.writableBytes());

        source.readerIndex(source.readerIndex() + compressor.getProcessed());
        target.writerIndex(target.writerIndex() + decompressed);
    }

    @Override
    protected void close0() {
        this.inflater.free(true, this.inflaterCtx);
        this.deflater.free(false, this.deflaterCtx);
    }

    @Override
    public boolean isNative() {
        return true;
    }
}

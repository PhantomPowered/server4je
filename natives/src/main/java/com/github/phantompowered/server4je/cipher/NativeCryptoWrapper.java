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
package com.github.phantompowered.server4je.cipher;

import com.github.phantompowered.server4je.util.DefaultCloseable;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;

public class NativeCryptoWrapper extends DefaultCloseable implements Crypto {

    private final NativeCrypto nativeCrypto = new NativeCrypto();
    private final long ctx;

    public NativeCryptoWrapper(boolean encrypt, SecretKey secretKey) {
        this.ctx = this.nativeCrypto.init(encrypt, secretKey.getEncoded());
    }

    @Override
    public void process(@NotNull ByteBuf source) {
        this.ensureNotClosed();

        final int length = source.readableBytes();
        if (length <= 0) {
            return;
        }

        final long begin = source.memoryAddress() + source.readerIndex();
        this.nativeCrypto.process(this.ctx, begin, begin, source.readableBytes());
    }

    @Override
    public boolean isNative() {
        return true;
    }

    @Override
    protected void close0() {
        this.nativeCrypto.free(this.ctx);
    }
}

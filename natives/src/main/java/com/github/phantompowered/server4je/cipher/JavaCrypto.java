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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

public class JavaCrypto extends DefaultCloseable implements Crypto {

    private final Cipher cipher;

    public JavaCrypto(boolean encrypt, SecretKey secretKey) throws GeneralSecurityException {
        this.cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        this.cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(secretKey.getEncoded()));
    }

    @Override
    public void process(@NotNull ByteBuf source) {
        this.ensureNotClosed();

        final int inputReadableBytes = source.readableBytes();
        final byte[] bufAsArray = source.hasArray() ? source.array() : this.bufferToArray(source);

        try {
            this.cipher.update(bufAsArray, 0, inputReadableBytes, bufAsArray);
        } catch (ShortBufferException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    protected void close0() {
    }

    private byte[] bufferToArray(@NotNull ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}

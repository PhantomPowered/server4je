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
package com.github.phantompowered.server4je.network;

import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;

public final class NetworkConstants {

    public static final String READ_TIMEOUT = "read-timeout";
    public static final String LEGACY_DECODER = "legacy-decoder";
    public static final String VAR_INT_21_FRAME_DECODER = "var-int-21-frame-decoder";
    public static final String LEGACY_ENCODER = "legacy-encoder";
    public static final String VAR_INT_21_FRAME_ENCODER = "var-int-21-frame-encoder";
    public static final String PACKET_DECODER = "packet-decoder";
    public static final String PACKET_ENCODER = "packet-encoder";
    // lazy handlers
    public static final String PACKET_DECOMPRESSOR = "packet-decompressor";
    public static final String PACKET_COMPRESSOR = "packet-compressor";

    private NetworkConstants() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }
}

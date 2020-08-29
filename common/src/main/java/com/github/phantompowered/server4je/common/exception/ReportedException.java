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
package com.github.phantompowered.server4je.common.exception;

import org.jetbrains.annotations.NotNull;

public class ReportedException extends RuntimeException {

    private static final ReportedException INSTANCE = new ReportedException();

    @NotNull
    public static ReportedException forThrowable(@NotNull Throwable throwable) {
        return new ReportedException(throwable);
    }

    @NotNull
    public static ReportedException forMessage(@NotNull String message) {
        return new ReportedException(message);
    }

    @NotNull
    public static ReportedException forMessageAndThrowable(@NotNull Throwable throwable, @NotNull String message) {
        return new ReportedException(message, throwable);
    }

    @NotNull
    public static ReportedException of() {
        return ReportedException.INSTANCE;
    }

    public static void throwWrapped(@NotNull Throwable throwable) {
        throw ReportedException.forThrowable(throwable);
    }

    public static void throwWrapped(@NotNull String message) {
        throw ReportedException.forMessage(message);
    }

    public static void throwWrapped(@NotNull Throwable throwable, @NotNull String message) {
        throw ReportedException.forMessageAndThrowable(throwable, message);
    }

    public static ReportedException throwEmpty() {
        throw ReportedException.of();
    }

    private ReportedException() {
    }

    private ReportedException(String message) {
        super(message);
    }

    private ReportedException(String message, Throwable cause) {
        super(message, cause);
    }

    private ReportedException(Throwable cause) {
        super(cause);
    }
}

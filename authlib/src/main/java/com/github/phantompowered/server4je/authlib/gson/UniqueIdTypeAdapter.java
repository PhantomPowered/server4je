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
package com.github.phantompowered.server4je.authlib.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

public class UniqueIdTypeAdapter extends TypeAdapter<UUID> {

    private static final String UUID_REGEX = "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})";
    private static final String UUID_REPLACEMENT = "$1-$2-$3-$4-$5";

    @Override
    public void write(JsonWriter jsonWriter, UUID uuid) throws IOException {
        jsonWriter.value(uuid.toString().replace("-", ""));
    }

    @Override
    public UUID read(JsonReader jsonReader) throws IOException {
        return UniqueIdTypeAdapter.fromString(jsonReader.nextString());
    }

    @NotNull
    public static UUID fromString(@NotNull String in) {
        return UUID.fromString(in.replaceFirst(UUID_REGEX, UUID_REPLACEMENT));
    }
}

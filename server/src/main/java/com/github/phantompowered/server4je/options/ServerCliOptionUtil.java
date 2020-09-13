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
package com.github.phantompowered.server4je.options;

import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import com.github.phantompowered.server4je.common.misc.KeyValueHolder;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class ServerCliOptionUtil {

    private ServerCliOptionUtil() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    @NotNull
    public static KeyValueHolder<OptionSet, OptionParser> parseOptions(@NotNull String[] args) {
        OptionParser optionParser = new OptionParser();

        // Server port
        optionParser.acceptsAll(asList("server-port", "port", "p"), "Set the server port")
            .withRequiredArg()
            .ofType(Integer.class)
            .defaultsTo(25565)
            .describedAs("Server port");

        // Server ip
        optionParser.acceptsAll(asList("server-host", "host", "h"), "Sets the server host")
            .withRequiredArg()
            .ofType(String.class)
            .defaultsTo("127.0.0.1")
            .describedAs("Server host");

        // Online mode
        optionParser.acceptsAll(asList("authentication", "auth", "online-mode", "a"), "Weather the server is in only mode or not")
            .withRequiredArg()
            .ofType(Boolean.class)
            .defaultsTo(true)
            .describedAs("Server online mode");

        // Config file location
        optionParser.acceptsAll(asList("configuration", "config", "c"), "Sets the path of the configuration file to use")
            .withRequiredArg()
            .ofType(File.class)
            .defaultsTo(new File("configuration.json"))
            .describedAs("Server configuration file");

        optionParser.acceptsAll(asList("help", "?"), "Shows the help").withOptionalArg();
        optionParser.acceptsAll(asList("version", "v"), "Shows the current version").withOptionalArg();

        return KeyValueHolder.of(optionParser.parse(args), optionParser);
    }

    @NotNull
    public static <V> Optional<V> getOption(@NotNull OptionSet optionSet, @NotNull String key, @NotNull Function<String, V> mapper, @Nullable V defaultValue) {
        if (optionSet.has(key)) {
            return Optional.ofNullable(mapper.apply(String.valueOf(optionSet.valueOf(key))));
        }

        return Optional.ofNullable(defaultValue);
    }
}

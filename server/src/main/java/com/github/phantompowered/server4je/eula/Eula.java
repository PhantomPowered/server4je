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
package com.github.phantompowered.server4je.eula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Eula {

    private static final Logger LOGGER = LoggerFactory.getLogger(Eula.class);
    private static final Path EULA_PATH = Path.of("eula.txt");

    private Eula() {
        throw new UnsupportedOperationException();
    }

    public static boolean loadEula() {
        if (Boolean.getBoolean("com.mojang.eula.agree")) {
            LOGGER.warn("You used the overriding eula agreement flag.");
            LOGGER.warn("By using this setting you are indicating your agreement to Mojang's EULA (https://account.mojang.com/documents/minecraft_eula).");
            LOGGER.warn("If you are not agreeing to the eula, stop the server and remove the flag immediately.");
            return true;
        }

        Properties properties = new Properties();
        if (Files.notExists(EULA_PATH)) {
            properties.setProperty("eula", "false");
            try (var stream = Files.newOutputStream(EULA_PATH)) {
                properties.store(stream, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).");
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to store eula file", exception);
            }

            return false;
        }

        try (var stream = Files.newInputStream(EULA_PATH)) {
            properties.load(stream);
            return properties.containsKey("eula") && Boolean.parseBoolean(properties.getProperty("eula"));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read eula file", exception);
        }
    }
}

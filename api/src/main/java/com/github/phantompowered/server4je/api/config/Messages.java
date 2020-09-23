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
package com.github.phantompowered.server4je.api.config;

public final class Messages {

    private final String shutdownMessage;
    private final String serverFullKickMessage;
    private final String outdatedServerVersionKickMessage;
    private final String noPermissionMessage;

    private final String messageOfTheDay;
    private final String serverModName;

    public Messages() {
        this.shutdownMessage = "§cServer shutdown.";
        this.serverFullKickMessage = "§cThe server has exceeded the player limit of {0} players. Try again later";
        this.outdatedServerVersionKickMessage = "§cIncompatible client version! Please use {0}";
        this.noPermissionMessage = "§cYou do not have the needed permission to execute this command!";
        this.messageOfTheDay = "§6Another server4je public server!\n§7Here could be your advertisement!";
        this.serverModName = "server4je";
    }

    public String getShutdownMessage() {
        return this.shutdownMessage;
    }

    public String getServerFullKickMessage() {
        return this.serverFullKickMessage;
    }

    public String getOutdatedServerVersionKickMessage() {
        return this.outdatedServerVersionKickMessage;
    }

    public String getNoPermissionMessage() {
        return this.noPermissionMessage;
    }

    public String getMessageOfTheDay() {
        return this.messageOfTheDay;
    }

    public String getServerModName() {
        return this.serverModName;
    }
}

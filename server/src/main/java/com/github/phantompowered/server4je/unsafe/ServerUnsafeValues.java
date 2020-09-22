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
package com.github.phantompowered.server4je.unsafe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;

@Deprecated
@ApiStatus.Internal
public final class ServerUnsafeValues implements UnsafeValues {

    public static final UnsafeValues INSTANCE = new ServerUnsafeValues();
    private static final List<String> SUPPORTED_API = Arrays.asList("1.13", "1.14", "1.15", "1.16");

    private ServerUnsafeValues() {
    }

    @Override
    public void reportTimings() {

    }

    @Override
    public Material toLegacy(Material material) {
        return material;
    }

    @Override
    public Material fromLegacy(Material material) {
        return material;
    }

    @Override
    public Material fromLegacy(MaterialData materialData) {
        return materialData.getItemType();
    }

    @Override
    public Material fromLegacy(MaterialData materialData, boolean b) {
        return materialData.getItemType();
    }

    @Override
    public BlockData fromLegacy(Material material, byte b) {
        return null;
    }

    @Override
    public Material getMaterial(String s, int i) {
        return null;
    }

    @Override
    public int getDataVersion() {
        return 0;
    }

    @Override
    public ItemStack modifyItemStack(ItemStack itemStack, String s) {
        return null;
    }

    @Override
    public void checkSupported(PluginDescriptionFile pluginDescriptionFile) throws InvalidPluginException {
        // assume supported
    }

    @Override
    public byte[] processClass(PluginDescriptionFile pluginDescriptionFile, String s, byte[] bytes) {
        return bytes;
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey namespacedKey, String s) {
        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public String getTimingsServerName() {
        return Bukkit.getName();
    }

    @Override
    public boolean isSupportedApiVersion(String s) {
        return SUPPORTED_API.contains(s);
    }

    @Override
    public byte[] serializeItem(ItemStack itemStack) {
        return new byte[0];
    }

    @Override
    public ItemStack deserializeItem(byte[] bytes) {
        return null;
    }

    @Override
    public String getTranslationKey(Material material) {
        return null;
    }

    @Override
    public String getTranslationKey(Block block) {
        return null;
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        return null;
    }
}

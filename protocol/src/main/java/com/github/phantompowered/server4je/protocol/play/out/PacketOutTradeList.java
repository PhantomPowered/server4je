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
package com.github.phantompowered.server4je.protocol.play.out;

import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;

public class PacketOutTradeList implements Packet {

    private int windowId;
    private Collection<MerchantRecipe> recipes;
    private int villagerLevel;
    private int experience;
    private boolean regularVillager;
    private boolean canRestock;

    public PacketOutTradeList() {
    }

    public PacketOutTradeList(int windowId, Collection<MerchantRecipe> recipes, int villagerLevel, int experience, boolean regularVillager, boolean canRestock) {
        this.windowId = windowId;
        this.recipes = recipes;
        this.villagerLevel = villagerLevel;
        this.experience = experience;
        this.regularVillager = regularVillager;
        this.canRestock = canRestock;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.windowId);

        dataBuffer.writeByte(this.recipes.size() & 255);
        for (MerchantRecipe recipe : this.recipes) {
            dataBuffer.writeItemStack(recipe.getIngredients().get(0));
            dataBuffer.writeItemStack(recipe.getResult());
            dataBuffer.writeBoolean(recipe.getIngredients().size() > 1);
            if (recipe.getIngredients().size() > 1) {
                dataBuffer.writeItemStack(recipe.getIngredients().get(1));
            }

            dataBuffer.writeBoolean(recipe.getUses() >= recipe.getMaxUses());
            dataBuffer.writeInt(recipe.getUses());
            dataBuffer.writeInt(recipe.getMaxUses());
            dataBuffer.writeInt(recipe.getVillagerExperience());
            dataBuffer.writeInt(0); // TODO: (special price) api for this?
            dataBuffer.writeFloat(recipe.getPriceMultiplier());
            dataBuffer.writeInt(0); // TODO: (demand) api for this?
        }

        dataBuffer.writeVarInt(this.villagerLevel);
        dataBuffer.writeVarInt(this.experience);
        dataBuffer.writeBoolean(this.regularVillager);
        dataBuffer.writeBoolean(this.canRestock);
    }

    @Override
    public void releaseData() {
        this.recipes = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutTradeList.class);
    }

    public int getWindowId() {
        return this.windowId;
    }

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    public Collection<MerchantRecipe> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(Collection<MerchantRecipe> recipes) {
        this.recipes = recipes;
    }

    public int getVillagerLevel() {
        return this.villagerLevel;
    }

    public void setVillagerLevel(int villagerLevel) {
        this.villagerLevel = villagerLevel;
    }

    public int getExperience() {
        return this.experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public boolean isRegularVillager() {
        return this.regularVillager;
    }

    public void setRegularVillager(boolean regularVillager) {
        this.regularVillager = regularVillager;
    }

    public boolean isCanRestock() {
        return this.canRestock;
    }

    public void setCanRestock(boolean canRestock) {
        this.canRestock = canRestock;
    }
}

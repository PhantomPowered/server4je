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

import com.destroystokyo.paper.Namespaced;
import com.github.phantompowered.server4je.common.enums.EnumUtil;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.play.in.PacketInSetRecipeBookState;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Map;

public class PacketOutDeclareRecipes implements Packet {

    private Action action;
    private Collection<Namespaced> recipes;
    private Collection<Namespaced> displayedRecipes;
    private Map<PacketInSetRecipeBookState.RecipeBookType, RecipeData> recipeDataMap;

    public PacketOutDeclareRecipes() {
    }

    public PacketOutDeclareRecipes(Action action, Collection<Namespaced> recipes, Map<PacketInSetRecipeBookState.RecipeBookType, RecipeData> recipeDataMap) {
        Preconditions.checkArgument(action == Action.INIT);
        this.action = action;
        this.recipes = recipes;
        this.recipeDataMap = recipeDataMap;
    }

    public PacketOutDeclareRecipes(Action action, Collection<Namespaced> recipes,
                                   Collection<Namespaced> displayedRecipes, Map<PacketInSetRecipeBookState.RecipeBookType, RecipeData> recipeDataMap) {
        this.action = action;
        this.recipes = recipes;
        this.displayedRecipes = displayedRecipes;
        this.recipeDataMap = recipeDataMap;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeVarInt(this.action.ordinal());

        for (PacketInSetRecipeBookState.RecipeBookType entry : EnumUtil.getEnumEntries(PacketInSetRecipeBookState.RecipeBookType.class)) {
            RecipeData recipeData = this.recipeDataMap.get(entry);
            if (recipeData == null) {
                dataBuffer.writeBoolean(Boolean.FALSE);
                dataBuffer.writeBoolean(Boolean.FALSE);
            } else {
                dataBuffer.writeBoolean(recipeData.open);
                dataBuffer.writeBoolean(recipeData.filtering);
            }
        }

        dataBuffer.writeVarInt(this.recipes.size());
        for (Namespaced recipe : this.recipes) {
            dataBuffer.writeNamespaced(recipe);
        }

        if (this.action == Action.INIT) {
            dataBuffer.writeVarInt(this.displayedRecipes.size());
            for (Namespaced displayedRecipe : this.displayedRecipes) {
                dataBuffer.writeNamespaced(displayedRecipe);
            }
        }
    }

    @Override
    public void releaseData() {
        this.action = null;
        this.recipes = null;
        this.displayedRecipes = null;
        this.recipeDataMap = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutDeclareRecipes.class);
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Collection<Namespaced> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(Collection<Namespaced> recipes) {
        this.recipes = recipes;
    }

    public Collection<Namespaced> getDisplayedRecipes() {
        return this.displayedRecipes;
    }

    public void setDisplayedRecipes(Collection<Namespaced> displayedRecipes) {
        this.displayedRecipes = displayedRecipes;
    }

    public Map<PacketInSetRecipeBookState.RecipeBookType, RecipeData> getRecipeDataMap() {
        return this.recipeDataMap;
    }

    public void setRecipeDataMap(Map<PacketInSetRecipeBookState.RecipeBookType, RecipeData> recipeDataMap) {
        this.recipeDataMap = recipeDataMap;
    }

    public enum Action {

        INIT,
        ADD,
        REMOVE
    }

    public static class RecipeData {

        private boolean open;
        private boolean filtering;

        public RecipeData(boolean open, boolean filtering) {
            this.open = open;
            this.filtering = filtering;
        }

        public void setOpen(boolean open) {
            this.open = open;
        }

        public void setFiltering(boolean filtering) {
            this.filtering = filtering;
        }
    }
}

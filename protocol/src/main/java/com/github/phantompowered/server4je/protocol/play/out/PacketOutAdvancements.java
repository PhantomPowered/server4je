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
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutAdvancements implements Packet {

    public boolean resetAdvancements;
    public AdvancementMapping[] advancements;
    public Namespaced[] advancementsForRemoval;
    public ProgressMapping[] progressMappings;

    public PacketOutAdvancements() {
    }

    public PacketOutAdvancements(boolean resetAdvancements, AdvancementMapping[] advancements, Namespaced[] advancementsForRemoval, ProgressMapping[] progressMappings) {
        this.resetAdvancements = resetAdvancements;
        this.advancements = advancements;
        this.advancementsForRemoval = advancementsForRemoval;
        this.progressMappings = progressMappings;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeBoolean(this.resetAdvancements);

        dataBuffer.writeVarInt(this.advancements.length);
        for (AdvancementMapping advancement : this.advancements) {
            advancement.write(dataBuffer);
        }

        dataBuffer.writeVarInt(this.advancementsForRemoval.length);
        for (Namespaced namespaced : this.advancementsForRemoval) {
            dataBuffer.writeNamespaced(namespaced);
        }

        dataBuffer.writeVarInt(this.progressMappings.length);
        for (ProgressMapping progressMapping : this.progressMappings) {
            progressMapping.write(dataBuffer);
        }
    }

    @Override
    public void releaseData() {
        this.advancements = null;
        this.advancementsForRemoval = null;
        this.progressMappings = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutAdvancements.class);
    }

    public boolean isResetAdvancements() {
        return this.resetAdvancements;
    }

    public void setResetAdvancements(boolean resetAdvancements) {
        this.resetAdvancements = resetAdvancements;
    }

    public AdvancementMapping[] getAdvancements() {
        return this.advancements;
    }

    public void setAdvancements(AdvancementMapping[] advancements) {
        this.advancements = advancements;
    }

    public Namespaced[] getAdvancementsForRemoval() {
        return this.advancementsForRemoval;
    }

    public void setAdvancementsForRemoval(Namespaced[] advancementsForRemoval) {
        this.advancementsForRemoval = advancementsForRemoval;
    }

    public ProgressMapping[] getProgressMappings() {
        return this.progressMappings;
    }

    public void setProgressMappings(ProgressMapping[] progressMappings) {
        this.progressMappings = progressMappings;
    }

    public static class AdvancementMapping {

        private String key;
        private Advancement advancement;

        public AdvancementMapping(String key, Advancement advancement) {
            this.key = key;
            this.advancement = advancement;
        }

        private void write(@NotNull DataBuffer dataBuffer) {
            dataBuffer.writeString(this.key);
            this.advancement.write(dataBuffer);
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Advancement getAdvancement() {
            return this.advancement;
        }

        public void setAdvancement(Advancement advancement) {
            this.advancement = advancement;
        }
    }

    public static class Advancement {

        private Namespaced parentId;
        private DisplayData displayData;
        private String[] criteria;
        private String[] requirements;

        public Advancement(Namespaced parentId, DisplayData displayData, String[] criteria, String[] requirements) {
            this.parentId = parentId;
            this.displayData = displayData;
            this.criteria = criteria;
            this.requirements = requirements;
        }

        private void write(@NotNull DataBuffer dataBuffer) {
            dataBuffer.writeBoolean(this.parentId != null);
            if (this.parentId != null) {
                dataBuffer.writeNamespaced(this.parentId);
            }

            dataBuffer.writeBoolean(this.displayData != null);
            if (this.displayData != null) {
                this.displayData.write(dataBuffer);
            }

            dataBuffer.writeStringArray(this.criteria);
            dataBuffer.writeVarInt(this.requirements.length);
            dataBuffer.writeStringArray(this.requirements);
        }

        public Namespaced getParentId() {
            return this.parentId;
        }

        public void setParentId(Namespaced parentId) {
            this.parentId = parentId;
        }

        public DisplayData getDisplayData() {
            return this.displayData;
        }

        public void setDisplayData(DisplayData displayData) {
            this.displayData = displayData;
        }

        public String[] getCriteria() {
            return this.criteria;
        }

        public void setCriteria(String[] criteria) {
            this.criteria = criteria;
        }

        public String[] getRequirements() {
            return this.requirements;
        }

        public void setRequirements(String[] requirements) {
            this.requirements = requirements;
        }
    }

    public static class DisplayData {

        private TextComponent title;
        private TextComponent description;
        private ItemStack icon;
        private FrameType frameType;
        private boolean hasBackgroundTexture;
        private boolean showToast;
        private boolean hidden;
        private String backgroundTexture;
        private float x;
        private float y;

        public DisplayData(TextComponent title, TextComponent description, ItemStack icon, FrameType frameType,
                           boolean hasBackgroundTexture, boolean showToast, boolean hidden, String backgroundTexture, float x, float y) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.frameType = frameType;
            this.hasBackgroundTexture = hasBackgroundTexture;
            this.showToast = showToast;
            this.hidden = hidden;
            this.backgroundTexture = backgroundTexture;
            this.x = x;
            this.y = y;
        }

        private void write(DataBuffer writer) {
            writer.writeString(ComponentSerializer.toString(this.title));
            writer.writeString(ComponentSerializer.toString(this.description));
            writer.writeItemStack(this.icon);
            writer.writeVarInt(this.frameType.ordinal());

            int flags = 0;
            if (this.hasBackgroundTexture) {
                flags = flags | 1;
            }

            if (this.showToast) {
                flags = flags | 2;
            }

            if (this.hidden) {
                flags = flags | 4;
            }

            writer.writeInt(flags);
            if (this.hasBackgroundTexture && this.backgroundTexture != null) {
                writer.writeString(this.backgroundTexture);
            }

            writer.writeFloat(this.x);
            writer.writeFloat(this.y);
        }

        public TextComponent getTitle() {
            return this.title;
        }

        public void setTitle(TextComponent title) {
            this.title = title;
        }

        public TextComponent getDescription() {
            return this.description;
        }

        public void setDescription(TextComponent description) {
            this.description = description;
        }

        public ItemStack getIcon() {
            return this.icon;
        }

        public void setIcon(ItemStack icon) {
            this.icon = icon;
        }

        public FrameType getFrameType() {
            return this.frameType;
        }

        public void setFrameType(FrameType frameType) {
            this.frameType = frameType;
        }

        public boolean isHasBackgroundTexture() {
            return this.hasBackgroundTexture;
        }

        public void setHasBackgroundTexture(boolean hasBackgroundTexture) {
            this.hasBackgroundTexture = hasBackgroundTexture;
        }

        public boolean isShowToast() {
            return this.showToast;
        }

        public void setShowToast(boolean showToast) {
            this.showToast = showToast;
        }

        public boolean isHidden() {
            return this.hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public String getBackgroundTexture() {
            return this.backgroundTexture;
        }

        public void setBackgroundTexture(String backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
        }

        public float getX() {
            return this.x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return this.y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    public static class ProgressMapping {

        private Namespaced key;
        private AdvancementProgress value;

        public ProgressMapping(Namespaced key, AdvancementProgress value) {
            this.key = key;
            this.value = value;
        }

        private void write(DataBuffer writer) {
            writer.writeNamespaced(this.key);
            this.value.write(writer);
        }

        public Namespaced getKey() {
            return this.key;
        }

        public void setKey(Namespaced key) {
            this.key = key;
        }

        public AdvancementProgress getValue() {
            return this.value;
        }

        public void setValue(AdvancementProgress value) {
            this.value = value;
        }
    }

    public static class AdvancementProgress {

        private Criteria[] criteria;

        public AdvancementProgress(Criteria[] criteria) {
            this.criteria = criteria;
        }

        private void write(DataBuffer writer) {
            writer.writeVarInt(this.criteria.length);
            for (Criteria criterion : this.criteria) {
                criterion.write(writer);
            }
        }

        public Criteria[] getCriteria() {
            return this.criteria;
        }

        public void setCriteria(Criteria[] criteria) {
            this.criteria = criteria;
        }
    }

    public static class Criteria {

        private Namespaced identifier;
        private Progress progress;

        public Criteria(Namespaced identifier, Progress progress) {
            this.identifier = identifier;
            this.progress = progress;
        }

        private void write(DataBuffer writer) {
            writer.writeNamespaced(this.identifier);
            this.progress.write(writer);
        }

        public Namespaced getIdentifier() {
            return this.identifier;
        }

        public void setIdentifier(Namespaced identifier) {
            this.identifier = identifier;
        }

        public Progress getProgress() {
            return this.progress;
        }

        public void setProgress(Progress progress) {
            this.progress = progress;
        }
    }

    public static class Progress {

        private boolean achieved;
        private long dateOfAchieving;

        public Progress(boolean achieved, long dateOfAchieving) {
            this.achieved = achieved;
            this.dateOfAchieving = dateOfAchieving;
        }

        private void write(DataBuffer writer) {
            writer.writeBoolean(this.achieved);
            if (this.dateOfAchieving > 0) {
                writer.writeLong(this.dateOfAchieving);
            }
        }

        public boolean isAchieved() {
            return this.achieved;
        }

        public void setAchieved(boolean achieved) {
            this.achieved = achieved;
        }

        public long getDateOfAchieving() {
            return this.dateOfAchieving;
        }

        public void setDateOfAchieving(long dateOfAchieving) {
            this.dateOfAchieving = dateOfAchieving;
        }
    }

    public enum FrameType {

        TASK,
        CHALLENGE,
        GOAL
    }
}

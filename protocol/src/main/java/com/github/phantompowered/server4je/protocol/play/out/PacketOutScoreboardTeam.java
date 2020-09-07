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

import com.github.phantompowered.server4je.common.CommonConstants;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.google.common.collect.ImmutableMap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;

public class PacketOutScoreboardTeam implements Packet {

    private static final Map<Team.OptionStatus, String> NAME_TAG_VISIBILITY_TO_NOTCH = ImmutableMap.of(
        Team.OptionStatus.ALWAYS, "always",
        Team.OptionStatus.NEVER, "never",
        Team.OptionStatus.FOR_OTHER_TEAMS, "hideForOtherTeams",
        Team.OptionStatus.FOR_OWN_TEAM, "hideForOwnTeam"
    );
    private static final Map<Team.OptionStatus, String> COLLISION_RULE_TO_NOTCH = ImmutableMap.of(
        Team.OptionStatus.ALWAYS, "always",
        Team.OptionStatus.NEVER, "never",
        Team.OptionStatus.FOR_OTHER_TEAMS, "pushOtherTeams",
        Team.OptionStatus.FOR_OWN_TEAM, "pushOwnTeam"
    );
    private static final ScoreboardTeamData REMOVE_TEAM = new RemoveTeamData();

    @SuppressWarnings("unchecked")
    public static <T extends ScoreboardTeamData> T createData(@NotNull Action action) {
        switch (action) {
            case CREATE:
                return (T) new CreateTeamData();
            case UPDATE:
                return (T) new UpdateTeamData();
            case ADD_PLAYERS:
                return (T) new AddPlayersTeamData();
            case REMOVE_PLAYERS:
                return (T) new RemovePlayersTeamData();
            case REMOVE:
                return (T) PacketOutScoreboardTeam.REMOVE_TEAM;
            default:
                throw ReportedException.forMessage("Unhandled scoreboard team action " + action);
        }
    }

    private String teamName;
    private ScoreboardTeamData teamData;

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeString(this.teamName == null ? CommonConstants.EMPTY_STRING : this.teamName, 16);
        dataBuffer.writeByte(this.teamData.getAction().ordinal());
        this.teamData.serialize(dataBuffer);
    }

    @Override
    public void releaseData() {
        this.teamName = null;
        this.teamData = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutScoreboardTeam.class);
    }

    public String getTeamName() {
        return this.teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public ScoreboardTeamData getTeamData() {
        return this.teamData;
    }

    public void setTeamData(ScoreboardTeamData teamData) {
        this.teamData = teamData;
    }

    public interface ScoreboardTeamData {

        @NotNull
        Action getAction();

        void serialize(@NotNull DataBuffer dataBuffer);
    }

    protected abstract static class BaseCreateUpdateTeamData<T extends BaseCreateUpdateTeamData<T>> implements ScoreboardTeamData {

        protected BaseComponent[] displayName;
        protected byte flags;
        protected Team.OptionStatus nameTagVisibility;
        protected Team.OptionStatus collisionRule;
        protected ChatColor teamColor;
        protected BaseComponent[] prefix;
        protected BaseComponent[] suffix;

        @NotNull
        public T displayName(@NotNull BaseComponent[] displayName) {
            this.displayName = displayName;
            return this.self();
        }

        @NotNull
        public T flags(boolean friendlyFire, boolean canSeeInvisiblePlayers) {
            this.flags = 0;
            if (friendlyFire) {
                this.flags |= 1;
            }

            if (canSeeInvisiblePlayers) {
                this.flags |= 2;
            }

            return this.self();
        }

        @NotNull
        public T nameTagVisibility(@NotNull Team.OptionStatus nameTagVisibility) {
            this.nameTagVisibility = nameTagVisibility;
            return this.self();
        }

        @NotNull
        public T collisionRule(@NotNull Team.OptionStatus collisionRule) {
            this.collisionRule = collisionRule;
            return this.self();
        }

        @NotNull
        public T teamColor(@NotNull ChatColor teamColor) {
            this.teamColor = teamColor;
            return this.self();
        }

        @NotNull
        public T prefix(@NotNull BaseComponent[] prefix) {
            this.prefix = prefix;
            return this.self();
        }

        @NotNull
        public T suffix(@NotNull BaseComponent[] suffix) {
            this.suffix = suffix;
            return this.self();
        }

        @Override
        public void serialize(@NotNull DataBuffer dataBuffer) {
            dataBuffer.writeString(ComponentSerializer.toString(this.displayName));
            dataBuffer.writeByte(this.flags);
            dataBuffer.writeString(NAME_TAG_VISIBILITY_TO_NOTCH.get(this.nameTagVisibility), 32);
            dataBuffer.writeString(COLLISION_RULE_TO_NOTCH.get(this.collisionRule), 32);
            dataBuffer.writeVarInt(this.teamColor.ordinal());
            dataBuffer.writeString(ComponentSerializer.toString(this.prefix));
            dataBuffer.writeString(ComponentSerializer.toString(this.suffix));
        }

        protected abstract T self();
    }

    protected interface BaseEntityData<T extends BaseEntityData<T>> extends ScoreboardTeamData {

        @NotNull
        T entities(@NotNull String[] entityIdentifiers);
    }

    public static final class CreateTeamData extends BaseCreateUpdateTeamData<CreateTeamData> implements BaseEntityData<CreateTeamData> {

        protected String[] entityIdentifiers;

        @Override
        public @NotNull Action getAction() {
            return Action.CREATE;
        }

        @Override
        public void serialize(@NotNull DataBuffer dataBuffer) {
            super.serialize(dataBuffer);
            dataBuffer.writeStringArrayFixedLength(this.entityIdentifiers, 40);
        }

        @Override
        protected CreateTeamData self() {
            return this;
        }

        @Override
        public @NotNull CreateTeamData entities(@NotNull String[] entityIdentifiers) {
            this.entityIdentifiers = entityIdentifiers;
            return this;
        }
    }

    public static final class RemoveTeamData implements ScoreboardTeamData {

        @Override
        public @NotNull Action getAction() {
            return Action.REMOVE;
        }

        @Override
        public void serialize(@NotNull DataBuffer dataBuffer) {
        }
    }

    public static final class UpdateTeamData extends BaseCreateUpdateTeamData<UpdateTeamData> {

        @Override
        public @NotNull Action getAction() {
            return Action.UPDATE;
        }

        @Override
        protected UpdateTeamData self() {
            return this;
        }
    }

    public static final class AddPlayersTeamData implements BaseEntityData<AddPlayersTeamData> {

        private String[] entityIdentifiers;

        @Override
        public @NotNull Action getAction() {
            return Action.ADD_PLAYERS;
        }

        @Override
        public void serialize(@NotNull DataBuffer dataBuffer) {
            dataBuffer.writeStringArrayFixedLength(this.entityIdentifiers, 40);
        }

        @Override
        public @NotNull AddPlayersTeamData entities(@NotNull String[] entityIdentifiers) {
            this.entityIdentifiers = entityIdentifiers;
            return this;
        }
    }

    public static final class RemovePlayersTeamData implements BaseEntityData<RemovePlayersTeamData> {

        private String[] entityIdentifiers;

        @Override
        public @NotNull Action getAction() {
            return Action.REMOVE_PLAYERS;
        }

        @Override
        public void serialize(@NotNull DataBuffer dataBuffer) {
            dataBuffer.writeStringArrayFixedLength(this.entityIdentifiers, 40);
        }

        @Override
        public @NotNull RemovePlayersTeamData entities(@NotNull String[] entityIdentifiers) {
            this.entityIdentifiers = entityIdentifiers;
            return this;
        }
    }

    public enum Action {

        CREATE,
        REMOVE,
        UPDATE,
        ADD_PLAYERS,
        REMOVE_PLAYERS
    }
}

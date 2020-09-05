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

import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.defaults.PrimitivePacket;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMaps;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public class PacketOutGameStateChange extends PrimitivePacket {

    // respawn not possible (no bed, no blocks around bed free)
    public static final StateChangeData NO_RESPAWN_BLOCK_AVAILABLE = StateChangeData.build(0);
    // rain
    public static final StateChangeData END_RAINING = StateChangeData.build(1);
    public static final StateChangeData START_RAINING = StateChangeData.build(2);
    // GameMode change
    public static final StateChangeData CHANGE_TO_SURVIVAL = StateChangeData.build(3);
    public static final StateChangeData CHANGE_TO_CREATIVE = StateChangeData.build(3, 1);
    public static final StateChangeData CHANGE_TO_ADVENTURE = StateChangeData.build(3, 2);
    public static final StateChangeData CHANGE_TO_SPECTATOR = StateChangeData.build(3, 3);
    // win game
    public static final StateChangeData WIN_GAME_JUST_RESPAWN = StateChangeData.build(4);
    public static final StateChangeData WIN_GAME_END_CREDITS = StateChangeData.build(4, 1);
    // demo events
    public static final StateChangeData SHOW_WELCOME_DEMO_SCREEN = StateChangeData.build(5);
    public static final StateChangeData TELL_MOVEMENT_CONTROLS = StateChangeData.build(5, 101);
    public static final StateChangeData TELL_JUMP_CONTROL = StateChangeData.build(5, 102);
    public static final StateChangeData TELL_INVENTORY_CONTROL = StateChangeData.build(5, 103);
    public static final StateChangeData DEMO_IS_OVER_GOODBYE = StateChangeData.build(5, 104);
    // arrow
    public static final StateChangeData ARROW_PLAYER_HIT = StateChangeData.build(6);
    // rain level
    public static final DataExpectingStateChangeData RAIN_LEVEL_CHANGE = DataExpectingStateChangeData.reason(7);
    // thunder level
    public static final DataExpectingStateChangeData THUNDER_LEVEL_CHANGE = DataExpectingStateChangeData.reason(8);
    // puffer fish sting
    public static final StateChangeData PLAY_PUFFER_FISH_STING = StateChangeData.build(9);
    // elder guardian appearance
    public static final StateChangeData ELDER_GUARDIAN_APPEARANCE = StateChangeData.build(10);
    // respawn screen
    public static final StateChangeData ENABLE_RESPAWN_SCREEN = StateChangeData.build(11);
    public static final StateChangeData IMMEDIATELY_RESPAWN = StateChangeData.build(11, 1);

    private StateChangeData stateChangeData;

    public PacketOutGameStateChange() {
    }

    public PacketOutGameStateChange(StateChangeData stateChangeData) {
        this.stateChangeData = stateChangeData;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {

    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        dataBuffer.writeByte(this.stateChangeData.reason);
        dataBuffer.writeFloat(this.stateChangeData.data);
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutGameStateChange.class);
    }

    public StateChangeData getStateChangeData() {
        return this.stateChangeData;
    }

    public void setStateChangeData(StateChangeData stateChangeData) {
        this.stateChangeData = stateChangeData;
    }

    public static final class StateChangeData {

        @NotNull
        @Contract("_ -> new")
        public static StateChangeData build(int reason) {
            return StateChangeData.build(reason, 0);
        }

        @NotNull
        @Contract("_, _ -> new")
        public static StateChangeData build(int reason, float data) {
            return new StateChangeData(reason, data);
        }

        private final int reason;
        private final float data;

        private StateChangeData(int reason, float data) {
            this.reason = reason;
            this.data = data;
        }
    }

    public static final class DataExpectingStateChangeData {

        @NotNull
        @Contract("_ -> new")
        public static DataExpectingStateChangeData reason(int reason) {
            return new DataExpectingStateChangeData(reason);
        }

        private final Float2ObjectMap<StateChangeData> builtStates = Float2ObjectMaps.synchronize(new Float2ObjectOpenHashMap<>());
        private final int reason;

        private DataExpectingStateChangeData(int reason) {
            this.reason = reason;
        }

        @NotNull
        public StateChangeData build(float data) {
            if (this.builtStates.containsKey(data)) {
                return this.builtStates.get(data);
            }

            StateChangeData stateChangeData = StateChangeData.build(this.reason, data);
            this.builtStates.put(data, stateChangeData);
            return stateChangeData;
        }
    }
}

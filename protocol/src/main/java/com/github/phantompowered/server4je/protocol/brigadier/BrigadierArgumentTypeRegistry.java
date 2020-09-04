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
package com.github.phantompowered.server4je.protocol.brigadier;

import com.github.phantompowered.server4je.common.collect.Iterables;
import com.github.phantompowered.server4je.common.exception.ClassShouldNotBeInstantiatedDirectlyException;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.brigadier.serializers.*;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.mojang.brigadier.arguments.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BrigadierArgumentTypeRegistry {

    private BrigadierArgumentTypeRegistry() {
        throw ClassShouldNotBeInstantiatedDirectlyException.INSTANCE;
    }

    private static final Collection<Entry> ENTRIES = new CopyOnWriteArrayList<>();

    private static void register(String id, Class<? extends ArgumentType<?>> type, BrigadierSerializer<?> buffer) {
        ENTRIES.add(new Entry(id, type, buffer));
    }

    private static void withoutType(String id, BrigadierSerializer<?> buffer) {
        register(id, null, buffer);
    }

    private static void withoutTypeAndBuffer(String id) {
        withoutType(id, VoidBrigadierSerializer.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    public static void serialize(@NotNull DataBuffer dataBuffer, @NotNull ArgumentType<?> argumentType) {
        Entry entry = Iterables.first(
            BrigadierArgumentTypeRegistry.ENTRIES,
            e -> e.type.equals(argumentType.getClass())
        ).orElse(null);
        if (entry == null) {
            throw ReportedException.forMessage("Unknown argument type " + argumentType.getClass().getName());
        }

        if (entry.type == null) {
            // not a brigadier argument type
            return;
        }

        dataBuffer.writeString(entry.id);
        ((BrigadierSerializer<ArgumentType<?>>) entry.buffer).serialize(argumentType, dataBuffer);
    }

    static {
        register("brigadier:bool", BoolArgumentType.class, BooleanArgumentBrigadierSerializer.INSTANCE);
        register("brigadier:double", DoubleArgumentType.class, DoubleArgumentBrigadierSerializer.INSTANCE);
        register("brigadier:float", FloatArgumentType.class, FloatArgumentBrigadierSerializer.INSTANCE);
        register("brigadier:integer", IntegerArgumentType.class, IntegerArgumentBrigadierSerializer.INSTANCE);
        register("brigadier:long", LongArgumentType.class, LongArgumentBrigadierSerializer.INSTANCE);
        register("brigadier:string", StringArgumentType.class, StringArgumentBrigadierSerializer.INSTANCE);

        withoutType("minecraft:entity", ByteBrigadierSerializer.INSTANCE);
        withoutType("minecraft:score_holder", ByteBrigadierSerializer.INSTANCE);
        withoutType("minecraft:range", BooleanBrigadierSerializer.INSTANCE);

        withoutTypeAndBuffer("minecraft:game_profile");
        withoutTypeAndBuffer("minecraft:block_pos");
        withoutTypeAndBuffer("minecraft:column_pos");
        withoutTypeAndBuffer("minecraft:vec3");
        withoutTypeAndBuffer("minecraft:vec2");
        withoutTypeAndBuffer("minecraft:block_state");
        withoutTypeAndBuffer("minecraft:block_predicate");
        withoutTypeAndBuffer("minecraft:item_stack");
        withoutTypeAndBuffer("minecraft:item_predicate");
        withoutTypeAndBuffer("minecraft:color");
        withoutTypeAndBuffer("minecraft:component");
        withoutTypeAndBuffer("minecraft:message");
        withoutTypeAndBuffer("minecraft:nbt");
        withoutTypeAndBuffer("minecraft:nbt_compound_tag");
        withoutTypeAndBuffer("minecraft:nbt_tag");
        withoutTypeAndBuffer("minecraft:nbt_path");
        withoutTypeAndBuffer("minecraft:objective");
        withoutTypeAndBuffer("minecraft:objective_criteria");
        withoutTypeAndBuffer("minecraft:operation");
        withoutTypeAndBuffer("minecraft:particle");
        withoutTypeAndBuffer("minecraft:rotation");
        withoutTypeAndBuffer("minecraft:scoreboard_slot");
        withoutTypeAndBuffer("minecraft:swizzle");
        withoutTypeAndBuffer("minecraft:team");
        withoutTypeAndBuffer("minecraft:item_slot");
        withoutTypeAndBuffer("minecraft:resource_location");
        withoutTypeAndBuffer("minecraft:mob_effect");
        withoutTypeAndBuffer("minecraft:function");
        withoutTypeAndBuffer("minecraft:entity_anchor");
        withoutTypeAndBuffer("minecraft:item_enchantment");
        withoutTypeAndBuffer("minecraft:entity_summon");
        withoutTypeAndBuffer("minecraft:dimension");
        withoutTypeAndBuffer("minecraft:int_range");
        withoutTypeAndBuffer("minecraft:float_range");
        withoutTypeAndBuffer("minecraft:time");
        withoutTypeAndBuffer("minecraft:uuid");
        withoutTypeAndBuffer("minecraft:angle");
    }

    private static class Entry {

        private final String id;
        private final Class<? extends ArgumentType<?>> type;
        private final BrigadierSerializer<?> buffer;

        public Entry(String id, Class<? extends ArgumentType<?>> type, BrigadierSerializer<?> buffer) {
            this.id = id;
            this.type = type;
            this.buffer = buffer;
        }
    }
}

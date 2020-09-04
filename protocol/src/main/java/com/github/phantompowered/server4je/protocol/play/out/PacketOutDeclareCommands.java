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
import com.destroystokyo.paper.NamespacedTag;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.protocol.Packet;
import com.github.phantompowered.server4je.protocol.annotation.BufferStatus;
import com.github.phantompowered.server4je.protocol.brigadier.BrigadierArgumentTypeRegistry;
import com.github.phantompowered.server4je.protocol.buffer.DataBuffer;
import com.github.phantompowered.server4je.protocol.exceptions.PacketOnlyToClientException;
import com.github.phantompowered.server4je.protocol.id.PacketIdUtil;
import com.github.phantompowered.server4je.protocol.state.ProtocolState;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

public class PacketOutDeclareCommands implements Packet {

    private static final Namespaced ASK_SERVER = NamespacedTag.minecraft("ask_server");

    private RootCommandNode<CommandSender> rootCommandNode;

    public PacketOutDeclareCommands() {
    }

    public PacketOutDeclareCommands(RootCommandNode<CommandSender> rootCommandNode) {
        this.rootCommandNode = rootCommandNode;
    }

    @Override
    public void readData(@NotNull @BufferStatus(BufferStatus.Status.FILLED) DataBuffer dataBuffer) {
        PacketOnlyToClientException.throwNow();
    }

    @Override
    public void writeData(@NotNull @BufferStatus(BufferStatus.Status.EMPTY) DataBuffer dataBuffer) {
        Deque<CommandNode<CommandSender>> deque = new ArrayDeque<>(Collections.singleton(this.rootCommandNode));
        Object2IntMap<CommandNode<CommandSender>> mappings = new Object2IntLinkedOpenHashMap<>();
        while (!deque.isEmpty()) {
            CommandNode<CommandSender> commandNode = deque.poll();
            if (!mappings.containsKey(commandNode)) {
                mappings.put(commandNode, mappings.size());
                deque.addAll(commandNode.getChildren());
            }
        }

        dataBuffer.writeVarInt(mappings.size());
        for (CommandNode<CommandSender> commandNode : mappings.keySet()) {
            PacketOutDeclareCommands.serializeCommandNode(commandNode, mappings, dataBuffer);
        }

        dataBuffer.writeVarInt(mappings.getInt(this.rootCommandNode));
    }

    @Override
    public void releaseData() {
        this.rootCommandNode = null;
    }

    @Override
    public @Range(from = 0, to = Short.MAX_VALUE) short getId() {
        return PacketIdUtil.getServerPacketId(ProtocolState.PLAY, PacketOutDeclareCommands.class);
    }

    public RootCommandNode<CommandSender> getRootCommandNode() {
        return this.rootCommandNode;
    }

    public void setRootCommandNode(RootCommandNode<CommandSender> rootCommandNode) {
        this.rootCommandNode = rootCommandNode;
    }

    private static void serializeCommandNode(@NotNull CommandNode<CommandSender> node,
                                             @NotNull Object2IntMap<CommandNode<CommandSender>> allNodes,
                                             @NotNull DataBuffer dataBuffer) {
        byte flags = 0;
        if (node instanceof LiteralCommandNode<?>) {
            flags |= 1;
        } else if (node instanceof ArgumentCommandNode<?, ?>) {
            flags |= 2;
            if (((ArgumentCommandNode<?, ?>) node).getCustomSuggestions() != null) {
                flags |= 16;
            }
        } else if (!(node instanceof RootCommandNode<?>)) {
            ReportedException.throwWrapped("Unimplemented command node type " + node.getClass().getName());
        }

        if (node.getCommand() != null) {
            flags |= 4;
        }

        if (node.getRedirect() != null) {
            flags |= 8;
        }

        dataBuffer.writeByte(flags);
        dataBuffer.writeVarInt(node.getChildren().size());
        for (CommandNode<CommandSender> child : node.getChildren()) {
            dataBuffer.writeVarInt(allNodes.getInt(child));
        }

        if (node.getRedirect() != null) {
            dataBuffer.writeVarInt(allNodes.getInt(node.getRedirect()));
        }

        if (node instanceof ArgumentCommandNode<?, ?>) {
            dataBuffer.writeString(node.getName());
            ArgumentCommandNode<?, ?> argumentCommandNode = (ArgumentCommandNode<?, ?>) node;

            BrigadierArgumentTypeRegistry.serialize(dataBuffer, argumentCommandNode.getType());
            if (argumentCommandNode.getCustomSuggestions() != null) {
                SuggestionProvider<?> suggestionProvider = argumentCommandNode.getCustomSuggestions();
                Namespaced suggestionType = PacketOutDeclareCommands.ASK_SERVER;
                if (suggestionProvider instanceof PhantomSuggestionProvider) {
                    suggestionType = ((PhantomSuggestionProvider) suggestionProvider).getSuggestionType();
                }

                dataBuffer.writeNamespaced(suggestionType);
            }
        } else if (node instanceof LiteralCommandNode<?>) {
            dataBuffer.writeString(node.getName());
        }
    }

    public static class PhantomSuggestionProvider implements SuggestionProvider<CommandSender> {

        private final SuggestionProvider<CommandSender> suggestionProvider;
        private final Namespaced suggestionType;

        public PhantomSuggestionProvider(SuggestionProvider<CommandSender> suggestionProvider, Namespaced suggestionType) {
            this.suggestionProvider = suggestionProvider;
            this.suggestionType = suggestionType;
        }

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSender> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return this.suggestionProvider.getSuggestions(context, builder);
        }

        public Namespaced getSuggestionType() {
            return this.suggestionType;
        }
    }
}

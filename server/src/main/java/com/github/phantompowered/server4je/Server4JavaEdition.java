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
package com.github.phantompowered.server4je;

import com.destroystokyo.paper.entity.ai.MobGoals;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.phantompowered.server4je.api.PhantomServer;
import com.github.phantompowered.server4je.api.network.NetworkManager;
import com.github.phantompowered.server4je.api.player.OfflinePlayerManager;
import com.github.phantompowered.server4je.api.player.PlayerManager;
import com.github.phantompowered.server4je.api.version.ServerVersion;
import com.github.phantompowered.server4je.common.exception.ReportedException;
import com.github.phantompowered.server4je.gson.JsonDataLoader;
import com.github.phantompowered.server4je.options.ServerCliOptionUtil;
import com.github.phantompowered.server4je.version.PhantomServerVersion;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.concurrent.FastThreadLocalThread;
import joptsimple.OptionSet;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.*;
import org.bukkit.command.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Server4JavaEdition extends PhantomServer {

    private final OptionSet options;
    private final ServerVersion serverVersion;
    private final ListeningScheduledExecutorService executorService;

    public Server4JavaEdition(@NotNull OptionSet options) {
        this.options = options;
        this.executorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicLong threadCount = new AtomicLong();

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new FastThreadLocalThread(r, "Server4JE Executor Thread #" + this.threadCount.getAndIncrement());
            }
        }));

        try (var stream = Server4JavaEdition.class.getClassLoader().getResourceAsStream("version_data.json")) {
            Preconditions.checkNotNull(stream, "version_data.json missing, custom build?");
            this.serverVersion = JsonDataLoader.loadFromStream(stream, PhantomServerVersion.class);
        } catch (IOException exception) {
            throw ReportedException.forThrowable(exception);
        }

        PhantomServer.setTheServer(this);
    }

    protected void bootstrap() {

    }

    @Override
    public @NotNull
    ListeningScheduledExecutorService getExecutor() {
        return this.executorService;
    }

    @Override
    public @NotNull
    ServerVersion getServerVersion() {
        return this.serverVersion;
    }

    @Override
    public @NotNull
    NetworkManager getNetworkManager() {
        return null;
    }

    @Override
    public void broadcast(@NotNull BaseComponent[] message, @NotNull String permission) {

    }

    @Override
    public @NotNull
    PlayerManager getPlayerManager() {
        return null;
    }

    @Override
    public @NotNull
    OfflinePlayerManager getOfflinePlayerManager() {
        return null;
    }

    @Override
    public @NotNull String getPrompt() {
        return ServerCliOptionUtil.getOption(this.options, "prompt", Functions.identity(), "> ").orElseThrow();
    }

    @Override
    public @NotNull
    String getName() {
        return null;
    }

    @Override
    public @NotNull
    String getVersion() {
        return null;
    }

    @Override
    public @NotNull
    String getBukkitVersion() {
        return null;
    }

    @Override
    public @NotNull
    String getMinecraftVersion() {
        return null;
    }

    @Override
    public @NotNull
    Collection<? extends Player> getOnlinePlayers() {
        return null;
    }

    @Override
    public int getMaxPlayers() {
        return 0;
    }

    @Override
    public void setMaxPlayers(int i) {

    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public int getViewDistance() {
        return 0;
    }

    @Override
    public @NotNull
    String getIp() {
        return null;
    }

    @Override
    public @NotNull
    String getWorldType() {
        return null;
    }

    @Override
    public boolean getGenerateStructures() {
        return false;
    }

    @Override
    public boolean getAllowEnd() {
        return false;
    }

    @Override
    public boolean getAllowNether() {
        return false;
    }

    @Override
    public boolean hasWhitelist() {
        return false;
    }

    @Override
    public void setWhitelist(boolean b) {

    }

    @Override
    public @NotNull
    Set<OfflinePlayer> getWhitelistedPlayers() {
        return null;
    }

    @Override
    public void reloadWhitelist() {

    }

    @Override
    public int broadcastMessage(@NotNull String s) {
        return 0;
    }

    @Override
    public @NotNull
    String getUpdateFolder() {
        return null;
    }

    @Override
    public @NotNull
    File getUpdateFolderFile() {
        return null;
    }

    @Override
    public long getConnectionThrottle() {
        return 0;
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerWaterSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerWaterAmbientSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerAmbientSpawns() {
        return 0;
    }

    @Override
    public @Nullable
    Player getPlayer(@NotNull String s) {
        return null;
    }

    @Override
    public @Nullable
    Player getPlayerExact(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull
    List<Player> matchPlayer(@NotNull String s) {
        return null;
    }

    @Override
    public @Nullable
    Player getPlayer(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @Nullable
    UUID getPlayerUniqueId(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull
    PluginManager getPluginManager() {
        return null;
    }

    @Override
    public @NotNull
    BukkitScheduler getScheduler() {
        return null;
    }

    @Override
    public @NotNull
    ServicesManager getServicesManager() {
        return null;
    }

    @Override
    public @NotNull
    List<World> getWorlds() {
        return null;
    }

    @Override
    public @Nullable
    World createWorld(@NotNull WorldCreator worldCreator) {
        return null;
    }

    @Override
    public boolean unloadWorld(@NotNull String s, boolean b) {
        return false;
    }

    @Override
    public boolean unloadWorld(@NotNull World world, boolean b) {
        return false;
    }

    @Override
    public @Nullable
    World getWorld(@NotNull String s) {
        return null;
    }

    @Override
    public @Nullable
    World getWorld(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @Nullable
    MapView getMap(int i) {
        return null;
    }

    @Override
    public @NotNull
    MapView createMap(@NotNull World world) {
        return null;
    }

    @Override
    public @NotNull
    ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType) {
        return null;
    }

    @Override
    public @NotNull
    ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType, int i, boolean b) {
        return null;
    }

    @Override
    public void reloadData() {

    }

    @Override
    public @NotNull
    Logger getLogger() {
        return null;
    }

    @Override
    public @Nullable
    PluginCommand getPluginCommand(@NotNull String s) {
        return null;
    }

    @Override
    public void savePlayers() {

    }

    @Override
    public boolean dispatchCommand(@NotNull CommandSender commandSender, @NotNull String s) throws CommandException {
        return false;
    }

    @Override
    public boolean addRecipe(@Nullable Recipe recipe) {
        return false;
    }

    @Override
    public @NotNull
    List<Recipe> getRecipesFor(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public @Nullable
    Recipe getRecipe(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public @NotNull
    Iterator<Recipe> recipeIterator() {
        return null;
    }

    @Override
    public void clearRecipes() {

    }

    @Override
    public void resetRecipes() {

    }

    @Override
    public boolean removeRecipe(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public @NotNull
    Map<String, String[]> getCommandAliases() {
        return null;
    }

    @Override
    public int getSpawnRadius() {
        return 0;
    }

    @Override
    public void setSpawnRadius(int i) {

    }

    @Override
    public boolean getOnlineMode() {
        return false;
    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int broadcast(@NotNull String s, @NotNull String s1) {
        return 0;
    }

    @Override
    public @NotNull
    OfflinePlayer getOfflinePlayer(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull
    OfflinePlayer getOfflinePlayer(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @NotNull
    Set<String> getIPBans() {
        return null;
    }

    @Override
    public void banIP(@NotNull String s) {

    }

    @Override
    public void unbanIP(@NotNull String s) {

    }

    @Override
    public @NotNull
    Set<OfflinePlayer> getBannedPlayers() {
        return null;
    }

    @Override
    public @NotNull
    BanList getBanList(BanList.Type type) {
        return null;
    }

    @Override
    public @NotNull
    Set<OfflinePlayer> getOperators() {
        return null;
    }

    @Override
    public @NotNull
    GameMode getDefaultGameMode() {
        return null;
    }

    @Override
    public void setDefaultGameMode(@NotNull GameMode gameMode) {

    }

    @Override
    public @NotNull
    ConsoleCommandSender getConsoleSender() {
        return null;
    }

    @Override
    public @NotNull
    File getWorldContainer() {
        return null;
    }

    @Override
    public @NotNull
    OfflinePlayer[] getOfflinePlayers() {
        return new OfflinePlayer[0];
    }

    @Override
    public @NotNull
    Messenger getMessenger() {
        return null;
    }

    @Override
    public @NotNull
    HelpMap getHelpMap() {
        return null;
    }

    @Override
    public @NotNull
    Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType) {
        return null;
    }

    @Override
    public @NotNull
    Inventory createInventory(@Nullable InventoryHolder inventoryHolder, @NotNull InventoryType inventoryType, @NotNull String s) {
        return null;
    }

    @Override
    public @NotNull
    Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull
    Inventory createInventory(@Nullable InventoryHolder inventoryHolder, int i, @NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull
    Merchant createMerchant(@Nullable String s) {
        return null;
    }

    @Override
    public int getMonsterSpawnLimit() {
        return 0;
    }

    @Override
    public int getAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public int getAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public boolean isPrimaryThread() {
        return false;
    }

    @Override
    public @NotNull
    String getMotd() {
        return null;
    }

    @Override
    public @Nullable
    String getShutdownMessage() {
        return null;
    }

    @Override
    public Warning.WarningState getWarningState() {
        return null;
    }

    @Override
    public @NotNull
    ItemFactory getItemFactory() {
        return null;
    }

    @Override
    public @NotNull
    ScoreboardManager getScoreboardManager() {
        return null;
    }

    @Override
    public @Nullable
    CachedServerIcon getServerIcon() {
        return null;
    }

    @Override
    public @NotNull
    CachedServerIcon loadServerIcon(@NotNull File file) throws IllegalArgumentException, Exception {
        return null;
    }

    @Override
    public @NotNull
    CachedServerIcon loadServerIcon(@NotNull BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
        return null;
    }

    @Override
    public void setIdleTimeout(int i) {

    }

    @Override
    public int getIdleTimeout() {
        return 0;
    }

    @Override
    public ChunkGenerator.ChunkData createChunkData(@NotNull World world) {
        return null;
    }

    @Override
    public ChunkGenerator.ChunkData createVanillaChunkData(@NotNull World world, int i, int i1) {
        return null;
    }

    @Override
    public @NotNull
    BossBar createBossBar(@Nullable String s, @NotNull BarColor barColor, @NotNull BarStyle barStyle, @NotNull BarFlag... barFlags) {
        return null;
    }

    @Override
    public @NotNull
    KeyedBossBar createBossBar(@NotNull NamespacedKey namespacedKey, @Nullable String s, @NotNull BarColor barColor, @NotNull BarStyle barStyle, @NotNull BarFlag... barFlags) {
        return null;
    }

    @Override
    public @NotNull
    Iterator<KeyedBossBar> getBossBars() {
        return null;
    }

    @Override
    public @Nullable
    KeyedBossBar getBossBar(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public boolean removeBossBar(@NotNull NamespacedKey namespacedKey) {
        return false;
    }

    @Override
    public @Nullable
    Entity getEntity(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @NotNull
    double[] getTPS() {
        return new double[0];
    }

    @Override
    public @NotNull
    long[] getTickTimes() {
        return new long[0];
    }

    @Override
    public double getAverageTickTime() {
        return 0;
    }

    @Override
    public @NotNull
    CommandMap getCommandMap() {
        return null;
    }

    @Override
    public @Nullable
    Advancement getAdvancement(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public @NotNull
    Iterator<Advancement> advancementIterator() {
        return null;
    }

    @Override
    public @NotNull
    BlockData createBlockData(@NotNull Material material) {
        return null;
    }

    @Override
    public @NotNull
    BlockData createBlockData(@NotNull Material material, @Nullable Consumer<BlockData> consumer) {
        return null;
    }

    @Override
    public @NotNull
    BlockData createBlockData(@NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull
    BlockData createBlockData(@Nullable Material material, @Nullable String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public <T extends Keyed> Tag<T> getTag(@NotNull String s, @NotNull NamespacedKey namespacedKey, @NotNull Class<T> aClass) {
        return null;
    }

    @Override
    public @NotNull
    <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String s, @NotNull Class<T> aClass) {
        return null;
    }

    @Override
    public @Nullable
    LootTable getLootTable(@NotNull NamespacedKey namespacedKey) {
        return null;
    }

    @Override
    public @NotNull
    List<Entity> selectEntities(@NotNull CommandSender commandSender, @NotNull String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @NotNull
    UnsafeValues getUnsafe() {
        return null;
    }

    @Override
    public @NotNull
    Spigot spigot() {
        return null;
    }

    @Override
    public void reloadPermissions() {

    }

    @Override
    public boolean reloadCommandAliases() {
        return false;
    }

    @Override
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        return false;
    }

    @Override
    public @NotNull
    String getPermissionMessage() {
        return null;
    }

    @Override
    public @NotNull
    PlayerProfile createProfile(@NotNull UUID uuid) {
        return null;
    }

    @Override
    public @NotNull
    PlayerProfile createProfile(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull
    PlayerProfile createProfile(@Nullable UUID uuid, @Nullable String s) {
        return null;
    }

    @Override
    public int getCurrentTick() {
        return 0;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public @NotNull
    MobGoals getMobGoals() {
        return null;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin plugin, @NotNull String s, @NotNull byte[] bytes) {

    }

    @Override
    public @NotNull
    Set<String> getListeningPluginChannels() {
        return null;
    }
}

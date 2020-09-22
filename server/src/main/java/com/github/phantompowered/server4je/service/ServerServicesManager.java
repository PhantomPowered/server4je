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
package com.github.phantompowered.server4je.service;

import com.google.common.collect.Lists;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerServicesManager implements ServicesManager {

    private final Collection<RegisteredServiceProvider<?>> registeredServiceProviders = Lists.newCopyOnWriteArrayList();

    @Override
    public <T> void register(@NotNull Class<T> aClass, @NotNull T t, @NotNull Plugin plugin, @NotNull ServicePriority servicePriority) {
        this.registeredServiceProviders.add(new RegisteredServiceProvider<>(aClass, t, servicePriority, plugin));
    }

    @Override
    public void unregisterAll(@NotNull Plugin plugin) {
        this.registeredServiceProviders.removeIf(provider -> provider.getPlugin().equals(plugin));
    }

    @Override
    public void unregister(@NotNull Class<?> aClass, @NotNull Object o) {
        this.registeredServiceProviders.removeIf(provider -> provider.getService().equals(aClass) && provider.getProvider() == o);
    }

    @Override
    public void unregister(@NotNull Object o) {
        this.registeredServiceProviders.removeIf(provider -> provider.getProvider() == o);
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T load(@NotNull Class<T> aClass) {
        for (RegisteredServiceProvider<?> registeredServiceProvider : this.registeredServiceProviders) {
            if (registeredServiceProvider.getService().equals(aClass)) {
                return (T) registeredServiceProvider.getProvider();
            }
        }

        return null;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> RegisteredServiceProvider<T> getRegistration(@NotNull Class<T> aClass) {
        for (RegisteredServiceProvider<?> registeredServiceProvider : this.registeredServiceProviders) {
            if (registeredServiceProvider.getService().equals(aClass)) {
                return (RegisteredServiceProvider<T>) registeredServiceProvider;
            }
        }

        return null;
    }

    @Override
    @NotNull
    public List<RegisteredServiceProvider<?>> getRegistrations(@NotNull Plugin plugin) {
        List<RegisteredServiceProvider<?>> providers = new ArrayList<>();
        for (RegisteredServiceProvider<?> registeredServiceProvider : this.registeredServiceProviders) {
            if (registeredServiceProvider.getPlugin().equals(plugin)) {
                providers.add(registeredServiceProvider);
            }
        }

        return providers;
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> Collection<RegisteredServiceProvider<T>> getRegistrations(@NotNull Class<T> aClass) {
        Collection<RegisteredServiceProvider<T>> providers = new ArrayList<>();
        for (RegisteredServiceProvider<?> registeredServiceProvider : this.registeredServiceProviders) {
            if (registeredServiceProvider.getService().equals(aClass)) {
                providers.add((RegisteredServiceProvider<T>) registeredServiceProvider);
            }
        }

        return providers;
    }

    @Override
    @NotNull
    public Collection<Class<?>> getKnownServices() {
        Collection<Class<?>> services = new ArrayList<>();
        for (RegisteredServiceProvider<?> registeredServiceProvider : this.registeredServiceProviders) {
            services.add(registeredServiceProvider.getService());
        }

        return services;
    }

    @Override
    public <T> boolean isProvidedFor(@NotNull Class<T> aClass) {
        for (RegisteredServiceProvider<?> registeredServiceProvider : this.registeredServiceProviders) {
            if (registeredServiceProvider.getService().equals(aClass)) {
                return true;
            }
        }

        return false;
    }
}

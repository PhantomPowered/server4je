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
package com.github.phantompowered.server4je.authlib.gson;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class PropertyMultimapSerializer implements JsonSerializer<Multimap<String, ProfileProperty>>, JsonDeserializer<Multimap<String, ProfileProperty>> {

    @Override
    public Multimap<String, ProfileProperty> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (element instanceof JsonObject) {
            return this.deserializeJsonObject((JsonObject) element);
        } else if (element instanceof JsonArray) {
            return this.deserializeJsonArray((JsonArray) element);
        }

        System.err.println("Unable to deserialize multimap from " + element.getClass().getName());
        return ImmutableMultimap.of();
    }

    private Multimap<String, ProfileProperty> deserializeJsonObject(@NotNull JsonObject jsonObject) {
        Multimap<String, ProfileProperty> map = LinkedHashMultimap.create();
        for (var entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonArray()) {
                var array = (JsonArray) entry.getValue();
                for (var element : array) {
                    map.put(entry.getKey(), new ProfileProperty(entry.getKey(), element.getAsString()));
                }
            }
        }

        return map;
    }

    private Multimap<String, ProfileProperty> deserializeJsonArray(@NotNull JsonArray jsonArray) {
        Multimap<String, ProfileProperty> map = LinkedHashMultimap.create();
        for (var element : jsonArray) {
            if (element.isJsonObject()) {
                var object = (JsonObject) element;

                var name = object.get("name").getAsString();
                var value = object.get("value").getAsString();

                if (object.has("signature") && !(object.get("signature") instanceof JsonNull)) {
                    map.put(name, new ProfileProperty(name, value, object.get("signature").getAsString()));
                } else {
                    map.put(name, new ProfileProperty(name, value));
                }
            }
        }

        return map;
    }

    @Override
    public JsonElement serialize(Multimap<String, ProfileProperty> map, Type type, JsonSerializationContext context) {
        var array = new JsonArray();
        for (var value : map.values()) {
            var object = new JsonObject();

            object.addProperty("name", value.getName());
            object.addProperty("value", value.getValue());
            if (value.isSigned()) {
                object.addProperty("signature", value.getSignature());
            }

            array.add(object);
        }

        return array;
    }
}

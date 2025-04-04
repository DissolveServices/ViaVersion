/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.rewriter;

import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface TagRewriter extends MappingDataListener {

    void removeTags(String registryKey);

    void removeTag(RegistryType type, String tagId);

    void renameTag(RegistryType type, String tagId, String renameTo);

    /**
     * Adds an empty tag (since the client crashes if a checked tag is not registered).
     *
     * @param tagType registry tag type
     * @param tagId   tag id
     */
    void addEmptyTag(RegistryType tagType, String tagId);

    void addEmptyTags(RegistryType tagType, String... tagIds);

    /**
     * Adds an entity tag type to be filled with the given entity type ids.
     *
     * @param tagId    registry tag type
     * @param entities mapped entity types
     */
    void addEntityTag(String tagId, EntityType... entities);

    /**
     * Adds a tag type to be filled with the given type ids after being mapped to new ids.
     *
     * @param tagType     registry tag type
     * @param tagId       tag id
     * @param unmappedIds unmapped type ids
     */
    void addTag(RegistryType tagType, String tagId, int... unmappedIds);

    /**
     * Adds a tag type to be filled with the given raw type ids.
     *
     * @param tagType registry tag type
     * @param tagId   tag id
     * @param ids     raw type ids
     */
    void addTagRaw(RegistryType tagType, String tagId, int... ids);

    @Nullable
    List<TagData> getNewTags(RegistryType tagType);

    List<TagData> getOrComputeNewTags(RegistryType tagType);
}

/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.BlockStateProperties;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableComponent;
import com.viaversion.viaversion.api.minecraft.item.data.FilterableString;
import com.viaversion.viaversion.api.minecraft.item.data.FireworkExplosion;
import com.viaversion.viaversion.api.minecraft.item.data.WrittenBook;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.EnchantmentMappings;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data.MapDecorationMappings;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.UUIDUtil;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_5 extends ItemRewriter<ClientboundPacket1_20_3, ServerboundPacket1_20_5, Protocol1_20_5To1_20_3> {

    private static final GameProfile.Property[] EMPTY_PROPERTIES = new GameProfile.Property[0];

    public BlockItemPacketRewriter1_20_5(final Protocol1_20_5To1_20_3 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY, Types1_20_5.ITEM, Types1_20_5.ITEM_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_3> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_3.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_3.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_3.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_3.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_3.CHUNK_DATA, ChunkType1_20_2::new);
        protocol.registerClientbound(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.VAR_INT); // Block entity type

            // No longer nullable
            final CompoundTag tag = wrapper.read(Type.COMPOUND_TAG);
            wrapper.write(Type.COMPOUND_TAG, tag != null ? tag : new CompoundTag());
        });

        registerSetCooldown(ClientboundPackets1_20_3.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_3.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_3.SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_20_3.ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_20_3.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_5.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_20_5.CREATIVE_INVENTORY_ACTION);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_3.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_20_3.SPAWN_PARTICLE, wrapper -> {
            final int particleId = wrapper.read(Type.VAR_INT);

            wrapper.passthrough(Type.BOOLEAN); // Long Distance
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Offset X
            wrapper.passthrough(Type.FLOAT); // Offset Y
            wrapper.passthrough(Type.FLOAT); // Offset Z
            wrapper.passthrough(Type.FLOAT); // Particle Data
            wrapper.passthrough(Type.INT); // Particle Count

            // Read data and add it to Particle
            final ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            final Particle particle = new Particle(mappings.getNewId(particleId));
            if (mappings.isBlockParticle(particleId)) {
                final int blockStateId = wrapper.read(Type.VAR_INT);
                particle.add(Type.VAR_INT, protocol.getMappingData().getNewBlockStateId(blockStateId));
            } else if (mappings.isItemParticle(particleId)) {
                final Item item = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                particle.add(Types1_20_5.ITEM, item);
            }

            wrapper.write(Types1_20_5.PARTICLE, particle);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.EXPLOSION, wrapper -> {
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Power
            final int blocks = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Type.BYTE); // Relative X
                wrapper.passthrough(Type.BYTE); // Relative Y
                wrapper.passthrough(Type.BYTE); // Relative Z
            }
            wrapper.passthrough(Type.FLOAT); // Knockback X
            wrapper.passthrough(Type.FLOAT); // Knockback Y
            wrapper.passthrough(Type.FLOAT); // Knockback Z
            wrapper.passthrough(Type.VAR_INT); // Block interaction type

            protocol.getEntityRewriter().rewriteParticle(wrapper, Types1_20_3.PARTICLE, Types1_20_5.PARTICLE); // Small explosion particle
            protocol.getEntityRewriter().rewriteParticle(wrapper, Types1_20_3.PARTICLE, Types1_20_5.PARTICLE); // Large explosion particle

            wrapper.write(Type.VAR_INT, 0); // "Empty" registry id to instead use the resource location that follows after
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.TRADE_LIST, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id
            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                final Item input = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                final Item output = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                final Item secondItem = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                wrapper.write(Types1_20_5.ITEM, input);
                wrapper.write(Types1_20_5.ITEM, output);
                wrapper.write(Types1_20_5.ITEM, secondItem);

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand

                wrapper.write(Type.BOOLEAN, false); // Ignore tags
            }
        });

        final RecipeRewriter1_20_3<ClientboundPacket1_20_3> recipeRewriter = new RecipeRewriter1_20_3<>(protocol);
        protocol.registerClientbound(ClientboundPackets1_20_3.DECLARE_RECIPES, wrapper -> {
            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                // Change order and write the type as an int
                final String type = wrapper.read(Type.STRING);
                wrapper.passthrough(Type.STRING); // Recipe Identifier

                wrapper.write(Type.VAR_INT, protocol.getMappingData().getRecipeSerializerMappings().mappedId(type));
                recipeRewriter.handleRecipeType(wrapper, Key.stripMinecraftNamespace(type));
            }
        });
    }

    @Override
    public @Nullable Item handleItemToClient(@Nullable final Item item) {
        if (item == null) return null;

        super.handleItemToClient(item);
        return toStructuredItem(item, true);
    }

    @Override
    public @Nullable Item handleItemToServer(@Nullable final Item item) {
        if (item == null) return null;

        super.handleItemToServer(item);
        return toOldItem(item);
    }

    // TODO Block entity changes
    public Item toOldItem(final Item item) {
        // Start out with custom data and add the rest on top, or short-curcuit with the original item
        final StructuredDataContainer data = item.structuredData();
        data.setIdLookup(protocol, true);

        final StructuredData<CompoundTag> customData = data.getNonEmpty(StructuredDataKey.CUSTOM_DATA);
        final CompoundTag tag = customData != null ? customData.value() : new CompoundTag();
        final DataItem dataItem = new DataItem(item.identifier(), (byte) item.amount(), (short) 0, tag);
        if (customData != null && tag.remove(nbtTagName()) != null) {
            return dataItem;
        }

        // TODO

        return dataItem;
    }

    public Item toStructuredItem(final Item old, final boolean addMarker) {
        final CompoundTag tag = old.tag();
        final StructuredItem item = new StructuredItem(old.identifier(), (byte) old.amount(), new StructuredDataContainer());
        final StructuredDataContainer data = item.structuredData();
        data.setIdLookup(protocol, true);
        if (tag == null) {
            return item;
        }

        // Rewrite nbt to new data structures
        final NumberTag hideFlags = tag.getNumberTag("HideFlags");
        final int hideFlagsValue = hideFlags != null ? hideFlags.asInt() : 0;
        if ((hideFlagsValue & 0x20) != 0) {
            data.set(StructuredDataKey.HIDE_ADDITIONAL_TOOLTIP);
        }

        updateDisplay(data, tag.getCompoundTag("display"), hideFlagsValue);

        final NumberTag damage = tag.getNumberTag("Damage");
        if (damage != null && damage.asInt() != 0) {
            data.set(StructuredDataKey.DAMAGE, damage.asInt());
        }

        final NumberTag repairCost = tag.getNumberTag("RepairCost");
        if (repairCost != null && repairCost.asInt() != 0) {
            data.set(StructuredDataKey.REPAIR_COST, repairCost.asInt());
        }

        final NumberTag customModelData = tag.getNumberTag("CustomModelData");
        if (customModelData != null) {
            data.set(StructuredDataKey.CUSTOM_MODEL_DATA, customModelData.asInt());
        }

        final CompoundTag blockState = tag.getCompoundTag("BlockStateTag");
        if (blockState != null) {
            final Map<String, String> properties = new HashMap<>();
            for (final Map.Entry<String, Tag> entry : blockState.entrySet()) {
                if (entry.getValue() instanceof StringTag) {
                    properties.put(entry.getKey(), ((StringTag) entry.getValue()).getValue());
                }
            }
            // TODO block state changes
            data.set(StructuredDataKey.BLOCK_STATE, new BlockStateProperties(properties));
        }

        final CompoundTag entityTag = tag.getCompoundTag("EntityTag");
        if (entityTag != null) {
            data.set(StructuredDataKey.ENTITY_DATA, entityTag);
        }

        final CompoundTag blockEntityTag = tag.getCompoundTag("BlockEntityTag");
        if (blockEntityTag != null) {
            // TODO lots of stuff
            // item.structuredData().add(protocol, "block_entity_data", Type.COMPOUND_TAG, blockEntityTag);
        }

        final NumberTag unbreakable = tag.getNumberTag("Unbreakable");
        if (unbreakable != null && unbreakable.asBoolean()) {
            if ((hideFlagsValue & 0x04) != 0) {
                data.set(StructuredDataKey.UNBREAKABLE, true); // TODO Value is hide, should have a wrapper
            } else {
                data.addEmpty(StructuredDataKey.UNBREAKABLE);
            }
        }

        final CompoundTag trimTag = tag.getCompoundTag("Trim");
        if (trimTag != null) {
            // TODO
            //final ArmorTrim armorTrim = new ArmorTrim(, , (hideFlagsValue & 0x80) == 0);
            //data.set(StructuredDataKey.TRIM, armorTrim);
        }

        final CompoundTag explosionTag = tag.getCompoundTag("Explosion");
        if (explosionTag != null) {
            final NumberTag shape = explosionTag.getNumberTag("Type");
            final IntArrayTag colors = explosionTag.getIntArrayTag("Colors");
            final IntArrayTag fadeColors = explosionTag.getIntArrayTag("FadeColors");
            final NumberTag trail = explosionTag.getNumberTag("Trail");
            final NumberTag flicker = explosionTag.getNumberTag("Flicker");
            final FireworkExplosion explosion = new FireworkExplosion(
                shape != null ? shape.asInt() : 0,
                colors != null ? colors.getValue() : new int[0],
                fadeColors != null ? fadeColors.getValue() : new int[0],
                trail != null && trail.asBoolean(),
                flicker != null && flicker.asBoolean()
            );
            data.set(StructuredDataKey.FIREWORK_EXPLOSION, explosion);
        }

        updateWritableBookPages(data, tag);
        updateWrittenBookPages(data, tag);

        updateItemList(data, tag, "ChargedProjectiles", StructuredDataKey.CHARGED_PROJECTILES);
        updateItemList(data, tag, "Items", StructuredDataKey.BUNDLE_CONTENTS);

        updateEnchantments(data, tag, "Enchantments", StructuredDataKey.ENCHANTMENTS, (hideFlagsValue & 0x01) == 0);
        updateEnchantments(data, tag, "StoredEnchantments", StructuredDataKey.STORED_ENCHANTMENTS, (hideFlagsValue & 0x20) == 0);

        final NumberTag map = tag.getNumberTag("map");
        if (map != null) {
            data.set(StructuredDataKey.MAP_ID, map.asInt());
        }

        updateMapDecorations(data, tag.getListTag("Decorations", CompoundTag.class));

        // MAP_POST_PROCESSING is only used internally

        updateProfile(data, tag.get("SkullOwner"));

        // TODO
        //  StructuredDataKey.CAN_PLACE_ON
        //  StructuredDataKey.CAN_BREAK
        //  StructuredDataKey.ATTRIBUTE_MODIFIERS
        //  StructuredDataKey.CREATIVE_SLOT_LOCK
        //  StructuredDataKey.INTANGIBLE_PROJECTILE
        //  StructuredDataKey.DYED_COLOR
        //  StructuredDataKey.POTION_CONTENTS
        //  StructuredDataKey.SUSPICIOUS_STEW_EFFECTS
        //  StructuredDataKey.TRIM
        //  StructuredDataKey.DEBUG_STICK_STATE
        //  StructuredDataKey.BUCKET_ENTITY_DATA
        //  StructuredDataKey.BLOCK_ENTITY_DATA
        //  StructuredDataKey.INSTRUMENT
        //  StructuredDataKey.RECIPES
        //  StructuredDataKey.LODESTONE_TARGET
        //  StructuredDataKey.FIREWORKS
        //  StructuredDataKey.NOTE_BLOCK_SOUND
        //  StructuredDataKey.BANNER_PATTERNS
        //  StructuredDataKey.BASE_COLOR
        //  StructuredDataKey.POT_DECORATIONS
        //  StructuredDataKey.CONTAINER
        //  StructuredDataKey.BEES
        //  StructuredDataKey.LOCK
        //  StructuredDataKey.CONTAINER_LOOT

        // Add the original as custom data, to be re-used for creative clients as well
        if (addMarker) {
            tag.putBoolean(nbtTagName(), true);
        }
        data.set(StructuredDataKey.CUSTOM_DATA, tag);
        return item;
    }

    private void updateWritableBookPages(final StructuredDataContainer data, final CompoundTag tag) {
        final ListTag pagesTag = tag.getListTag("pages");
        final CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");
        if (pagesTag == null) {
            return;
        }

        final List<FilterableString> pages = new ArrayList<>();
        for (int i = 0; i < pagesTag.size(); i++) {
            final Tag page = pagesTag.get(i);
            if (!(page instanceof StringTag)) {
                continue;
            }

            String filtered = null;
            if (filteredPagesTag != null) {
                final StringTag filteredPage = filteredPagesTag.getStringTag(String.valueOf(i));
                if (filteredPage != null) {
                    filtered = filteredPage.getValue();
                }
            }
            pages.add(new FilterableString(((StringTag) page).getValue(), filtered));

        }
        data.set(StructuredDataKey.WRITABLE_BOOK_CONTENT, pages.toArray(new FilterableString[0]));
    }

    private void updateWrittenBookPages(final StructuredDataContainer data, final CompoundTag tag) {
        final ListTag pagesTag = tag.getListTag("pages");
        final CompoundTag filteredPagesTag = tag.getCompoundTag("filtered_pages");
        if (pagesTag == null) {
            return;
        }

        final List<FilterableComponent> pages = new ArrayList<>();
        for (int i = 0; i < pagesTag.size(); i++) {
            final Tag page = pagesTag.get(i);
            if (!(page instanceof StringTag)) {
                continue;
            }

            Tag filtered = null;
            if (filteredPagesTag != null) {
                final StringTag filteredPage = filteredPagesTag.getStringTag(String.valueOf(i));
                if (filteredPage != null) {
                    filtered = ComponentUtil.jsonStringToTag(filteredPage.getValue());
                }
            }

            final Tag parsedPage = ComponentUtil.jsonStringToTag(((StringTag) page).getValue());
            pages.add(new FilterableComponent(parsedPage, filtered));
        }

        final StringTag title = tag.getStringTag("title");
        final StringTag filteredTitle = tag.getStringTag("filtered_title");
        final StringTag author = tag.getStringTag("author");
        final NumberTag generation = tag.getNumberTag("generation");
        final NumberTag resolved = tag.getNumberTag("resolved");
        final WrittenBook writtenBook = new WrittenBook(
            new FilterableString(title != null ? title.getValue() : "", filteredTitle != null ? filteredTitle.getValue() : null),
            author != null ? author.getValue() : "",
            generation != null ? generation.asInt() : 0,
            pages.toArray(new FilterableComponent[0]),
            resolved != null && resolved.asBoolean()
        );
        data.set(StructuredDataKey.WRITTEN_BOOK_CONTENT, writtenBook);
    }

    private void updateItemList(final StructuredDataContainer data, final CompoundTag tag, final String key, final StructuredDataKey<Item[]> dataKey) {
        final ListTag chargedProjectiles = tag.getListTag(key);
        if (chargedProjectiles == null) {
            return;
        }

        final List<Item> items = new ArrayList<>();
        for (final Tag item : chargedProjectiles) {
            if (!(item instanceof CompoundTag)) {
                continue;
            }
            items.add(itemFromTag((CompoundTag) item));
        }
        data.set(dataKey, items.toArray(new Item[0]));
    }

    private Item itemFromTag(final CompoundTag item) {
        final StringTag id = item.getStringTag("id");
        final NumberTag count = item.getNumberTag("Count");
        final CompoundTag tag = item.getCompoundTag("tag");
        return handleItemToClient(new DataItem(0, count.asByte(), (short) 0, tag)); // TODO unmapped id from key
    }

    private void updateEnchantments(final StructuredDataContainer data, final CompoundTag tag, final String key,
                                    final StructuredDataKey<Enchantments> newKey, final boolean show) {
        final ListTag<CompoundTag> enchantmentsTag = tag.getListTag(key, CompoundTag.class);
        if (enchantmentsTag == null) {
            return;
        }

        tag.remove(key);

        final Enchantments enchantments = new Enchantments(new Int2IntOpenHashMap(), show);
        for (final CompoundTag enchantment : enchantmentsTag) {
            final StringTag id = enchantment.getStringTag("id");
            final NumberTag lvl = enchantment.getNumberTag("lvl");
            if (id == null || lvl == null) {
                continue;
            }

            final int intId = EnchantmentMappings.id(id.getValue());
            if (intId == -1) {
                continue;
            }

            enchantments.enchantments().put(intId, lvl.asInt());
        }

        data.set(newKey, enchantments);

        // Add glint if none of the enchantments were valid
        if (enchantments.size() == 0 && !enchantmentsTag.isEmpty()) {
            data.set(StructuredDataKey.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
    }

    private void updateProfile(final StructuredDataContainer data, final Tag skullOwnerTag) {
        final List<GameProfile.Property> properties = new ArrayList<>(1);
        if (skullOwnerTag instanceof StringTag) {
            final String name = ((StringTag) skullOwnerTag).getValue();
            data.set(StructuredDataKey.PROFILE, new GameProfile(name, null, EMPTY_PROPERTIES));
        } else if (skullOwnerTag instanceof CompoundTag) {
            final CompoundTag skullOwner = (CompoundTag) skullOwnerTag;
            final StringTag nameTag = skullOwner.getStringTag("Name");
            final String name = nameTag != null ? nameTag.getValue() : "";

            final IntArrayTag idTag = skullOwner.getIntArrayTag("Id");
            UUID uuid = null;
            if (idTag != null) {
                uuid = UUIDUtil.fromIntArray(idTag.getValue());
            }

            final CompoundTag propertiesTag = skullOwner.getCompoundTag("Properties");
            if (propertiesTag != null) {
                updateProperties(propertiesTag, properties);
            }
            data.set(StructuredDataKey.PROFILE, new GameProfile(name, uuid, properties.toArray(EMPTY_PROPERTIES)));
        }
    }

    private void updateProperties(final CompoundTag propertiesTag, final List<GameProfile.Property> properties) {
        for (final Map.Entry<String, Tag> entry : propertiesTag.entrySet()) {
            if (!(entry.getValue() instanceof ListTag)) {
                continue;
            }

            for (final Tag propertyTag : (ListTag<?>) entry.getValue()) {
                if (!(propertyTag instanceof CompoundTag)) {
                    continue;
                }

                final StringTag valueTag = ((CompoundTag) propertyTag).getStringTag("Value");
                final StringTag signatureTag = ((CompoundTag) propertyTag).getStringTag("Signature");
                final GameProfile.Property property = new GameProfile.Property(
                    entry.getKey(),
                    valueTag != null ? valueTag.getValue() : "",
                    signatureTag != null ? signatureTag.getValue() : null
                );
                properties.add(property);
            }
        }
    }

    private void updateMapDecorations(final StructuredDataContainer data, final ListTag<CompoundTag> decorationsTag) {
        if (decorationsTag == null) {
            return;
        }

        final CompoundTag updatedDecorationsTag = new CompoundTag();
        for (final CompoundTag decorationTag : decorationsTag) {
            final StringTag idTag = decorationTag.getStringTag("id");
            final String id = idTag != null ? idTag.asRawString() : "";
            final NumberTag typeTag = decorationTag.getNumberTag("type");
            final int type = typeTag != null ? typeTag.asInt() : 0;
            final NumberTag xTag = decorationTag.getNumberTag("x");
            final NumberTag zTag = decorationTag.getNumberTag("z");
            final NumberTag rotationTag = decorationTag.getNumberTag("rot");

            final CompoundTag updatedDecorationTag = new CompoundTag();
            updatedDecorationTag.putString("type", MapDecorationMappings.mapDecoration(type));
            updatedDecorationTag.putDouble("x", xTag != null ? xTag.asDouble() : 0);
            updatedDecorationTag.putDouble("z", zTag != null ? zTag.asDouble() : 0);
            updatedDecorationTag.putFloat("rotation", rotationTag != null ? rotationTag.asFloat() : 0);
            updatedDecorationsTag.put(id, updatedDecorationTag);
        }

        data.set(StructuredDataKey.MAP_DECORATIONS, updatedDecorationsTag);
    }

    private void updateDisplay(final StructuredDataContainer data, final CompoundTag displayTag, final int hideFlags) {
        if (displayTag == null) {
            return;
        }

        final NumberTag mapColorTag = displayTag.getNumberTag("MapColor");
        if (mapColorTag != null) {
            data.set(StructuredDataKey.MAP_COLOR, mapColorTag.asInt());
        }

        final StringTag nameTag = displayTag.getStringTag("Name");
        if (nameTag != null) {
            data.set(StructuredDataKey.CUSTOM_NAME, ComponentUtil.jsonStringToTag(nameTag.getValue()));
        }

        final ListTag loreTag = displayTag.getListTag("Lore");
        if (loreTag != null) {
            final List<Tag> updatedLore = new ArrayList<>();
            for (final Tag loreEntry : loreTag) {
                if (loreEntry instanceof StringTag) {
                    updatedLore.add(ComponentUtil.jsonStringToTag(((StringTag) loreEntry).getValue()));
                }
            }
            data.set(StructuredDataKey.LORE, updatedLore.toArray(new Tag[0]));
        }

        final NumberTag colorTag = displayTag.getNumberTag("color");
        if (colorTag != null) {
            data.set(StructuredDataKey.DYED_COLOR, new DyedColor(colorTag.asInt(), (hideFlags & 0x40) == 0));
        }
    }
}
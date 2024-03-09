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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.data;

import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TrimPatterns1_20_3 {

    private static final KeyMappings PATTERNS = new KeyMappings(
        "sentry",
        "dune",
        "coast",
        "wild",
        "ward",
        "eye",
        "vex",
        "tide",
        "snout",
        "rib",
        "spire",
        "wayfinder",
        "shaper",
        "silence",
        "raiser",
        "host"
    );

    public static @Nullable String idToKey(final int id) {
        return PATTERNS.idToKey(id);
    }

    public static int keyToId(final String pattern) {
        return PATTERNS.keyToId(pattern);
    }
}

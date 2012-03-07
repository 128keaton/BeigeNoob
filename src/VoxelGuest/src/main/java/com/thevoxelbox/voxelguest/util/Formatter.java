/*
 * Copyright (C) 2011 - 2012, psanker and contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this list of 
 *   conditions and the following 
 * * Redistributions in binary form must reproduce the above copyright notice, this list of 
 *   conditions and the following disclaimer in the documentation and/or other materials 
 *   provided with the distribution.
 * * Neither the name of The VoxelPlugineering Team nor the names of its contributors may be 
 *   used to endorse or promote products derived from this software without specific prior 
 *   written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.thevoxelbox.voxelguest.util;

import com.thevoxelbox.voxelguest.players.GuestPlayer;

public abstract class Formatter {
    
    public static Formatter selectFormatter(Class<? extends Formatter> cls) {
        try {
            Formatter formatter = cls.newInstance();
            return formatter;
        } catch (Throwable t) {
            return null;
        }
    }
    
    public static String encodeColors(String input) {
        for (FormatColors color : FormatColors.values()) {
            input = input.replace(color.getColorCode(), color.getBukkitColorCode());
        }
        
        return input;
    }
    
    public abstract String[] format(String in, GuestPlayer gp);
}

enum FormatColors {
    WHITE("&f"),
    DARK_BLUE("&1"),
    DARK_GREEN("&2"),
    TEAL("&3"),
    DARK_RED("&4"),
    PURPLE("&5"),
    ORANGE("&6"),
    LIGHT_GREY("&7"),
    DARK_GREY("&8"),
    INDIGO("&9"),
    LIGHT_GREEN("&a"),
    CYAN("&b"),
    RED("&c"),
    PINK("&d"),
    YELLOW("&e"),
    BLACK("&0");
    
    private String color;
    
    private FormatColors(String c) {
        color = c;
    }
    
    public String getColorCode() {
        return color;
    }
    
    public String getBukkitColorCode() {
        return color.replace("&", "\u00A7");
    }
 }

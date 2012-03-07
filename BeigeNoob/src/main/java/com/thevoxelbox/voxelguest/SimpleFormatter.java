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

package com.thevoxelbox.voxelguest;

import com.thevoxelbox.voxelguest.util.Formatter;
import com.thevoxelbox.voxelguest.players.GuestPlayer;
import com.thevoxelbox.voxelguest.util.Configuration;
import org.bukkit.Bukkit;

public class SimpleFormatter extends Formatter {
    
    /*
     * --------------
     * FORMAT HANDLES
     * --------------
     * $n = name of the player
     * $name = long form of $n
     * $g = group (INDEX 0) of the player
     * $group = long form of $g
     * $gc = code for that group (if desired)
     * $nonline = number of people online
     * 
     * Would be best to extend from this implementation
     * so you don't have to rewrite the group crap again
     * 
     */
    
    @Override
    public String[] format(String input, GuestPlayer gp) {
        String copy = input;
        boolean guestPlayerParcing;
        AsshatMitigationModule amm;
        
        guestPlayerParcing = (gp == null) ? false : true;
        
        if (guestPlayerParcing) {
            
            if (gp.getGroups() != null) {
                String group = gp.getGroups()[0];
                Configuration config = VoxelGuest.getGroupManager().getGroupConfiguration(group);
                String groupID = config.getString("group-id");

                copy = copy.replace("$group", group);
                if (groupID != null) {copy = copy.replace("$gc", groupID);}
                copy = copy.replace("$g", group);
            }
            
            copy = copy.replace("$nonline", Integer.toString(Bukkit.getOnlinePlayers().length));
            copy = copy.replace("$name", gp.getPlayer().getName());
            copy = copy.replace("$n", gp.getPlayer().getName());
        }
        
        copy = encodeColors(copy);
        
        if (copy.contains("\n")) {
            String[] copies = copy.split("\n");
            return copies;
        } else {
            return new String[] {copy};
        }
    }
}

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

package com.thevoxelbox.voxelguest.commands;

import com.thevoxelbox.commands.Command;
import com.thevoxelbox.commands.CommandPermission;
import com.thevoxelbox.commands.Subcommands;
import com.thevoxelbox.voxelguest.AFKModule;
import com.thevoxelbox.voxelguest.VanishModule;
import com.thevoxelbox.voxelguest.VoxelGuest;
import com.thevoxelbox.voxelguest.modules.ModuleException;
import com.thevoxelbox.voxelguest.modules.ModuleManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class MiscellaneousCommands {
    private final String AFK = "§8[§9AFK§8]";
    private final String FAKEQUIT = "§8[§cFQ§8]";
    private final String COMMA = "§6,";
    
    
    @Command(aliases={"who", "online", "list", "readlist"},
            bounds={0,1},
            help="To list all online players, type §c/who",
            playerOnly=true)
    @Subcommands(arguments={"-f"},
            permission={"voxelguest.miscellaneous.list.admin"})
    @CommandPermission(permission="voxelguest.miscellaneous.list.list")
    public void who(CommandSender cs, String[] args) {
        HashMap<String, String[]> storage = new HashMap<String, String[]>();
        
        if (args[0].equalsIgnoreCase("-f")) {
            String header = "";
            
            for (String group : VoxelGuest.getGroupManager().getRegisteredGroups()) {
                List<String> players = VoxelGuest.getGroupManager().getPlayerListForGroup(group);
                String groupId = VoxelGuest.getGroupManager().getGroupConfiguration(group).getString("group-id");
                
                if (groupId == null)
                    groupId = "§fG";
                
                String[] list = new String[players.size()];
                
                Iterator<String> it = players.listIterator();
                int x = 0;
                while (it.hasNext()) {
                    String player = it.next();
                    list[x] = player;
                    x++;
                }
                
                boolean colorSwitch = false;
                boolean afk = false;
                boolean fakequit = false;
                
                for (int i = 0; i < list.length; i++) {
                    String str = list[i];
                    
                    try {
                        if (ModuleManager.getManager().getModule(AFKModule.class).isEnabled()) {
                            AFKModule module = (AFKModule) ModuleManager.getManager().getModule(AFKModule.class);
                            afk = module.isAFK(Bukkit.getPlayer(str));
                        }
                        
                        if (ModuleManager.getManager().getModule(VanishModule.class).isEnabled()) {
                            VanishModule module = (VanishModule) ModuleManager.getManager().getModule(VanishModule.class);
                            fakequit = module.isInFakequit(Bukkit.getPlayer(str));
                        }
                    } catch (ModuleException ex) {
                        // continue
                    }
                    
                    str = ((fakequit) ? FAKEQUIT : "") + ((afk) ? AFK : "") + ((colorSwitch) ? "§f" : "§7") + str;
                    colorSwitch = !colorSwitch;
                    list[i] = str;
                    
                    header = header + "§8[" + groupId + ":" + list.length + "§8] ";
                }
                
                storage.put(groupId, list);
            }
            
            cs.sendMessage("§8------------------------------");
            cs.sendMessage(header.trim());
            
            for (Map.Entry<String, String[]> entry : storage.entrySet()) {
                sendGroupStrings(cs, Arrays.asList(entry.getValue()), ("§8[" + entry.getKey() + "§8]"));
            }
            cs.sendMessage("§8------------------------------");
        }
        
        String header = "";
            
        for (String group : VoxelGuest.getGroupManager().getRegisteredGroups()) {
            List<String> players = VoxelGuest.getGroupManager().getPlayerListForGroup(group);
            String groupId = VoxelGuest.getGroupManager().getGroupConfiguration(group).getString("group-id");
            
            Iterator<String> itr = players.listIterator();
            List<String> toRemove = new ArrayList<String>();
            
            while (itr.hasNext()) {
                try {
                    String str = itr.next();
                    
                    if (ModuleManager.getManager().getModule(VanishModule.class).isEnabled()) {
                        VanishModule vanish = (VanishModule) ModuleManager.getManager().getModule(VanishModule.class);
                        
                        if (vanish.isInFakequit(Bukkit.getPlayer(str)))
                            toRemove.add(str);
                    }
                } catch (ModuleException ex) {
                    break;
                }
            }
            
            players.removeAll(toRemove);

            if (groupId == null)
                groupId = "§fG";

            String[] list = new String[players.size()];

            Iterator<String> it = players.listIterator();
            int x = 0;
            while (it.hasNext()) {
                String player = it.next();
                list[x] = player;
                x++;
            }

            boolean colorSwitch = false;
            boolean afk = false;

            for (int i = 0; i < list.length; i++) {
                String str = list[i];

                try {
                    if (ModuleManager.getManager().getModule(AFKModule.class).isEnabled()) {
                        AFKModule module = (AFKModule) ModuleManager.getManager().getModule(AFKModule.class);
                        afk = module.isAFK(Bukkit.getPlayer(str));
                    }
                } catch (ModuleException ex) {
                    // continue
                }

                str = ((afk) ? AFK : "") + ((colorSwitch) ? "§f" : "§7") + str;
                colorSwitch = !colorSwitch;
                list[i] = str;

                header = header + "§8[" + groupId + ":" + list.length + "§8] ";
            }

            storage.put(groupId, list);
        }

        cs.sendMessage("§8------------------------------");
        cs.sendMessage(header.trim());

        for (Map.Entry<String, String[]> entry : storage.entrySet()) {
            sendGroupStrings(cs, Arrays.asList(entry.getValue()), ("§8[" + entry.getKey() + "§8]"));
        }
        cs.sendMessage("§8------------------------------");
    }
    
    private void sendGroupStrings(CommandSender cs, List<String> list, String groupHeader) {
        if (list == null || list.isEmpty()) {
            return;
        } else {
            boolean groupStart = false;
            String line = " ";
            Iterator<String> it = list.listIterator();
            
            while (it.hasNext()) {
                String str = it.next();
                
                if (line.length() + (str.length() + COMMA.length()) > 70) {
                    if (!groupStart) {
                        groupStart = true;
                        cs.sendMessage(groupHeader + line.substring(0, line.length() - 2));
                        line = " ";
                    } else {
                        cs.sendMessage(groupHeader + line.substring(0, line.length() - 2));
                        line = " ";
                    }
                }
                
                line += (str + COMMA);
            }
            
            if (!line.isEmpty()) {
                if (!groupStart) {
                    cs.sendMessage(groupHeader + line.substring(0, line.length() - 2));
                } else {
                    cs.sendMessage(groupHeader + line.substring(0, line.length() - 2));
                }
            }
        }
    }
}

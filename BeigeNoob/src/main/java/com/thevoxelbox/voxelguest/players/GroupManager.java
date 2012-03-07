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

package com.thevoxelbox.voxelguest.players;

import com.thevoxelbox.permissions.PermissionsManager;
import com.thevoxelbox.voxelguest.util.Configuration;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;

public class GroupManager {
    
    protected static HashMap<String, Configuration> groupMap = new HashMap<String, Configuration>();
    protected static HashMap<String, List<String>> playerMap = new HashMap<String, List<String>>();
    
    // Basic group defaults
    private final String groupName = "Group";
    private final String groupIcon = "§fG";
    
    public GroupManager() {
        File dir = new File("plugins/VoxelGuest/data/groups/");
        
        if (!dir.isDirectory()) {
            dir.mkdirs();
            return;
        }
        
        String[] files = dir.list();
        
        for (String file : files) {
            if (file.endsWith(".properties")) {
                String f = file.replace(".properties", "");
                Configuration config = new Configuration(f, "/groups");
                groupMap.put(f, config);
            }
        }
    }
    
    public Configuration getGroupConfiguration(String name) {
        if (groupMap.containsKey(name))
            return groupMap.get(name);
        
        return null;
    }
    
    public void setGroupConfiguration(String name, Configuration config) {
        groupMap.put(name, config);
    }
    
    public String findGroup(String key, Object value) throws GroupNotFoundException {
        for (Map.Entry<String, Configuration> entry : groupMap.entrySet()) {
            Configuration config = entry.getValue();
            
            if (config.getEntry(key) != null && value.equals(config.getEntry(key)))
                return entry.getKey();
        }
        
        throw new GroupNotFoundException("No group found for key-value pair");
    }
    
    public void saveGroupConfigurations() {
        for (Map.Entry<String, Configuration> entry : groupMap.entrySet()) {
            saveGroupConfiguration(entry.getKey());
        }
    }
    
    public void saveGroupConfiguration(String name) {
        Configuration config = groupMap.get(name);
        config.save();
    }
    
    public void addPlayerToGroupMap(Player p) {
        String group = PermissionsManager.getHandler().getGroups(p.getName())[0];
        
        if (group == null)
            group = "Unknown";
        
        List<String> list = playerMap.get(group);
        
        if (list == null || list.isEmpty()) {
            List<String> newList = new ArrayList<String>();
            newList.add(p.getName());
            playerMap.put(group, newList);
        } else {
            if (!list.contains(p.getName())) {
                list.add(p.getName());
                playerMap.put(group, list);
            }
        }
    }
    
    public void removePlayerFromGroupMap(Player p) {
        String group = PermissionsManager.getHandler().getGroups(p.getName())[0];
        
        if (group == null)
            group = "Unknown";
        
        List<String> list = playerMap.get(group);
        
        if (list.isEmpty() || list == null) {
            // Do nothing
        } else {
            if (list.contains(p.getName())) {
                list.remove(p.getName());
                playerMap.put(group, list);
            }
        }
    }
    
    public List<String> getPlayerListForGroup(String group) {
        return playerMap.get(group);
    }
    
    public List<String> getRegisteredGroups() {
        List<String> l = new ArrayList<String>();
        
        for (Map.Entry<String, Configuration> entry : groupMap.entrySet()) {
            if (!l.contains(entry.getKey()))
                l.add(entry.getKey());
        }
        
        return l;
    }
}

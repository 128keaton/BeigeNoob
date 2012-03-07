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

package com.thevoxelbox.permissions;

import com.thevoxelbox.voxelguest.VoxelGuest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;

public class GuestPermissionsHandler extends PermissionsHandler {

    private final File file = new File("plugins/VoxelGuest/permissions.yml");
    private final YamlConfiguration yamlPerms = new YamlConfiguration();
    
    private final String header = "#########################################\n" +
            "#\n" +
            "# =============================\n" +
            "# VOXELGUEST 4 PERMISSIONS FILE\n" +
            "#\n" +
            "# Built by: psanker\n" +
            "# =============================\n" +
            "#\n" +
            "# This is a standard permissions system for VoxelGuest, which VoxelGuest handles after all\n" +
            "# other systems are disregarded OR the config explicitly says to use the VG perms system.\n" +
            "# This file accepts both UNIX LF line endings (\"\\n\") and Windows line endings (\"\\r\\n\").\n" +
            "#\n" +
            "# BE SURE TO USE SPACES AND NOT TABS. Use a good text editor to edit this file.\n" +
            "# Notepad++ is recommended for Windows, and TextMate is recommended for Mac.\n" +
            "#\n" +
            "# Comments start with a pound sign (like the beginning of this and the above lines).\n" +
            "# The YAML processor will ignore these lines. To verify your YAML syntax, please use\n" +
            "# http://yaml-online-parser.appspot.com/\n" +
            "#\n" +
            "# Please note: There is no multi-world and multi-group support\n" +
            "#\n" +
            "#########################################\n" +
            "\n";
    
    @Override
    public PermissionsHandler initialize(Server server) {
        return new GuestPermissionsHandler(server);
    }
    
    public GuestPermissionsHandler(Server server) {
        try {
            
            this.server = server;
        
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            
            yamlPerms.load(file);
            yamlPerms.options().header(header);
            queueSave();
            
        } catch (Throwable t) {
            VoxelGuest.log("Error in loading permissions file", 2);
            t.printStackTrace();
            return;
        }
    }

    @Override
    public String getDetectionMessage() {
        return "Using built-in permissions system";
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        List<String> list = yamlPerms.getStringList("user." + name + ".permissions");
        String group = yamlPerms.getString("user." + name + ".group");
        
        if (list.contains("*") || list.contains("all"))
            return true;
        
        if (processNodes(list, permission))
            return true;
        
        if (group != null)
            return hasGroupPermissionRecursive(group, permission);
        
        return false;
    }

    @Override
    public boolean hasPermission(String world, String name, String permission) {
        return hasPermission(name, permission);
    }

    @Override
    public boolean inGroup(String name, String group) {
        String userGroup = yamlPerms.getString("user." + name + ".group");
        
        return userGroup.equalsIgnoreCase(group);
    }

    @Override
    public String[] getGroups(String name) {
        String[] out = new String[1];
        out[0] = yamlPerms.getString("user." + name + ".group");
        
        if (out[0] == null)
            return null;
        else
            return out;
    }
    
    private boolean hasGroupPermissionRecursive(String group, String permission) {
        List<String> list = yamlPerms.getStringList("group." + group + ".permissions");
        String parent = yamlPerms.getString("group." + group + ".inherits");
        
        if (list.contains("*") || list.contains("all"))
            return true;
        
        if (list == null)
            return false;
        else if (processNodes(list, permission))
            return true;
        else if (parent != null)
            return hasGroupPermissionRecursive(parent, permission);
        
        return false;
    }
    
    private boolean processNodes(List<String> permissions, String permission) {
        String[] nodes = permission.split(".");
        int maxIndex = nodes.length;
        String concat = "";
        
        for (int i = 0; i < maxIndex; i++) {
            String node = nodes[i];
            concat = concat + "." + node;
            
            if ((node.equals("*") || node.equals("all")) && (i <= (maxIndex - 1))) {
                return true;
            }
            
            if (permissions.contains(concat))
                return true;
        }
        
        return false;
    }

    @Override
    public void givePermission(String world, String name, String permission) {
        givePermission(name, permission);
    }

    @Override
    public void givePermission(String name, String permission) {
        List<String> list = yamlPerms.getStringList("user." + name + ".permissions");
        
        if (!list.isEmpty() || list != null) {
            list.add(permission);
            queueSave("user." + name + ".permissions", list);
        } else {
            List<String> l = new ArrayList<String>();
            l.add(permission);
            queueSave("user." + name + ".permissions", l);
        }
    }

    @Override
    public void removePermission(String world, String name, String permission) {
        removePermission(name, permission);
    }

    @Override
    public void removePermission(String name, String permission) {
        List<String> list = yamlPerms.getStringList("user." + name + ".permissions");
        
        if (!list.isEmpty() || list != null) {
            if (list.contains(permission)) {
                list.remove(permission);
                queueSave("user." + name + ".permissions", list);
            }
        }
    }

    @Override
    public void addGroup(String username, String groupname) {
        queueSave("user." + username + ".group", groupname);
    }

    @Override
    public void removeGroup(String username, String groupname) {
        return; // Overwrite provided in addGroup()
    }

    @Override
    public void giveGroupPermission(String world, String name, String permission) {
        giveGroupPermission(name, permission);
    }

    @Override
    public void giveGroupPermission(String name, String permission) {
        List<String> list = yamlPerms.getStringList("group." + name + ".permissions");
        
        if (!list.isEmpty() || list != null) {
            list.add(permission);
            queueSave("group." + name + ".permissions", list);
        } else {
            List<String> l = new ArrayList<String>();
            l.add(permission);
            queueSave("group." + name + ".permissions", l);
        }
    }

    @Override
    public void removeGroupPermission(String world, String name, String permission) {
        removeGroupPermission(name, permission);
    }

    @Override
    public void removeGroupPermission(String name, String permission) {
        List<String> list = yamlPerms.getStringList("group." + name + ".permissions");
        
        if (!list.isEmpty() || list != null) {
            if (list.contains(permission)) {
                list.remove(permission);
                queueSave("group." + name + ".permissions", list);
            }
        }
    }
    
    private synchronized void queueSave() {
        try {
            yamlPerms.save(file);
        } catch (IOException ex) {
            return;
        }
    }
    
    private synchronized void queueSave(String key, Object value) {
        try {
            yamlPerms.set(key, value);
            yamlPerms.save(file);
        } catch (IOException ex) {
            return;
        }
    }
}

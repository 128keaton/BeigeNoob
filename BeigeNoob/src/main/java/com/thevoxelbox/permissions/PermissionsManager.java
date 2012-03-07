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

import com.thevoxelbox.voxelguest.util.Configuration;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 *
 * @author psanker
 * 
 * Inspired by WEPIF
 * 
 */
public class PermissionsManager implements Listener {
    private final Server server;
    private static String tag;
    private final Configuration configuration;
    protected static PermissionsHandler handler;
    
    protected static boolean multigroup = false;
    protected static boolean multiworld = false;
    
    public PermissionsManager(Server s, String pluginPrefix, Configuration config) {
        server = s;
        tag = pluginPrefix;
        configuration = config;
        
        multigroup = configuration.getBoolean("permissions-multigroup");
        multiworld = configuration.getBoolean("permissions-multiworld");
    }
    
    protected Class<? extends PermissionsHandler>[] availableHandlers = new Class[] {
        PermissionsExHandler.class,
        BPermissionsHandler.class,
        DinnerpermsHandler.class,
        GuestPermissionsHandler.class
    };
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        String[] plugins = {"PermissionsEx", "bPermissions"};
        
        if (Arrays.asList(plugins).contains(event.getPlugin().getDescription().getName())) {
            registerActiveHandler();
        }
    }
    
    public void registerActiveHandler() {
        for (Class<? extends PermissionsHandler> handlerClass : availableHandlers) {
            try {
                Method init = handlerClass.getMethod("initialize", Server.class);

                PermissionsHandler _handler = (PermissionsHandler) init.invoke(null, this.server);

                if (_handler != null) {
                    if (_handler instanceof GuestPermissionsHandler)
                        continue;
                    
                    handler = _handler;
                    log(handler.getDetectionMessage(), 0);
                    break;
                }
                
            } catch (Throwable t) {
                continue;
            }
        }
        
        if (handler == null) {
            handler = new GuestPermissionsHandler(server);
            log(handler.getDetectionMessage(), 0);
        }
    }
    
    public static PermissionsHandler getHandler() {
        return handler;
    }
    
    public boolean hasPermission(String name, String permission) {
        return handler.hasPermission(name, permission);
    }
    
    public boolean hasPermission(String world, String name, String permission) {
        return handler.hasPermission(world, name, permission);
    }
    
    public boolean inGroup(String name, String group) {
        return handler.inGroup(name, group);
    }
    
    public String[] getGroups(String name) {
        return handler.getGroups(name);
    }

    public String getDetectionMessage() {
        return handler.getDetectionMessage();
    }
    
    public static boolean hasMultiGroupSupport() {
        return multigroup;
    }
    
    public static boolean hasMultiWorldSupport() {
        return multiworld;
    }
    
    public static void log(String str, int importance) {
        switch (importance) {
            case 0:
                Logger.getLogger("Mincraft").info(tag + " " + str);
                return;
            case 1:
                Logger.getLogger("Mincraft").warning(tag + " " + str);
                return;
            case 2:
                Logger.getLogger("Mincraft").severe(tag + " " + str);
                return;
            default:
                Logger.getLogger("Mincraft").info(tag + " " + str);
                return;
        }
    }
}

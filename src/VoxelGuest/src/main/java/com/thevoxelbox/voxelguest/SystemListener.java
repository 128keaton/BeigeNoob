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

import com.thevoxelbox.voxelguest.modules.ModuleSystemListener;
import com.thevoxelbox.voxelguest.players.GuestPlayer;
import com.thevoxelbox.voxelguest.util.FormatException;
import com.thevoxelbox.voxelguest.util.Formatter;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SystemListener extends ModuleSystemListener {
    
    @EventHandler(priority = EventPriority.HIGH) 
    public void onPlayerJoin(PlayerJoinEvent event) {
        GuestPlayer gp = VoxelGuest.registerPlayer(event.getPlayer());
        
        try {
            String format = VoxelGuest.getConfigData().getString("join-message-format");
            event.setJoinMessage(formatJoinQuitKickMessage(format, gp));
        } catch (FormatException ex) {
            VoxelGuest.log(ex.getMessage(), 1);
        } catch (NullPointerException ex) {
            event.setJoinMessage(ChatColor.YELLOW + gp.getPlayer().getName() + " joined");
            ex.printStackTrace();
        }
        
        super.processModuleEvents(event);
        
        if (event.getPlayer().isOnline())
            VoxelGuest.getGroupManager().addPlayerToGroupMap(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        GuestPlayer gp = VoxelGuest.getGuestPlayer(event.getPlayer());
        VoxelGuest.unregsiterPlayer(gp);
        gp.saveData(VoxelGuest.getPluginId(VoxelGuest.getInstance()));
        VoxelGuest.getGroupManager().removePlayerFromGroupMap(event.getPlayer());
        
        try {
            String format = VoxelGuest.getConfigData().getString("leave-message-format");
            event.setQuitMessage(formatJoinQuitKickMessage(format, gp));
        } catch (FormatException ex) {
            VoxelGuest.log(ex.getMessage(), 1);
        } catch (NullPointerException ex) {
            event.setQuitMessage(ChatColor.YELLOW + gp.getPlayer().getName() + " left");
        }
        
        super.processModuleEvents(event);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent event) {
        GuestPlayer gp = VoxelGuest.getGuestPlayer(event.getPlayer());
        VoxelGuest.unregsiterPlayer(gp);
        gp.saveData(VoxelGuest.getPluginId(VoxelGuest.getInstance()));
        VoxelGuest.getGroupManager().removePlayerFromGroupMap(event.getPlayer());
        
        try {
            String format = VoxelGuest.getConfigData().getString("kick-message-format");
            event.setLeaveMessage(formatJoinQuitKickMessage(format, gp));
        } catch (FormatException ex) {
            VoxelGuest.log(ex.getMessage(), 1);
        } catch (NullPointerException ex) {
            event.setLeaveMessage(ChatColor.YELLOW + gp.getPlayer().getName() + " was kicked out");
        }
        
        super.processModuleEvents(event);
    }
    
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        super.processModuleEvents(event);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        super.processModuleEvents(event);
    }
    
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        super.processModuleEvents(event);
    }
    
    private String formatJoinQuitKickMessage(String format, GuestPlayer gp) throws FormatException {
        if (format.contains("\n"))
            throw new FormatException("Line feeds are not accepted in join/quit/kick messages");
        
        return Formatter.selectFormatter(SimpleFormatter.class).format(format, gp)[0];
    }
}

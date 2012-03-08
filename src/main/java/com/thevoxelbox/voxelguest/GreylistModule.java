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

import com.thevoxelbox.commands.Command;
import com.thevoxelbox.commands.CommandPermission;
import com.thevoxelbox.commands.Subcommands;
import com.thevoxelbox.permissions.PermissionsManager;
import com.thevoxelbox.voxelguest.modules.BukkitEventWrapper;
import com.thevoxelbox.voxelguest.modules.MetaData;
import com.thevoxelbox.voxelguest.modules.Module;
import com.thevoxelbox.voxelguest.modules.ModuleEvent;
import com.thevoxelbox.voxelguest.modules.ModuleEventPriority;
import com.thevoxelbox.voxelguest.players.GroupNotFoundException;
import com.thevoxelbox.voxelguest.players.GuestPlayer;
import com.thevoxelbox.voxelguest.util.FlatFileManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@MetaData(name="Greylist", description="Allows for the setup of a greylist system!")
public class GreylistModule extends Module {
    
    private static final List<String> greylist = new ArrayList<String>();
    private static final List<String> onlineGreys = new ArrayList<String>();
    
    private int streamTask = -1;
    private String streamPasswordHash;
    private int streamPort;
    private boolean couldNotConnect = false;
    private long disconnectTimestamp = -1;
    
    private int onlineGreylistLimit = -1;
    private boolean explorationMode = false;
    
    public GreylistModule() {
        super(GreylistModule.class.getAnnotation(MetaData.class));
    }
    
    @Override
    public void enable() {
        String[] list = FlatFileManager.load("greylist");
        
        if (list == null) {
            VoxelGuest.getConfigData().setBoolean("enable-greylist", false);
            super.disable();
            return;
        } else if (!VoxelGuest.getConfigData().getBoolean("enable-greylist")) {
            disable();
            return;
        }
        
        injectGreylist(list);
        
        if (VoxelGuest.getConfigData().getBoolean("enable-greylist-stream") && 
                VoxelGuest.getConfigData().getString("greylist-stream-password") != null &&
                VoxelGuest.getConfigData().getInt("greylist-stream-port") != -1) {
            
            streamPort = VoxelGuest.getConfigData().getInt("greylist-stream-port");
            streamPasswordHash = VoxelGuest.getConfigData().getString("greylist-stream-password");
            streamTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(VoxelGuest.getInstance(), new Runnable() {

                @Override
                public void run() {
                    if (couldNotConnect && (System.currentTimeMillis() - disconnectTimestamp) < 60000)
                        return;
                    
                    couldNotConnect = false;
                    
                    try {
                        ServerSocket socket = new ServerSocket(streamPort);
                        Future<List<String>> future = Bukkit.getScheduler().callSyncMethod(VoxelGuest.getInstance(), new StreamReader(socket.accept()));
                        
                        List<String> list = future.get();
                        
                        if (list == null || list.isEmpty())
                            return;
                        
                        injectGreylist(list);
                        announceGreylist(list);
                    } catch (InterruptedException ex) {
                        // Don't inject greylist
                    } catch (ExecutionException ex) {
                        // Don't inject greylist
                    } catch (IOException ex) {
                        VoxelGuest.log(name, "Could not connect to stream -- Retrying in a minute...", 1);
                        couldNotConnect = true;
                        disconnectTimestamp = System.currentTimeMillis();
                    }
                }
            }, 0L, 200L);
            
            explorationMode = VoxelGuest.getConfigData().getBoolean("exploration-mode");
            onlineGreylistLimit = VoxelGuest.getConfigData().getInt("greylist-online-limit");
        }
    }
    
    @Override
    public void disable() {
        Bukkit.getScheduler().cancelTask(streamTask);
        String[] toSave = new String[greylist.size()];
        toSave = greylist.toArray(toSave);
        FlatFileManager.save(toSave, "greylist");
        super.disable();
    }

    @Override
    public String getLoadMessage() {
        return "Greylist module loaded";
    }
    
    @Command(aliases={"greylist", "gl", "graylist"},
            bounds={1, -1})
    @CommandPermission(permission="voxelguest.greylist.admin.add")
    @Subcommands(arguments={"limit", "password"},
            permission={"voxelguest.greylist.admin.limit", "voxelguest.greylist.admin.password"})
    public void greylist(CommandSender cs, String[] args) {
        if (args[0].equalsIgnoreCase("limit")) {
            try {
                int newLimit = Integer.parseInt(args[1]);
                onlineGreylistLimit = newLimit;
                VoxelGuest.getConfigData().setInt("greylist-online-limit", onlineGreylistLimit);
                cs.sendMessage(ChatColor.GREEN + "Reset the online greylist limit to " + onlineGreylistLimit);
                return;
            } catch (NumberFormatException ex) {
                cs.sendMessage("Incorrect format. Try /gl limit [number]");
                return;
            }
        } else if (args[0].equalsIgnoreCase("password")) {
            String concat = "";
            
            for (int i = 1; i < args.length; i++) {
                if (i == (args.length - 1))
                    concat = concat + args[i];
                else
                    concat = concat + args[i] + " ";
            }
            
            String reverse = (new StringBuilder(concat)).reverse().toString();
            
            try {
                setPassword(name, reverse);
                cs.sendMessage(ChatColor.GREEN + "Set the greylist stream password to \"" + concat + "\"");
                return;
            } catch (CouldNotStoreEncryptedPasswordException ex) {
                cs.sendMessage(ChatColor.RED + "Could not store the greylist stream password");
            }
        }
        
        String user = args[0];
        injectGreylist(new String[] {user});
        cs.sendMessage(ChatColor.GREEN + "Added " + args[0] + " to the greylist");
    }
    
    @Command(aliases={"explorationmode"},
            bounds={0,0})
    @CommandPermission(permission="voxelguest.greylist.admin.exploration")
    public void explorationMode(CommandSender cs, String[] args) {
        explorationMode = !explorationMode;
        VoxelGuest.getConfigData().setBoolean("exploration-mode", explorationMode);
        cs.sendMessage(ChatColor.GREEN + "Exploration mode has been " + ((explorationMode) ? "enabled" : "disabled"));
    }
    
    @ModuleEvent(event=PlayerJoinEvent.class, priority=ModuleEventPriority.HIGHEST)
    public void onPlayerJoin(BukkitEventWrapper wrapper) {
        PlayerJoinEvent event = (PlayerJoinEvent) wrapper.getEvent();
        GuestPlayer gp = VoxelGuest.getGuestPlayer(event.getPlayer());
        
        if (PermissionsManager.getHandler().hasPermission(gp.getPlayer().getName(), "voxelguest.greylist.bypass")) {
            return;
        }
        
        if (!explorationMode) {
            if (!greylist.contains(gp.getPlayer().getName())) {
                gp.getPlayer().kickPlayer((VoxelGuest.getConfigData().getString("greylist-not-greylisted-kick-message") != null) ? VoxelGuest.getConfigData().getString("greylist-not-greylisted-kick-message") : "You are not greylisted on this server.");
                event.setJoinMessage("");
                wrapper.setCancelled(true);
                return;
            } else if (greylist.contains(gp.getPlayer().getName()) && !PermissionsManager.getHandler().hasPermission(gp.getPlayer().getName(), "voxelguest.greylist.bypass")) {
                if (onlineGreylistLimit > -1 && onlineGreys.size() >= onlineGreylistLimit) {
                    String str = VoxelGuest.getConfigData().getString("greylist-over-capacity-kick-message");
                    gp.getPlayer().kickPlayer((str != null) ? str : "The server is temporarily over guest capacity. Check back later.");
                    wrapper.setCancelled(true);
                    return;
                }
                
                if (!onlineGreys.contains(gp.getPlayer().getName()))
                    onlineGreys.add(gp.getPlayer().getName());
            }
        }
    }
    
    @ModuleEvent(event=PlayerQuitEvent.class)
    public void onPlayerQuit(BukkitEventWrapper wrapper) {
        PlayerQuitEvent event = (PlayerQuitEvent) wrapper.getEvent();
        
        if (!explorationMode && onlineGreys.contains(event.getPlayer().getName()))
            onlineGreys.remove(event.getPlayer().getName());
    }
    
    @ModuleEvent(event=PlayerKickEvent.class)
    public void onPlayerKick(BukkitEventWrapper wrapper) {
        PlayerKickEvent event = (PlayerKickEvent) wrapper.getEvent();
        
        if (!explorationMode && onlineGreys.contains(event.getPlayer().getName()))
            onlineGreys.remove(event.getPlayer().getName());
    }
    
    private void announceGreylist(String user) {
        Bukkit.getServer().broadcastMessage(ChatColor.GRAY + user + ChatColor.DARK_GRAY + " was added to the greylist.");
    }
    
    private void announceGreylist(List<String> users) {
        Iterator<String> it = users.listIterator();
        
        while (it.hasNext()) {
            String user = it.next();
            announceGreylist(user);
        }
    }
    
    private void injectGreylist(String[] strs) {
        if (strs == null)
            return;
        
        for (String str : strs) {
            if (!greylist.contains(str))
                greylist.add(str);
            
            try {
                String[] groups = PermissionsManager.getHandler().getGroups(str);
                String group = VoxelGuest.getGroupManager().findGroup("greylist", true);
                
                if (!PermissionsManager.hasMultiGroupSupport()) {
                    for (String _group : groups) {
                        PermissionsManager.getHandler().removeGroup(str, _group);
                    }
                    
                    PermissionsManager.getHandler().addGroup(str, group);
                } else {
                    PermissionsManager.getHandler().addGroup(str, group);
                }
                
            } catch (GroupNotFoundException ex) {
                // Just leave in greylist ... no group defined
            }
        }
    }
    
    private void injectGreylist(List<String> list) {
        if (list.isEmpty() || list == null)
            return;
        
        Iterator<String> it = list.listIterator();
        
        while (it.hasNext()) {
            String user = it.next();
            
            if (!greylist.contains(user))
                greylist.add(user);
            
            try {
                String[] groups = PermissionsManager.getHandler().getGroups(user);
                String group = VoxelGuest.getGroupManager().findGroup("greylist", true);
                
                if (!PermissionsManager.hasMultiGroupSupport()) {
                    for (String _group : groups) {
                        PermissionsManager.getHandler().removeGroup(user, _group);
                    }
                    
                    PermissionsManager.getHandler().addGroup(user, group);
                } else {
                    PermissionsManager.getHandler().addGroup(user, group);
                }
                
            } catch (GroupNotFoundException ex) {
                // Just leave in greylist ... no group defined
            }
        }
    }
    
    private String interpretStreamInput(String input) {
        String[] args = input.split("\\:");
        
        if (args[0].equals(streamPasswordHash)) {
            String user = args[1];
            boolean accepted = !(Boolean.parseBoolean(args[2]));
            
            if (accepted) {
                return user;
            }
        }
        
        return null;
    }
    
    class StreamReader implements Callable< List<String> > {
        private final Socket socket;

        public StreamReader(Socket s) {
            socket = s;
        }

        @Override
        public List<String> call() {
            List<String> list = new ArrayList<String>();
            String line = null;
            
            try {
                BufferedReader stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                while ((line = stream.readLine()) != null) {
                    String toAdd = interpretStreamInput(line);
                    
                    if (toAdd != null) {
                        if (!list.contains(toAdd))
                            list.add(line);
                    }
                }
                
                stream.close();
                return list;
            } catch (IOException ex) {
                return null;
            }
        }
    }
    
    public void setPassword(String name, String input) throws CouldNotStoreEncryptedPasswordException {
        byte[] shhash = new byte[40];
        String store = "";
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(input.getBytes("iso-8859-1"), 0, input.length());
            shhash = md.digest();
            store = convertToHex(shhash);
            
            VoxelGuest.getConfigData().setString("greylist-stream-password", store);
        } catch (NoSuchAlgorithmException e) {
            throw new CouldNotStoreEncryptedPasswordException("Fatal error in storage - NoSuchAlgorithmException");
        } catch (UnsupportedEncodingException e) {
            throw new CouldNotStoreEncryptedPasswordException("Fatal error in storage - UnsupportedEncodingException");
        }
    }
    
    private static String convertToHex(byte[] data) { 
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    }
}

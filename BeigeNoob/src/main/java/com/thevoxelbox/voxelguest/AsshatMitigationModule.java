package com.thevoxelbox.voxelguest;

import com.thevoxelbox.commands.Command;
import com.thevoxelbox.commands.CommandPermission;
import com.thevoxelbox.voxelguest.modules.BukkitEventWrapper;
import com.thevoxelbox.voxelguest.modules.MetaData;
import com.thevoxelbox.voxelguest.modules.Module;
import com.thevoxelbox.voxelguest.modules.ModuleConfiguration;
import com.thevoxelbox.voxelguest.modules.ModuleEvent;
import com.thevoxelbox.voxelguest.modules.Setting;
import com.thevoxelbox.voxelguest.util.PropertyManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;

/**
 *
 * @author Razorcane
 */
@MetaData(name = "Asshat Mitigator", description = "Major asshat handling.")
public class AsshatMitigationModule extends Module {

    public HashMap<String, Object> banned = (HashMap<String, Object>) PropertyManager.load("banned", "/asshatmitigation");
    public List<String> gagged = new ArrayList<String>();

    public AsshatMitigationModule() {
        super(AsshatMitigationModule.class.getAnnotation(MetaData.class));
    }

    @Override
    public void enable() {
        setConfiguration(new AsshatMitigationConfiguration(this));
        banned.clear();
        gagged.clear();
    }

    @Override
    public void disable() {
        PropertyManager.save("banned", banned, "/asshatmitigation");
    }

    @Override
    public String getLoadMessage() {
        return "Asshat Mitigator has been loaded.";
    }
    
    class AsshatMitigationConfiguration extends ModuleConfiguration {
        @Setting("default-asshat-reason") public String defaultAsshatReason = "§cAsshat";
        @Setting("save-banlist-on-ban") public boolean saveBanlistOnBan = false;
        @Setting("unrestrict-chat-message") public String unrestrictChatMessage = "I agree. Allow me to chat.";
        @Setting("gag-message-format") public String gagMessageFormat = "§cYou have been gagged. You cannot chat until you say\n"
                + "§c6the ungag key phrase.";
        @Setting("ungag-message-format") public String ungagMessageFormat = "§aYou have been ungagged.";
        
        public AsshatMitigationConfiguration(AsshatMitigationModule parent) {
            super(parent);
        }
    }

    /*
     * Asshat Mitigation - Ban
     * Written by: Razorcane
     * 
     * Handles the banning of both online and offline players.  However,
     * exact player names must be given when banning offline players.
     */
    @Command(aliases = {"ban", "vban", "vbano", "bano"},
    bounds = {1, -1},
    help = "To ban someone, simply type\n"
    + "§c/ban [player] (reason)",
    playerOnly = false)
    @CommandPermission(permission = "voxelguest.asshat.ban")
    public void ban(CommandSender cs, String[] args) {
        List<Player> l = Bukkit.matchPlayer(args[0]);
        String reason = "";

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                reason += args[i] + " ";
            }
        }

        if (l.size() > 1) {
            cs.sendMessage(ChatColor.RED + "Partial match.");
        } else if (l.isEmpty()) {
            String player = args[0];

            banned.put(player, reason);
            if (args.length > 1) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + player + ChatColor.DARK_GRAY + " has been banned by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + " for:");
                Bukkit.broadcastMessage(ChatColor.BLUE + reason);
            } else {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + player + ChatColor.DARK_GRAY + " has been banned by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + "for:");
                Bukkit.broadcastMessage(getConfiguration().getString("default-asshat-reason"));
            }
        } else {
            l.get(0).kickPlayer(reason);
            banned.put(l.get(0).getName(), reason);
            if (args.length > 1) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + l.get(0).getName() + ChatColor.DARK_GRAY + " has been banned by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + " for:");
                Bukkit.broadcastMessage(ChatColor.BLUE + reason);
            } else {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + l.get(0).getName() + ChatColor.DARK_GRAY + " has been banned by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + "for:");
                Bukkit.broadcastMessage(getConfiguration().getString("default-asshat-reason"));
            }
        }

        if (getConfiguration().getBoolean("save-banlist-on-ban")) {
            PropertyManager.save("banned", banned, "/asshatmitigation");
        }
    }

    /*
     * Asshat Mitigation - Unban
     * Written by: Razorcane
     * 
     * Controls the unbanning of banned players.  Name must be exact, and
     * player must be banned, in order to be unbanned.
     */
    @Command(aliases = {"unban", "vunban"},
    bounds = {1, -1},
    help = "To unban someone, simply type\n"
    + "§c/unban [player]",
    playerOnly = false)
    @CommandPermission(permission = "voxelguest.asshat.unban")
    public void unban(CommandSender cs, String[] args) {
        if (args.length < 1) {
            cs.sendMessage(ChatColor.RED + "Invalid arguments.");
        } else if (args.length > 1) {
            cs.sendMessage(ChatColor.RED + "Too many arguments.");
        } else {
            String player = args[0];
            if (banned.containsKey(player)) {
                banned.remove(player);
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + player + ChatColor.DARK_GRAY + "has been unbanned by " + ChatColor.RED + cs.getName());
            } else {
                cs.sendMessage(ChatColor.RED + "Player isn't banned.");
            }
        }
    }

    /*
     * Asshat Mitigation - Gag
     * Written by: Razorcane
     * 
     * Gags a player, or prevents them from talking until they are ungagged,
     * there is a server restart, or they type the designated phrase.
     */
    @Command(aliases = {"gag", "vgag"},
    bounds = {1, -1},
    help = "To gag someone, simply type\n"
    + "§c/gag [player] (reason)",
    playerOnly = false)
    @CommandPermission(permission = "voxelguest.asshat.gag")
    public void gag(CommandSender cs, String[] args) {
        List<Player> l = Bukkit.matchPlayer(args[0]);
        String reason = "";

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                reason += args[i] + " ";
            }
        }

        if (l.size() > 1) {
            cs.sendMessage(ChatColor.RED + "Partial match.");
        } else if (l.isEmpty()) {
            cs.sendMessage(ChatColor.RED + "No player to match.");
        } else {
            Player p = l.get(0);
            
            if (gagged.contains(p.getName())) {
                gagged.remove(p.getName());
                cs.sendMessage(ChatColor.RED + p.getName() + ChatColor.WHITE + " has been ungagged.");
            } else {
                gagged.add(p.getName());
                if (args.length > 1) {
                    Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + p.getName() + ChatColor.DARK_GRAY + " has been gagged by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + " for:");
                    Bukkit.broadcastMessage(ChatColor.BLUE + reason);
                } else {
                    Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + p.getName() + ChatColor.DARK_GRAY + " has been gagged by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + "for:");
                    Bukkit.broadcastMessage(getConfiguration().getString("default-asshat-reason"));
                }
            }
        }
    }

    /*
     * Asshat Mitigation - Kick
     * Written by: Razorcane
     * 
     * Kicks a player from the server.  Entering no reason defaults to the
     * default asshat reason, which is "Asshat".
     */
    @Command(aliases = {"kick", "vkick"},
    bounds = {1, -1},
    help = "To kick someone, simply type\n"
    + "§c/kick [player] (reason)",
    playerOnly = false)
    @CommandPermission(permission = "voxelguest.asshat.kick")
    public void kick(CommandSender cs, String[] args) {
        List<Player> l = Bukkit.matchPlayer(args[0]);
        String reason = "";

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                reason += args[i] + " ";
            }
        }

        if (l.size() > 1) {
            cs.sendMessage(ChatColor.RED + "Partial match.");
        } else if (l.isEmpty()) {
            cs.sendMessage(ChatColor.RED + "No player to match.");
        } else {
            l.get(0).kickPlayer(reason);
            if (args.length > 1) {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + l.get(0).getName() + ChatColor.DARK_GRAY + " has been kicked by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + " for:");
                Bukkit.broadcastMessage(ChatColor.BLUE + reason);
            } else {
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "Player " + ChatColor.RED + l.get(0).getName() + ChatColor.DARK_GRAY + " has been kicked by " + ChatColor.RED + cs.getName() + ChatColor.DARK_GRAY + "for:");
                Bukkit.broadcastMessage(getConfiguration().getString("default-asshat-reason"));
            }
        }
    }

    @ModuleEvent(event = PlayerPreLoginEvent.class)
    public void onPlayerPreLogin(BukkitEventWrapper wrapper) {
        PlayerPreLoginEvent event = (PlayerPreLoginEvent) wrapper.getEvent();
        String player = event.getName();

        if (banned.containsKey(player)) {
            event.setResult(PlayerPreLoginEvent.Result.KICK_FULL);
            event.disallow(PlayerPreLoginEvent.Result.KICK_FULL, banned.get(player).toString());
        }
    }

    @ModuleEvent(event = PlayerChatEvent.class)
    public void onPlayerChat(BukkitEventWrapper wrapper) {
        PlayerChatEvent event = (PlayerChatEvent) wrapper.getEvent();
        Player p = event.getPlayer();

        if (gagged.contains(p.getName())) {
            if (event.getMessage().equals(getConfiguration().getString("unrestrict-chat-message"))) {
                gagged.remove(p.getName());
                p.sendMessage(getConfiguration().getString("ungag-message-format"));
                event.setCancelled(true);
                wrapper.setCancelled(true);
            } else {
                p.sendMessage(getConfiguration().getString("gag-message-format"));
                event.setCancelled(true);
                wrapper.setCancelled(true);
            }
        }
    }
}

package com.untamedears.civchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;
import java.util.HashMap;

/*
 * Coded by Rourke750 & ibbignerd
 */
public class Commands implements CommandExecutor {

    private CivChat civ;
    private ChatManager chatManager;
    private HashMap<String, String> replyList = new HashMap<>();

    public Commands(ChatManager chatManagerInstance, CivChat instance) {
        chatManager = chatManagerInstance;
        civ = instance;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("tell")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You have to be a player to use that command!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length < 1) {
                if (chatManager.getChannel(player.getName()) == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /tell <player>");
                } else {
                    chatManager.removeChannel(player.getName());
                    player.sendMessage(ChatColor.RED + "You have moved to regular chat.");
                }
                return true;
            } else if (args.length == 1) {
                Player receiver = Bukkit.getPlayerExact(args[0]);
                if (receiver == null) {
                    player.sendMessage(ChatColor.RED + "Error: Player is offline.");
                    return true;
                } else {
                    if (chatManager.getGroupTalk(player.getName()) != null) {
                        sender.sendMessage(ChatColor.RED + "You were removed from Group Chat.");
                        chatManager.removeGroupTalk(player.getName());
                    }
                    chatManager.addChannel(player.getName(), receiver.getName());
                    player.sendMessage(ChatColor.RED + "You are now chatting with " + receiver.getName() + ".");
                    return true;
                }
            }

            if (args.length > 1) {
                Player receiver = Bukkit.getPlayerExact(args[0]);

                if (receiver == null) {
                    sender.sendMessage(ChatColor.RED + "Error: Player is offline.");
                    return true;
                } else {
                    StringBuilder message = new StringBuilder();

                    for (int i = 1; i < args.length; i++) {
                        message.append(args[i]);

                        if (i < args.length - 1) {
                            message.append(" ");
                        }
                    }
                    chatManager.sendPrivateMessage(player, receiver, message.toString());
                    replyList.put(player.getName(), receiver.getName());
                    return true;
                }
            }
            return true;
        }

        if (label.equalsIgnoreCase("reply")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You have to be a player to use that command!");
                return true;
            }
            
            String player = sender.getName();
            if (replyList.containsKey(player)) {
                if (replyList.get(player) == null) {
                    sender.sendMessage(ChatColor.RED + "Error: Player is offline.");
                    replyList.remove(player);
                    return true;
                } else {
                    if (args.length > 0) {
                        StringBuilder message = new StringBuilder();

                        for (int i = 1; i < args.length; i++) {
                            message.append(args[i]);

                            if (i < args.length - 1) {
                                message.append(" ");
                            }
                        }

                        String receiver = replyList.get(player);
                        Bukkit.getPlayer(player).sendMessage(ChatColor.DARK_PURPLE + "To " + receiver + ": " + message);
                        Bukkit.getPlayer(receiver).sendMessage(ChatColor.DARK_PURPLE + "From " + player + ": " + message);
                    } else {
                        Bukkit.getPlayer(player).sendMessage(ChatColor.DARK_PURPLE + "You will message " + ChatColor.YELLOW + replyList.get(player));
                    }
                }
            } else {
                Bukkit.getPlayer(player).sendMessage(ChatColor.RED + "There is no one to reply to");
            }
            return true;
        }

        if (label.equalsIgnoreCase("exit")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("You have to be a player to use that command!");
                return true;
            }

            Player player = (Player) sender;
            if (chatManager.getChannel(player.getName()) != null) {
                chatManager.removeChannel(player.getName());
                player.sendMessage(ChatColor.RED + "You have been moved to regular chat.");
                return true;
            }
            if (chatManager.getGroupTalk(player.getName()) != null) {
                chatManager.removeGroupTalk(player.getName());
                player.sendMessage(ChatColor.RED + "You have been moved to regular chat.");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You are not in a private chat or group chat");
                return true;
            }
        }

        if (label.equalsIgnoreCase("civchat")) {
            if (sender.hasPermission("civchat.admin")) {
                if (args.length < 1) {
                    sender.sendMessage("Usage: /civchat <save/reload>");
                    return true;
                }

                if (args[0].equalsIgnoreCase("save")) {
                    sender.sendMessage("Saved configuration.");
                    civ.saveConfig();

                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    sender.sendMessage("Reloaded configuration.");
                    civ.reloadConfig();

                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You Do not have Permissions civchat.admin");
            }
        }
        if (label.equalsIgnoreCase("groupchat")) {

            Player player = (Player) sender;
            StringBuilder message = new StringBuilder();
            Faction group = Citadel.getGroupManager().getGroup(args[0]);
            if (group == null) {
                sender.sendMessage(ChatColor.RED + "Not a valid group name");
                return true;
            }
            if (!Citadel.getGroupManager().getGroup(group.getName()).isMember(sender.getName())
                    && !Citadel.getGroupManager().getGroup(group.getName()).isModerator(sender.getName())
                    && !Citadel.getGroupManager().getGroup(group.getName()).isFounder(sender.getName())) {
                sender.sendMessage(ChatColor.RED + "You are not in that group.");
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "You have been moved to normal chat.");
                chatManager.removeGroupTalk(player.getName());
                return true;
            }
            if (args.length == 1) {
                if (chatManager.getGroupTalk(player.getName()) == null) {
                    if (chatManager.getChannel(player.getName()) != null) {
                        sender.sendMessage(ChatColor.RED + "You were removed from private chat.");
                        chatManager.removeChannel(player.getName());
                    }
                    sender.sendMessage(ChatColor.RED + "You have moved to group chat in the group: " + group + ".");
                    chatManager.addGroupTalk(sender.getName(), group);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "You have been switched to group: " + group);
                    chatManager.removeGroupTalk(player.getName());
                    chatManager.addGroupTalk(player.getName(), group);
                    return true;
                }
            }
            if (args.length > 1) {

                for (int i = 1; i < args.length; i++) {
                    message.append(args[i]);

                    if (i < args.length - 1) {
                        message.append(" ");
                    }
                }
                chatManager.GroupChat(group, message, sender.getName());
                return true;
            }
            return true;
        }
        /*
         * aliases
         */
        if (label.equalsIgnoreCase("chat")) {
            String chatPrefix = ChatColor.DARK_RED + "===" + ChatColor.YELLOW + "CivChat" + ChatColor.DARK_RED + "=========================\n";
            if (args.length == 0) {
                String help = " /chat range\n /chat groupchat\n /chat tell\n";
                if (chatManager.shout) {
                    help += " /chat shout\n";
                }
                if (chatManager.whisper) {
                    help += " /chat whisper\n";
                }
                if (chatManager.yvar) {
                    help += " /chat height\n";
                }
                help += " /chat alias";

                sender.sendMessage(chatPrefix
                        + " " + ChatColor.WHITE
                        + help);
            } else if (args.length > 0) {
                if (args[0].equalsIgnoreCase("shout") && chatManager.shout) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " By putting a \"" + chatManager.shoutChar + "\" in front of your message, chat range\n"
                            + "  is extended " + chatManager.shoutDist + "m\n"
                            + " There is a " + chatManager.shoutCool / 1000 + " second cooldown");
                } else if (args[0].equalsIgnoreCase("whisper") && chatManager.whisper) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " By putting a \"" + chatManager.whisperChar + "\" in front of your message, chat range\n"
                            + "  is reduced to " + chatManager.whisperDist + "m\n");
                } else if (args[0].equalsIgnoreCase("height") && chatManager.yvar) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " The higher you are the farther your messages can be heard");
                } else if (args[0].equalsIgnoreCase("range")) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " The default chat range is " + (int) chatManager.chatmax + "m\n"
                            + " This can be extended by using shout or climbing a mountain\n");
                } else if (args[0].equalsIgnoreCase("garble") && chatManager.garbleEnabled) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " Depending on the range, chat will be more garbled");
                } else if (args[0].equalsIgnoreCase("groupchat")){
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " /groupchat <citadel group> <message>\n" 
                            + " Send a message to everyone in the citadel group");
                } else if (args[0].equalsIgnoreCase("alias")) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " /groupchat [g, group]\n" 
                            + " /tell [message, msg, m, pm]\n" 
                            + " /chat [chathelp, ch]\n" 
                            + " /reply [r]\n");
                } else if (args[0].equalsIgnoreCase("tell")) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " /tell <player> <message>\n" 
                            + "   Send one message to the player\n"
                            + " /tell <player>\n"
                            + "   Create a channel with player. All regular chat will\n"
                            + "    go to player"
                            + " /exit\n"
                            + "   Stop the channel with player. All regular chat will\n"
                            + "    go to regular chat");
                } else if(args[0].equalsIgnoreCase("info")) {
                    sender.sendMessage(chatPrefix + ChatColor.WHITE
                            + " Version " + civ.pdf.getVersion() + " \n"
                            + " Coded by: " + civ.pdf.getAuthors());
                }
            }
        }
        return true;
    }
}

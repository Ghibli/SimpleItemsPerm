package it.alessiogta.simpleItemsPerms.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SipTabCompleter implements TabCompleter {
    
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "gui", "give", "remove", "check", "list", "info", "reload"
    );
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Suggerimenti per il primo argomento (subcomandi)
            completions = SUBCOMMANDS.stream()
                    .filter(sub -> sub.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Suggerimenti per il secondo argomento del comando give (nome giocatore)
            completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Suggerimenti per il terzo argomento del comando give (permesso senza prefisso)
            completions = Arrays.asList(
                    "vip",
                    "mvip",
                    "elite"
            );
            completions = completions.stream()
                    .filter(perm -> perm.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return completions;
    }
}

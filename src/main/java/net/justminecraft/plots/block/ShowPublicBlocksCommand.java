package net.justminecraft.plots.block;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class ShowPublicBlocksCommand extends SubCommand {
    public ShowPublicBlocksCommand() {
        super("/p showpublic", "Add a block you are looking at to blocks anyone can use", "showpublic", "spublic");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        Plot plot = JustPlots.getPlotAt((Player) sender);

        if (plot == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        if (!plot.isOwner((Player) sender) && !sender.hasPermission("justplots.public.other")) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " owns that plot");
            return false;
        }

        Set<Location> publicLocations = JustPlotsPublicBlock.getLocations(plot.toString());

        if(publicLocations == null || publicLocations.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no public locations on this plot");
            return false;
        }

        for (Location l : publicLocations) {
            new FlashBlock((Player) sender, l);
        }
        sender.sendMessage(ChatColor.GREEN + "Highlighting " + publicLocations.size() + " public blocks");
        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
    }
}

package net.justminecraft.plots.block;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PrivateBlockCommand extends SubCommand {
    public PrivateBlockCommand() {
        super("/p private", "Remove a block you are looking from use of everyone", "private");
    }

    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return false;
        }

        Plot plot = JustPlots.getPlotAt((Player) sender);

        Location toPrivate = ((Player) sender).getTargetBlock(null, 5).getLocation();

        Plot lookingAtPlot = JustPlots.getPlotAt(toPrivate);

        if (plot == null || lookingAtPlot == null) {
            sender.sendMessage(ChatColor.RED + "You are not standing on a plot");
            return false;
        }

        if(plot != lookingAtPlot) {
            sender.sendMessage(ChatColor.RED + "That location is not on your plot");
            return false;
        }

        if (!plot.isOwner((Player) sender) && !sender.hasPermission("justplots.public.other")) {
            sender.sendMessage(ChatColor.RED + JustPlots.getUsername(plot.getOwner()) + " owns that plot");
            return false;
        }


        if (!JustPlotsPublicBlock.isPublic(plot, toPrivate)) {
            sender.sendMessage(ChatColor.RED + JustPlotsPublicBlock.locationString(toPrivate) + " is not public on that plot");
            return false;
        }

        PlotLocationPrivateEvent event = new PlotLocationPrivateEvent(plot, ((Player) sender).getUniqueId());

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            throw new RuntimeException("Event was cancelled");
        }

        JustPlotsPublicBlock.removePublicLocation(plot, toPrivate);

        String whos = plot.isOwner((Player) sender) ? "your" : JustPlots.getUsername(plot.getOwner()) + "'s";

        sender.sendMessage(ChatColor.GREEN + "Succesfully made  " + JustPlotsPublicBlock.locationString(toPrivate) + " private on " + whos + " plot");

        return true;
    }

    @Override
    public void onTabComplete(CommandSender sender, String[] args, List<String> tabCompletion) {
    }
}

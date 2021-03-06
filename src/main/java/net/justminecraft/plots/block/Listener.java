package net.justminecraft.plots.block;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Listener implements org.bukkit.event.Listener {

    private void playerModify(Player player, Block block, Cancellable cancellable) {
        playerModify(player, block.getLocation(), cancellable);
    }

    private void playerModify(Player player, Entity entity, Cancellable cancellable) {
        playerModify(player, entity.getLocation(), cancellable);
    }

    private void playerModify(Player player, Location location, Cancellable cancellable) {
        if (!JustPlots.isPlotWorld(location.getWorld())) {
            return; // Not a plot world
        }

        Plot plot = JustPlots.getPlotAt(location);

        Material material = location.getBlock().getType();

        if (plot != null && (JustPlotsPublicBlock.isPublic(plot, location) || material.name().contains("PRESSURE_PLATE")) && !plot.isAdded(player)) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder("You used a public block").color(ChatColor.GREEN).create());
            cancellable.setCancelled(false);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getClickedBlock() != null) {
            playerModify(event.getPlayer(), event.getClickedBlock(), event);
        }

        if (event.getClickedBlock() == null) {
            Block block = event.getPlayer().getTargetBlock(null, 5);
            if(!block.isEmpty()) {
                playerModify(event.getPlayer(), block, event);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Location location = event.getBlock().getLocation();

        Player player = event.getPlayer();

        if (!JustPlots.isPlotWorld(location.getWorld())) {
            return; // Not a plot world
        }

        Plot plot = JustPlots.getPlotAt(location);

        if(plot == null || !plot.isAdded(player)) {
            return; // Not a plot, or not added
        }

        if(JustPlotsPublicBlock.isPublic(plot, location)) {
            JustPlotsPublicBlock.removePublicLocation(plot, location);
            player.sendMessage(ChatColor.RED + "Setting " + JustPlotsPublicBlock.locationString(location) + " to a private block because it was broken.");
        }
    }
}

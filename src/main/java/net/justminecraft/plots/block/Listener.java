package net.justminecraft.plots.block;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

        if ((plot == null || !plot.isAdded(player)) && (!player.hasPermission("justplots.edit.other") && !JustPlotsPublicBlock.isPublic(plot, location))) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder("You cannot build here").color(ChatColor.RED).create());

            cancellable.setCancelled(true);
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
}

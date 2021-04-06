package net.justminecraft.plots.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FlashBlock implements Runnable {
    private final Player player;
    private final Location location;
    private int flashCounter;

    public FlashBlock(Player player, Location location) {
        this.player = player;
        this.location = location;
        flashCounter = 10;
        run();
    }

    @Override
    public void run() {
        if(player == null) return;
        if(flashCounter == 0) {
            location.getBlock().getState().update(true, false);
            return;
        }
        flashCounter--;

        Material material = flashCounter % 2 == 0 ? Material.GREEN_STAINED_GLASS : Material.BLUE_STAINED_GLASS;

        player.sendBlockChange(location, material.createBlockData());

        JustPlotsPublicBlock.getPlugin().getServer().getScheduler().runTaskLater(JustPlotsPublicBlock.getPlugin(), this, 20);
    }
}

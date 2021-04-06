package net.justminecraft.plots.block;

import net.justminecraft.plots.JustPlots;
import net.justminecraft.plots.Plot;
import net.justminecraft.plots.PlotInfoEntry;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class JustPlotsPublicBlock extends JavaPlugin {

    private static HashMap<String, HashSet<Location>> availablePublicLocations = new HashMap<>();
    private static JustPlotsPublicBlock plugin;

    @NotNull
    public static HashSet<Location> getLocations(String plotId) {
        return availablePublicLocations.computeIfAbsent(plotId, key -> new HashSet<>());
    }

    public static boolean isPublic(Plot plot, Location location) {
        if (plot != null)
            return isPublic(plot.toString(), location);
        return false;
    }

    public static boolean isPublic(String plotId, Location location) {
        HashSet<Location> publicLocations = availablePublicLocations.get(plotId);
        return publicLocations != null && publicLocations.contains(location);
    }

    public static void addPublicLocation(Plot plot, Location location) {
        try(PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "INSERT OR IGNORE INTO justplots_public (world, x, z, locX, locY, locZ) VALUES (?, ?, ?, ?, ?, ?)"
        )) {
            statement.setString(1, plot.getWorldName());
            statement.setInt(2, plot.getId().getX());
            statement.setInt(3, plot.getId().getZ());
            statement.setInt(4, location.getBlockX());
            statement.setInt(5, location.getBlockY());
            statement.setInt(6, location.getBlockZ());

            statement.executeUpdate();

            getLocations(plot.toString()).add(location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removePublicLocation(Plot plot, Location location) {
        try(PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "DELETE FROM justplots_public WHERE world = ? AND x = ? AND z = ? AND locX = ? AND locY = ? AND locZ = ?"
        )) {
            statement.setString(1, plot.getWorldName());
            statement.setInt(2, plot.getId().getX());
            statement.setInt(3, plot.getId().getZ());
            statement.setInt(4, location.getBlockX());
            statement.setInt(5, location.getBlockY());
            statement.setInt(6, location.getBlockZ());

            statement.executeUpdate();

            getLocations(plot.toString()).remove(location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {

        plugin = this;

        getServer().getPluginManager().registerEvents(new net.justminecraft.plots.block.Listener(), this);

        getServer().getScheduler().runTaskAsynchronously(this, this::loadLocations);

        JustPlots.getCommandExecuter().addCommand(new PublicBlockCommand());
        JustPlots.getCommandExecuter().addCommand(new PrivateBlockCommand());
        JustPlots.getCommandExecuter().addCommand(new ShowPublicBlocksCommand());

        new PlotInfoEntry("Public blocks") {
            @Override
            public @Nullable BaseComponent[] getValue(@NotNull Plot plot) {
                ComponentBuilder builder = new ComponentBuilder();

                Set<Location> publicLocations = availablePublicLocations.get(plot.toString());

                if(publicLocations == null || publicLocations.isEmpty()) {
                    return builder.append("None").color(ChatColor.GRAY).create();
                }

                boolean first = true;
                for (Location loc : publicLocations) {
                    if (!first) {
                        builder.append(", ");
                    }
                    builder.append(locationString(loc));
                    first = false;
                }

                return builder.create();
            }
        };
    }
    
    private void loadLocations() {
        int counter = 0;
        
        createTable();

        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement("SELECT * FROM justplots_public")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("x");
                int z = results.getInt("z");
                int locX = results.getInt("locX");
                int locY = results.getInt("locY");
                int locZ = results.getInt("locZ");

                World w = Bukkit.getWorld(world);
                Location loc = new Location(w, locX, locY, locZ);

                try {
                    getLocations(world + ";" + x + ";" + z).add(loc);
                } catch (Exception e) {
                    getLogger().warning("Could not load public block locations for plot " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++counter % 10000 == 0) {
                    getLogger().info("Loading public block locations... (" + counter + ")");
                }
            }
        } catch (SQLException e) {
            getLogger().severe("FAILED TO LOAD PUBLIC BLOCK LOCATIONS");
            e.printStackTrace();
        }
    }

    private void createTable() {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "CREATE TABLE IF NOT EXISTS justplots_public ("
                        + "world VARCHAR(45) NOT NULL,"
                        + "x INT NOT NULL,"
                        + "z INT NOT NULL,"
                        + "locX INT NOT NULL,"
                        + "locY INT NOT NULL,"
                        + "locZ INT NOT NULL,"
                        + "UNIQUE (world, x, z, locX, locY, locZ))")) {
            statement.execute();
        } catch (SQLException e) {
            getLogger().severe("FAILED TO CREATE PUBLIC BLOCK DATABASE TABLE");
            e.printStackTrace();
        }
    }

    public static String locationString(Location location) {
        int x = (int) location.getX();
        int y = (int) location.getY();
        int z = (int) location.getZ();
        return x+";"+y+";"+z;
    }

    public static JustPlotsPublicBlock getPlugin() {
        return plugin;
    }
}

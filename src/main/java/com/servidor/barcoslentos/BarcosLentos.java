package com.servidor.barcoslentos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BarcosLentos extends JavaPlugin implements CommandExecutor {

    private double multiplicadorAgua;
    private double multiplicadorHielo;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cargarConfiguracion();

        if (getCommand("barcosreload") != null) {
            getCommand("barcosreload").setExecutor(this);
        }

        // Tarea que revisa los barcos activos cada 1 tick (20 veces por segundo)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World mundo : Bukkit.getWorlds()) {
                    for (Boat barco : mundo.getEntitiesByClass(Boat.class)) {
                        if (barco.getPassengers().isEmpty()) {
                            continue;
                        }

                        Entity conductor = barco.getPassengers().get(0);
                        if (!(conductor instanceof Player)) {
                            continue;
                        }

                        Vector velActual = barco.getVelocity();
                        double velocidadHorizontal = Math.hypot(velActual.getX(), velActual.getZ());

                        // Si el barco está prácticamente quieto, no intervenir
                        if (velocidadHorizontal < 0.01) {
                            continue;
                        }

                        Block bloqueDebajo = barco.getLocation().subtract(0, 0.2, 0).getBlock();
                        Material tipo = bloqueDebajo.getType();

                        double mult = multiplicadorAgua;
                        if (tipo == Material.ICE || tipo == Material.PACKED_ICE || tipo == Material.BLUE_ICE || tipo == Material.FROSTED_ICE) {
                            mult = multiplicadorHielo;
                        }

                        // Velocidad base estándar de un bote en Minecraft (~0.4 bloques/tick)
                        double velocidadObjetivo = 0.4 * mult;

                        // Solo limitar si supera la velocidad objetivo configurada
                        if (velocidadHorizontal > velocidadObjetivo) {
                            double factor = velocidadObjetivo / velocidadHorizontal;
                            barco.setVelocity(new Vector(
                                    velActual.getX() * factor,
                                    velActual.getY(),
                                    velActual.getZ() * factor
                            ));
                        }
                    }
                }
            }
        }.runTaskTimer(this, 1L, 1L);

        getLogger().info("BarcosLentos activado correctamente.");
    }

    public void cargarConfiguracion() {
        reloadConfig();
        this.multiplicadorAgua = getConfig().getDouble("multiplicador-agua", 0.5);
        this.multiplicadorHielo = getConfig().getDouble("multiplicador-hielo", 0.2);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("barcosreload")) {
            cargarConfiguracion();
            sender.sendMessage(ChatColor.GREEN + "[BarcosLentos] Configuración recargada (Agua: " + multiplicadorAgua + " | Hielo: " + multiplicadorHielo + ").");
            return true;
        }
        return false;
    }
}

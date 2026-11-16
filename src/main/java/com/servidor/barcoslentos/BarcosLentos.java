package com.servidor.barcoslentos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class BarcosLentos extends JavaPlugin implements Listener, CommandExecutor {

    private double multiplicadorAgua;
    private double multiplicadorHielo;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cargarConfiguracion();

        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("barcosreload") != null) {
            getCommand("barcosreload").setExecutor(this);
        }

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
            sender.sendMessage(ChatColor.GREEN + "[BarcosLentos] Configuración recargada correctamente.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void alMoverBarco(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat)) {
            return;
        }

        Boat barco = (Boat) event.getVehicle();

        // Solo aplicar si hay alguien conduciendo el barco
        if (barco.getPassengers().isEmpty()) {
            return;
        }

        Vector velocidad = barco.getVelocity();
        
        // Revisar qué bloque hay debajo del barco para aplicar el multiplicador correcto
        Block bloqueDebajo = barco.getLocation().subtract(0, 0.2, 0).getBlock();
        Material tipo = bloqueDebajo.getType();

        double multiplicador = this.multiplicadorAgua;
        if (tipo == Material.ICE || tipo == Material.PACKED_ICE || tipo == Material.BLUE_ICE || tipo == Material.FROSTED_ICE) {
            multiplicador = this.multiplicadorHielo;
        }

        // Aplicar reducción solo en los ejes horizontales (X y Z) para no afectar la gravedad (Y)
        double nuevaX = velocidad.getX() * multiplicador;
        double nuevaZ = velocidad.getZ() * multiplicador;

        barco.setVelocity(new Vector(nuevaX, velocidad.getY(), nuevaZ));
    }
}

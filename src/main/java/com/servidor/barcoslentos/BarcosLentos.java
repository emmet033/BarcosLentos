package com.servidor.barcoslentos;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
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
            sender.sendMessage(ChatColor.GREEN + "[BarcosLentos] Configuración recargada (Agua: " + multiplicadorAgua + " | Hielo: " + multiplicadorHielo + ").");
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

        // Solo aplicar si hay un jugador conduciendo
        if (barco.getPassengers().isEmpty() || !(barco.getPassengers().get(0) instanceof Player)) {
            return;
        }

        Location desde = event.getFrom();
        Location hacia = event.getTo();

        // Calcular el desplazamiento real que intentó hacer el jugador en este tick
        double deltaX = hacia.getX() - desde.getX();
        double deltaZ = hacia.getZ() - desde.getZ();

        // Si no hubo desplazamiento horizontal, salir
        if (Math.abs(deltaX) < 0.0001 && Math.abs(deltaZ) < 0.0001) {
            return;
        }

        Block bloqueDebajo = barco.getLocation().subtract(0, 0.2, 0).getBlock();
        Material tipo = bloqueDebajo.getType();

        double mult = this.multiplicadorAgua;
        if (tipo == Material.ICE || tipo == Material.PACKED_ICE || tipo == Material.BLUE_ICE || tipo == Material.FROSTED_ICE) {
            mult = this.multiplicadorHielo;
        }

        // Aplicar el multiplicador directamente al desplazamiento real y enviarlo al cliente
        Vector nuevaVelocidad = new Vector(
                deltaX * mult,
                barco.getVelocity().getY(),
                deltaZ * mult
        );

        barco.setVelocity(nuevaVelocidad);
    }
}

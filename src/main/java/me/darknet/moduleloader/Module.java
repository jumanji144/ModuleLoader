package me.darknet.moduleloader;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public abstract class Module implements Listener {

    public final String name;
    public final String version;
    public final String author;

    public List<Command> registeredCommands = new ArrayList<>();

    public Module(String name, String version, String author) {
        this.name = name;
        this.version = version;
        this.author = author;
    }

    public abstract void onEnable();
    public abstract void onDisable();
    public abstract void tick();

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    // Helper methods
    public void registerCommand(Command command) {
        registeredCommands.add(command);
        Bukkit.getLogger().info("Module " + name + " registered command " + command.getName());
        ModuleLoader.getInstance().registerCommand(command);
    }

}

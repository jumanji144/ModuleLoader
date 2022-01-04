package me.darknet.moduleloader;

import me.darknet.moduleloader.commands.ModuleCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public final class ModuleLoader extends JavaPlugin {

    public static Loader loader;
    public static ModuleLoader instance;

    public static ModuleLoader getInstance() {
        return instance;
    }

    public SimpleCommandMap commandMap;
    public Map<String, Command> knownCommands;

    @Override
    public void onEnable() {
        instance = this;
        Path path = Paths.get(getDataFolder().getAbsolutePath(), "modules");
        path.toFile().mkdirs();
        loader = new Loader(path);

        // register command
        getCommand("module").setExecutor(new ModuleCommand());

        // reflect command map
        try {
            // assuming we are using a CraftBukkit plugin
            // and assume that SimplePluginManager is the class that
            // contains the command map
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            Field f2 = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            f2.setAccessible(true);
            commandMap = (SimpleCommandMap) f.get(getServer().getPluginManager());
            knownCommands = (Map<String, Command>) f2.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void registerCommand(org.bukkit.command.Command command) {
        commandMap.register(getName(), command);
    }

    public void registerCommands(Command... commands) {
        Arrays.stream(commands).forEach(this::registerCommand);
    }

    public void unregisterCommand(org.bukkit.command.Command command) {
        command.unregister(commandMap);
        knownCommands.remove(command.getName());
        knownCommands.remove(command.getLabel());
        knownCommands.remove(getName() + ":" + command.getLabel());

        for (String alias : command.getAliases()) {
            knownCommands.remove(alias);
            knownCommands.remove(getName() + ":" + alias);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

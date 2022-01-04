package me.darknet.moduleloader.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import me.darknet.moduleloader.Module;
import me.darknet.moduleloader.ModuleLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ModuleCommand implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage("/" + label + " load <module>");
            sender.sendMessage("/" + label + " unload <module>");
            sender.sendMessage("/" + label + " reload <module>");
            sender.sendMessage("/" + label + " info <module>");
            sender.sendMessage("/" + label + " list");
            sender.sendMessage("/" + label + " modules");
        }

        if(args.length >= 1) {

            switch (args[0]) {

                case "load": {

                    switch (args.length) {
                        case 1: {
                            sender.sendMessage("/" + label + " load <module>");
                            break;

                        }
                        case 2: {
                            String module = args[1];

                            Module loadedModule = ModuleLoader.loader.findModule(module);
                            if(loadedModule != null) {

                                ModuleLoader.loader.load(loadedModule);
                                sender.sendMessage("Loaded module: " + module);

                            }else {
                                sender.sendMessage("Module not found: " + module);
                                return true;
                            }

                            return true;

                        }
                    }

                    break;

                }

                case "unload": {

                    switch (args.length) {

                        case 1: {
                            sender.sendMessage("/" + label + " unload <module>");
                            return true;
                        }

                        case 2: {
                            String module = args[1];

                            ModuleLoader.loader.unload(module);

                            sender.sendMessage("Unloaded module: " + module);
                            return true;
                        }

                    }

                    return true;


                }

                case "reload": {

                    ModuleLoader.loader.reload();
                    return true;
                }

                case "list": {

                    List<Module> modules = ModuleLoader.loader.getModules();
                    if(modules.isEmpty()) {
                        sender.sendMessage("No modules loaded");
                        return true;
                    }

                    sender.sendMessage("Loaded Modules:");
                    for(Module module : ModuleLoader.loader.getModules()) {
                        sender.sendMessage(" - " + module.getName());
                    }
                    return true;

                }

                case "modules": {

                    List<String> files = ModuleLoader.loader.getModuleFiles();
                    if(files.isEmpty()) {
                        sender.sendMessage("No modules to load");
                        return true;
                    }

                    sender.sendMessage("Modules to load:");
                    for(String file : files) {
                        sender.sendMessage(" - " + file);
                    }
                    return true;


                }

                case "info": {

                    switch (args.length) {
                        case 1: {
                            sender.sendMessage("/" + label + " info <module>");
                            return true;
                        }
                        case 2: {
                            String module = args[1];
                            Module loadedModule = ModuleLoader.loader.getModule(module);
                            if(loadedModule != null) {

                                sender.sendMessage("Module: " + loadedModule.getName());
                                sender.sendMessage("Version: " + loadedModule.getVersion());
                                sender.sendMessage("Author: " + loadedModule.getAuthor());

                            }
                            return true;
                        }
                    }

                }

            }

        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1) {
            return Arrays.asList("load", "unload", "reload", "info", "list", "modules");
        }
        if(args.length == 2) {

            if(args[0].equalsIgnoreCase("load")) {
                return ModuleLoader.loader.getModuleFiles();
            }

            // get all the module names from the ModuleLoader.loader.loadedModules
            List<String> names = Lists.newArrayList();
            Set<String> loadedModules = ModuleLoader.loader.loadedModules.keySet();
            names.addAll(loadedModules);

            return names;

        }
        return Collections.emptyList();
    }
}

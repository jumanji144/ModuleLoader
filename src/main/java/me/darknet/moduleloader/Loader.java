package me.darknet.moduleloader;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Loader {

    public Map<String, Module> loadedModules = new HashMap<>();
    final Path moduleFolder;
    ModuleClassLoader classLoader;


    public Loader(Path moduleFolder) {
        this.moduleFolder = moduleFolder;
        classLoader = new ModuleClassLoader();
        try {
            classLoader.addURL(moduleFolder.toUri().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getModuleFiles() {

        try {
            return Files.walk(moduleFolder)
                    .map(Path::toString)
                    .filter(s -> s.endsWith(".class") && !s.contains("$"))
                    .map(s -> s.replace(".class", "")
                            .replace(moduleFolder.toString(), "")
                            .replace("/", ""))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();

    }

    public List<Module> getModules() {

        return new ArrayList<>(loadedModules.values());

    }

    public void load(Module module) {

        module.onEnable();
        loadedModules.put(module.getName(), module);
        Bukkit.getPluginManager().registerEvents(module, ModuleLoader.getInstance());

    }

    public Module getModule(String name) {

        return loadedModules.get(name);

    }

    public Module findModule(String name) {

        File moduleFile = new File(moduleFolder.toFile(), name + ".class");
        if(moduleFile.exists()) {
            // load class file and create instance
                try {
                    Class<?> moduleClass = classLoader.loadClass(name);
                    return (Module) moduleClass.newInstance();


                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
        }

        return null;

    }

    public void unload(String name) {

        Module module = loadedModules.get(name);
        if(module != null) {
            module.onDisable();
            loadedModules.remove(name);
            HandlerList.unregisterAll(module);
            for (Command registeredCommand : module.registeredCommands) {
                ModuleLoader.getInstance().unregisterCommand(registeredCommand);
            }
        }

    }

    public void reload() {

        // remember loaded modules
        List<String> loadedModuleNames = new ArrayList<>();
        for (Module module : loadedModules.values()) {
            loadedModuleNames.add(module.getClass().getSimpleName());
        }

        // disable all modules
        for(Module module : loadedModules.values()) {
            module.onDisable();
            HandlerList.unregisterAll(module);
            for (Command registeredCommand : module.registeredCommands) {
                ModuleLoader.getInstance().unregisterCommand(registeredCommand);
            }
        }

        // clear loaded modules
        loadedModules.clear();

        try {
            classLoader.close(); // close old classloader
            classLoader = new ModuleClassLoader(); // create new classloader
            classLoader.addURL(moduleFolder.toUri().toURL());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load previous modules
        for(String moduleName : loadedModuleNames) {

            Module module = findModule(moduleName);
            if(module != null) {
                load(module);
            }else {
                Bukkit.getLogger().warning("Could not reload module: " + moduleName);
            }

        }




    }

    // Custom class loader to prevent conflicts with other modules
    public static class ModuleClassLoader extends URLClassLoader {

        public ModuleClassLoader() {
            super(new URL[0], ModuleClassLoader.class.getClassLoader());
        }

        public void addURL(URL url) {
            super.addURL(url);
        }

        public boolean hasURL(URL url) {
            return super.findLoadedClass(url.getFile()) != null;
        }

        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return super.loadClass(name);
        }

        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }

        public Class<?> defineClass(String name, byte[] b) {

           return defineClass(name, b, 0, b.length);

        }
    }

}

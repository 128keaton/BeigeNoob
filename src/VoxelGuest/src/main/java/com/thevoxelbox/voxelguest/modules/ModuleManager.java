/*
 * Copyright (C) 2011 - 2012, psanker and contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this list of 
 *   conditions and the following 
 * * Redistributions in binary form must reproduce the above copyright notice, this list of 
 *   conditions and the following disclaimer in the documentation and/or other materials 
 *   provided with the distribution.
 * * Neither the name of The VoxelPlugineering Team nor the names of its contributors may be 
 *   used to endorse or promote products derived from this software without specific prior 
 *   written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.thevoxelbox.voxelguest.modules;

import com.thevoxelbox.commands.CommandsManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public class ModuleManager {
    private static Plugin plugin;
    private final CommandsManager commandsManager;
    private static ModuleManager instance;
        
    protected List<Module> activeModules = new LinkedList<Module>();
    protected HashMap<Class<? extends Module>, Module> classInstanceMap = new HashMap<Class<? extends Module>, Module>();
    
    public ModuleManager(Plugin p, CommandsManager manager) {
        plugin = p;
        commandsManager = manager;
    }
    
    public static void setActiveModuleManager(ModuleManager manager) {
        instance = manager;
    }
    
    public static ModuleManager getManager() {
        return instance;
    }
    
    public void loadModules(Class<? extends Module>[] classes) {
        for (Class<? extends Module> cls : classes) {
            try {
                loadModule(cls);
            } catch (ModuleException ex) {
                if (ex instanceof ModuleInitialisationException || ex instanceof MalformattedModuleException) {
                    log(ex.getMessage(), 2);
                    ex.printStackTrace();
                }
                
                continue;
            }
        }
    }
    
    public synchronized void loadModule(Class<? extends Module> cls) throws ModuleException {
        Module module = null;
        
        if (!cls.isAnnotationPresent(MetaData.class))
            throw new MalformattedModuleException("Malformatted Module: " + cls.getCanonicalName());
        
        try {
            module = (Module) cls.newInstance();
        } catch (IllegalAccessException ex) {
            throw new ModuleInitialisationException("Failed to load Module instance: " + cls.getCanonicalName());
        } catch (IllegalArgumentException ex) {
            throw new ModuleInitialisationException("Failed to load Module instance: " + cls.getCanonicalName());
        } catch (SecurityException ex) {
            throw new ModuleInitialisationException("Failed to load Module instance: " + cls.getCanonicalName());
        } catch (InstantiationException ex) {
            throw new ModuleInitialisationException("Failed to load Module instance: " + cls.getCanonicalName());
        }
        
        if (activeModules.contains(module))
            throw new ModuleException("Module already registered: " + cls.getCanonicalName());
        
        if (module != null) {
            // Find and register commands and events
            commandsManager.registerCommands(cls);
            
            module.enable();
            activeModules.add(module);
            classInstanceMap.put(cls, module);
            log(getName(module), module.getLoadMessage(), 0);
        } else {
            throw new ModuleException("Module is null or already registered"); // Only in weird cases would this happen
        }
    }
    
    public void shutDownModules() {
        Iterator<Module> it = activeModules.listIterator();
        
        while (it.hasNext()) {
            Module module = it.next();
            module.disable();
        }
        
        activeModules.clear();
    }
    
    public Module getModule(Class<? extends Module> cls) throws ModuleException {
        if (classInstanceMap.containsKey(cls))
            return classInstanceMap.get(cls);
        
        throw new ModuleException("Module not found: " + cls.getCanonicalName());
    }
    
    public Module[] getModules() {
        Module[] modules = new Module[activeModules.size()];
        return activeModules.toArray(modules);
    }
    
    public String getName(Module mod) {
        if (!activeModules.contains(mod))
            return null;
        
        MetaData md = mod.getClass().getAnnotation(MetaData.class);
        return md.name();
    }
    
    public String getDescription(Module mod) {
        if (!activeModules.contains(mod))
            return null;
        
        MetaData md = mod.getClass().getAnnotation(MetaData.class);
        return md.description();
    }
    
    private static void log(String str, int importance) {
        switch (importance) {
            case 0:
                Logger.getLogger("Mincraft").info("[" + plugin.getDescription().getName() + "] " + str);
                return;
            case 1:
                Logger.getLogger("Mincraft").warning("[" + plugin.getDescription().getName() + "] " + str);
                return;
            case 2:
                Logger.getLogger("Mincraft").severe("[" + plugin.getDescription().getName() + "] " + str);
                return;
            default:
                Logger.getLogger("Mincraft").info("[" + plugin.getDescription().getName() + "] " + str);
                return;
        }
    }
    
    private static void log(String module, String str, int importance) {
        switch (importance) {
            case 0:
                Logger.getLogger("Mincraft").info("[" + plugin.getDescription().getName() + ":" + module + "] " + str);
                return;
            case 1:
                Logger.getLogger("Mincraft").warning("[" + plugin.getDescription().getName() + ":" + module + "] " + str);
                return;
            case 2:
                Logger.getLogger("Mincraft").severe("[" + plugin.getDescription().getName() + ":" + module + "] " + str);
                return;
            default:
                Logger.getLogger("Mincraft").info("[" + plugin.getDescription().getName() + ":" + module + "] " + str);
                return;
        }
    }
}

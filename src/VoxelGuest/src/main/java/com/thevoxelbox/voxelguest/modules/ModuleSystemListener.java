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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

public class ModuleSystemListener implements Listener {
    private HashMap<Class<? extends Event>, HashMap<Method, ModuleEvent>> moduleEventMap = new HashMap<Class<? extends Event>, HashMap<Method, ModuleEvent>>();
    private HashMap<Method, Module> instances = new HashMap<Method, Module>();
    
    public void registerModuleEvents() {
        for (Module module : ModuleManager.getManager().getModules()) {
            registerModuleEvents(module);
        }
    }
    
    public void registerModuleEvents(Module module) {
        for (Method method : module.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ModuleEvent.class)) {
                Class classType = method.getParameterTypes()[0];
                if (!BukkitEventWrapper.class.isAssignableFrom(classType) || method.getParameterTypes().length != 1)
                    continue;
                
                if (!void.class.isAssignableFrom(method.getReturnType()))
                    continue;
                
                ModuleEvent moduleEvent = method.getAnnotation(ModuleEvent.class);

                if (!moduleEventMap.containsKey(moduleEvent.event())) {
                    HashMap<Method, ModuleEvent> map = new HashMap<Method, ModuleEvent>();
                    map.put(method, moduleEvent);
                    moduleEventMap.put(moduleEvent.event(), map);
                } else {
                    HashMap<Method, ModuleEvent> map = moduleEventMap.get(moduleEvent.event());
                    map.put(method, moduleEvent);
                    moduleEventMap.put(moduleEvent.event(), map);
                }

                instances.put(method, module);
            }
        }
    }
    
    public void processModuleEvents(Event event) {
        HashMap<Method, ModuleEvent> map = moduleEventMap.get(event.getClass());
        BukkitEventWrapper wrapper = new BukkitEventWrapper(event);
        
        if (map == null)
            return;
        
        for (int i = 4; i >= 0; i--) {
            for (Map.Entry<Method, ModuleEvent> entry : map.entrySet()) {
                if (entry.getValue().priority().getIntValue() < i)
                    continue;
                
                Method method = entry.getKey();
                boolean ignoreCancelled = entry.getValue().ignoreCancelledEvents();
                
                if (!instances.get(method).isEnabled())
                    continue;
                
                if (wrapper.isCancelled() && ignoreCancelled)
                    continue;
                
                try {
                    method.invoke(instances.get(method), wrapper);
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

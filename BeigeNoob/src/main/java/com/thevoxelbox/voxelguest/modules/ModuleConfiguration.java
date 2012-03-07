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

import com.thevoxelbox.voxelguest.util.PropertyManager;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleConfiguration {
    protected HashMap<String, Object> settings = new HashMap<String, Object>();
    private final Module parentModule;
    
    public ModuleConfiguration(Module parent) {
        parentModule = parent;
    }
    
    public Object getEntry(String key) {
        if (settings == null)
            return null;
        else if (!settings.containsKey(key))
            return null;
        else
            return settings.get(key);
    }
    
    public Object[] getArray(String key) {
        if (settings == null)
            return null;
        else if (!settings.containsKey(key))
            return null;
        else if (!(settings.get(key) instanceof Object[]))
            return null;
        else
            return (Object[]) settings.get(key);
    }
    
    public String getString(String key) {
        if (settings == null)
            return null;
        else if (!settings.containsKey(key))
            return null;
        else if (!(settings.get(key) instanceof String))
            return null;
        else
            return settings.get(key).toString();
    }
    
    public List<String> getStringList(String key) {
        if (settings == null)
            return null;
        else if (!settings.containsKey(key))
            return null;
        else if (!(settings.get(key) instanceof String[]))
            return null;
        else
            return Arrays.asList((String[]) settings.get(key));
    }
    
    public boolean getBoolean(String key) {
        if (settings == null)
            return false;
        else if (!settings.containsKey(key))
            return false;
        else if (!(settings.get(key) instanceof Boolean))
            return false;
        else
            return ((Boolean) settings.get(key)).booleanValue();
    }
    
    public int getInt(String key) {
        if (settings == null)
            return -1;
        else if (!settings.containsKey(key))
            return -1;
        else if (!(settings.get(key) instanceof Integer))
            return -1;
        else
            return ((Integer) settings.get(key)).intValue();
    }
    
    public void setEntry(String key, Object value) {
        if (settings == null)
            return;
        
        settings.put(key, value);
    }
    
    public void setArray(String key, Object[] value) {
        if (settings == null)
            return;
        
        settings.put(key, value);
    }
    
    public void setString(String key, String value) {
        if (settings == null)
            return;
        
        settings.put(key, value);
    }
    
    public void setStringList(String key, List<String> value) {
        if (settings == null)
            return;
        
        Object[] objs = value.toArray();
        String[] strs = new String[objs.length];
        
        for (int i = 0; i < strs.length; i++) {
            strs[i] = objs[i].toString();
        }
        
        settings.put(key, strs);
    }
    
    public void setBoolean(String key, boolean value) {
        if (settings == null)
            return;
        
        settings.put(key, Boolean.valueOf(value));
    }
    
    public void setInt(String key, int value) {
        if (settings == null)
            return;
        
        settings.put(key, Integer.valueOf(value));
    }
    
    public void load() {
        registerFieldSettings(getClass());
        loadWrittenSettings();
    }
    
    public void save() {
        PropertyManager.save(parentModule.getName(), settings, "/modules");
    }
    
    private void loadWrittenSettings() {
        HashMap<String, Object> map = (HashMap<String, Object>) PropertyManager.load(parentModule.getName(), "/modules");
        
        if (map == null || map.isEmpty())
            return;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            setEntry(entry.getKey(), entry.getValue());
        }
    }
    
    private void registerFieldSettings(Class<? extends ModuleConfiguration> cls) {
        for (Field field : cls.getDeclaredFields()) {
            try {
                registerFieldSetting(field);
            } catch (IllegalArgumentException ex) {
                continue;
            } catch (IllegalAccessException ex) {
                continue;
            }
        }
    }
    
    private void registerFieldSetting(Field field) throws IllegalArgumentException, 
        IllegalAccessException {

        if (!field.isAnnotationPresent(Setting.class))
            return;

        if (!field.isAccessible())
            field.setAccessible(true);

        Setting setting = field.getAnnotation(Setting.class);
        String key = setting.value();
        Object value = field.get(parentModule);
        
        setEntry(key, value);
    }
}

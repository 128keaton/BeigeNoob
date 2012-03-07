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

package com.thevoxelbox.voxelguest.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class FlatFileManager {
    
    private static String directory = "plugins/VoxelGuest/data";
    
    public static String[] load(String target) {
        return load(target, "", false);
    }
    
    public static String[] load(String target, String destination) {
        return load(target, destination, false);
    }
    
    public static String[] load(String target, String destination, boolean autoDestroy) {
        // Note: Destination is appended to plugins/VoxelGuest/data
        // For example, when destination is "/channels",
        // the target directory will be "plugins/VoxelGuest/data/channels/"
        
        File f = new File(directory + destination + "/" + target + ".txt");
        Scanner snr = null;
        
        if (f.exists()) {
            try {
                snr = new Scanner(f);
                String toCut = "";
                
                while (snr.hasNextLine()) {
                    String line = snr.nextLine();
                    
                    if (line.startsWith("#"))
                        continue;
                    
                    toCut = toCut + line + "\n";
                }
                
                snr.close();
                String[] split = toCut.split("\n");
                
                if (autoDestroy)
                    f.delete();
                
                return split;
            } catch (FileNotFoundException ex) {
                return null;
            }
        } else {
            if (!autoDestroy) {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    return null;
                }
            }
        }
        
        return null;
    }
    
    public static void save(String[] strs, String target) {
        save(strs, target, "");
    }
    
    public static void save(String[] strs, String target, String destination) {
        // Note: Destination is appended to plugins/VoxelGuest/data
        // For example, when destination is "/channels",
        // the target directory will be "plugins/VoxelGuest/data/channels/"
        
        File f = new File(directory + destination + "/" + target + ".txt");
        PrintWriter pw = null;
        
        if (f.exists()) {
            try {
                pw = new PrintWriter(f);
                
                String concat = "";
                
                for (int i = 0; i < strs.length; i++) {
                    if (i == (strs.length - 1)) {
                        concat = concat + strs[i];
                    } else {
                        concat = concat + strs[i] + "\n";
                    }
                }
                
                pw.write(concat);
                pw.close();
            } catch (FileNotFoundException ex) {
                return;
            }
        }
    }
}

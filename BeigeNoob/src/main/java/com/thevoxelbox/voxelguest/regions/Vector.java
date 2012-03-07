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

package com.thevoxelbox.voxelguest.regions;

public class Vector {
    private final double x, y, z;
    
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector(int x, int y, int z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }
    
    public Vector(float x, float y, float z) {
        this.x = (double) x;
        this.y = (double) y;
        this.z = (double) z;
    }
    
    public Vector(Vector vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public Vector setX(double x) {
        return new Vector(x, this.y, this.z);
    }
    
    public Vector setX(int x) {
        return new Vector((double) x, this.y, this.z);
    }
    
    public Vector setY(double y) {
        return new Vector(this.x, y, this.z);
    }
    
    public Vector setY(int y) {
        return new Vector(this.x, (double) y, this.z);
    }
    
    public Vector setZ(double z) {
        return new Vector(this.x, this.y, z);
    }
    
    public Vector setZ(int z) {
        return new Vector(this.x, this.y, (double) z);
    }
    
    public Vector add(Vector vec) {
        return new Vector(this.x + vec.x, this.y + vec.y, this.z + vec.z);
    }
    
    public Vector subtract(Vector vec) {
        return new Vector(this.x - vec.x, this.y - vec.y, this.z - vec.z);
    }
    
    public double getDistanceSquared(Vector vec) {
        return (vec.x - this.x)*(vec.x - this.x) + (vec.y - this.y)*(vec.y - this.y) + (vec.z - this.z)*(vec.z - this.z);
    }
    
    public double getDistance(Vector vec) {
        return Math.sqrt(getDistanceSquared(vec));
    }
    
    @Override
    public int hashCode() {
        int hash = 9;
        
        hash = 81 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 81 * hash + (int) (Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 81 * hash + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final Vector other = (Vector) obj;
        
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        
        return true;
    }
}

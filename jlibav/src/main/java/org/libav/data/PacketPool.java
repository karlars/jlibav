/*
 * Copyright (C) 2012 Ondrej Perutka
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package org.libav.data;

import java.util.ArrayDeque;
import java.util.Deque;
import org.bridj.Pointer;
import org.libav.avcodec.IPacketWrapper;
import org.libav.avcodec.PacketWrapperFactory;

/**
 * Packet pool.
 * 
 * @author Ondrej Perutka
 */
public class PacketPool {
    
    private PacketWrapperFactory packetFactory;
    private Deque<PooledPacket> recycle;
    
    public PacketPool() {
        recycle = new ArrayDeque<PooledPacket>();
        packetFactory = PacketWrapperFactory.getInstance();
    }
    
    public IPacketWrapper getEmptyPacket() {
        IPacketWrapper result;
        
        synchronized (this) {
            result = recycle.poll();
        }
        
        if (result == null)
            return new PooledPacket(packetFactory.alloc());
        
        result.free();
        result.init();
        
        return result;
    }
    
    public IPacketWrapper clonePacket(IPacketWrapper packet) {
        PooledPacket result = getPacket();
        int growBy = packet.getSize() - result.getSize();
        if (growBy > 0)
            result.grow(growBy);
        
        Pointer<Byte> pData = result.getData();
        packet.getPointer().copyTo(result.getPointer());
        result.setData(pData);
        
        pData = packet.getData();
        if (pData != null)
            pData.copyTo(result.getData(), packet.getSize());
        
        result.clearWrapperCache();
        
        return result;
    }
    
    private PooledPacket getPacket() {
        PooledPacket result;
        
        synchronized (this) {
            result = recycle.poll();
        }
        
        if (result == null)
            return new PooledPacket(packetFactory.alloc());
        
        return result;
    }
    
    private synchronized void recycle(PooledPacket packet) {
        recycle.add(packet);
    }
    
    private class PooledPacket implements IPacketWrapper {
        private IPacketWrapper internal;
        private int bufferSize;

        public PooledPacket(IPacketWrapper internal) {
            this.internal = internal;
            this.bufferSize = internal.getSize();
        }

        @Override
        public void init() {
            internal.init();
        }

        @Override
        public void free() {
            setSize(bufferSize);
            recycle(this);
        }

        @Override
        public void grow(int growBy) {
            internal.grow(growBy);
            bufferSize = internal.getSize();
        }

        @Override
        public void shrink(int size) {
            internal.shrink(size);
        }

        @Override
        public int getStreamIndex() {
            return internal.getStreamIndex();
        }

        @Override
        public void setStreamIndex(int streamIndex) {
            internal.setStreamIndex(streamIndex);
        }

        @Override
        public int getSize() {
            return internal.getSize();
        }

        @Override
        public void setSize(int size) {
            internal.setSize(size);
        }

        @Override
        public Pointer<Byte> getData() {
            return internal.getData();
        }

        @Override
        public void setData(Pointer<Byte> data) {
            internal.setData(data);
        }

        @Override
        public int getFlags() {
            return internal.getFlags();
        }

        @Override
        public void setFlags(int flags) {
            internal.setFlags(flags);
        }

        @Override
        public long getPts() {
            return internal.getPts();
        }

        @Override
        public void setPts(long pts) {
            internal.setPts(pts);
        }

        @Override
        public long getDts() {
            return internal.getDts();
        }

        @Override
        public void setDts(long dts) {
            internal.setDts(dts);
        }

        @Override
        public int getDuration() {
            return internal.getDuration();
        }

        @Override
        public void setDuration(int duration) {
            internal.setDuration(duration);
        }

        @Override
        public long getConvergenceDuration() {
            return internal.getConvergenceDuration();
        }

        @Override
        public void setConvergenceDuration(long convergenceDuration) {
            internal.setConvergenceDuration(convergenceDuration);
        }

        @Override
        public long getPosition() {
            return internal.getPosition();
        }

        @Override
        public void setPosition(long position) {
            internal.setPosition(position);
        }

        @Override
        public IPacketWrapper clone() {
            return clonePacket(internal);
        }

        @Override
        public void clearWrapperCache() {
            internal.clearWrapperCache();
        }

        @Override
        public Pointer<?> getPointer() {
            return internal.getPointer();
        }
    }
    
}

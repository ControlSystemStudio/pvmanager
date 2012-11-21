/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 * @author carcassi
 */
public class QueueCollector<T> implements Collector<T, List<T>> {
    
    private final Object lock = new Object();
    private List<T> readBuffer;
    private List<T> writeBuffer;
    private final int maxElements;

    public QueueCollector(int maxElements) {
        this.maxElements = maxElements;
        synchronized(lock) {
            readBuffer = new ArrayList<>();
            writeBuffer = new ArrayList<>();
        }
    }

    @Override
    public void setValue(T newValue) {
        synchronized(lock) {
            writeBuffer.add(newValue);
            if (writeBuffer.size() > maxElements) {
                writeBuffer.remove(0);
            }
        }
    }

    @Override
    public List<T> getValue() {
        synchronized(lock) {
            List<T> data = writeBuffer;
            writeBuffer = readBuffer;
            writeBuffer.clear();
            readBuffer = data;
        }
        return readBuffer;
    }
    
}

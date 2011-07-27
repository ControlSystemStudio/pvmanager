/*
 * Copyright 2011 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.tests;

import org.epics.pvmanager.PV;
import org.epics.pvmanager.PVWriter;
import org.epics.pvmanager.PVWriterListener;
import java.util.List;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.CompositeDataSource;
import org.epics.pvmanager.sim.SimulationDataSource;
import gov.aps.jca.Context;
import gov.aps.jca.Monitor;
import org.epics.pvmanager.jca.JCADataSource;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import static org.epics.pvmanager.util.Executors.*;
import static org.epics.pvmanager.ExpressionLanguage.*;
import static org.epics.pvmanager.util.TimeDuration.*;

/**
 * This is the code from the examples in the docs, to make sure it
 * actually compiles
 *
 * @author carcassi
 */
public class Examples {

    public void c2() {
        // Route notification for this pv on the Swing EDT
        PVReader<?> pvReader = PVManager.read(channel("test")).notifyOn(swingEDT()).every(ms(100));

        // Or you can change the default
        PVManager.setDefaultNotificationExecutor(swingEDT());
    }

    public void c3() {
        // Sets CAJ (pure java implementation) as the default data source,
        // monitoring both value and alarm changes
        PVManager.setDefaultDataSource(new JCADataSource());

        // For utltimate control, you can create the JCA context yourself
        // and pass it to the data source
        Context jcaContext = null;
        PVManager.setDefaultDataSource(new JCADataSource(jcaContext, Monitor.VALUE | Monitor.ALARM));
    }

    public void c4() {
        // Create a multiple data source, and add different data sources
        CompositeDataSource composite = new CompositeDataSource();
        composite.putDataSource("ca", new JCADataSource());
        composite.putDataSource("sim", new SimulationDataSource());

        // If no prefix is given to a channel, use JCA as default
        composite.setDefaultDataSource("ca");

        // Set the composite as the default
        PVManager.setDefaultDataSource(composite);
    }

    public void b1() {
        // Let's statically import so the code looks cleaner

        // Read channel "channelName" up to every 100 ms
        final PVReader<Object> pvReader = PVManager.read(channel("channelName")).every(ms(100));
        pvReader.addPVReaderListener(new PVReaderListener() {

            public void pvChanged() {
                // Do something with each value
                Object newValue = pvReader.getValue();
                System.out.println(newValue);
            }
        });

        // Remember to close
        pvReader.close();
    }

    public void b1a() {
        // Read channel "channelName" up to every 100 ms, and get all
        // the new values from the last notification.
        final PVReader<List<Object>> pvReader = PVManager.read(newValuesOf(channel("channelName"))).every(ms(100));
        pvReader.addPVReaderListener(new PVReaderListener() {

            public void pvChanged() {
                // Do something with each value
                for (Object newValue : pvReader.getValue()) {
                    System.out.println(newValue);
                }
            }
        });

        // Remember to close
        pvReader.close();
    }

    public void b2() {
        PVWriter<Object> pvWriter = PVManager.write(channel("channelName")).async();
        pvWriter.addPVWriterListener(new PVWriterListener() {

            public void pvWritten() {
                System.out.println("Write finished");
            }
        });
        // This will return right away, and the notification will be sent
        // on the listener
        pvWriter.write("New value");

        // Remember to close
        pvWriter.close();
    }

    public void b3() {
        PVWriter<Object> pvWriter = PVManager.write(channel("channelName")).sync();
        // This will block until the write is done
        pvWriter.write("New value");
        System.out.println("Write finished");

        // Remember to close
        pvWriter.close();
    }

    public void b4() {
        // A PV is both a PVReader and a PVWriter
        final PV<Object, Object> pv = PVManager.readAndWrite(channel("channelName")).asynchWriteAndReadEvery(ms(10));
        pv.addPVReaderListener(new PVReaderListener() {

            public void pvChanged() {
                // Do something with each value
                Object newValue = pv.getValue();
                System.out.println(newValue);
            }
        });
        pv.write("New value");

        // Remember to close
        pv.close();
    }
}
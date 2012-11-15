/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.pvmanager.expression.DesiredRateExpression;
import org.epics.util.time.TimeDuration;

/**
 * Object responsible to notify the PVReader of changes on the appropriate thread.
 *
 * @author carcassi
 */
public class PVReaderDirector<T> {
    
    private static final Logger log = Logger.getLogger(PVReaderDirector.class.getName());

    private final WeakReference<PVReaderImpl<T>> pvRef;
    private final Function<T> function;
    private final DataSource dataSource;
    private final NewConnectionCollector connCollector =
            new NewConnectionCollector();
    private final NewQueueCollector<Exception> exceptionCollector =
            new NewQueueCollector<>(1);
    private final Map<DesiredRateExpression<?>, DataRecipe> recipes =
            new HashMap<>();
    
    private final Executor notificationExecutor;
    private final ScheduledExecutorService scannerExecutor;
    
    private volatile ScheduledFuture<?> scanTaskHandle;
    
    /**
     * Calculate the recipes and connects the channel to the datasource.
     * 
     * @param expression 
     */
    public void connectExpression(DesiredRateExpression<?> expression) {
        DataRecipeBuilder builder = new DataRecipeBuilder();
        expression.fillDataRecipe(this, builder);
        DataRecipe recipe = builder.build(exceptionCollector, connCollector);
        synchronized(this) {
            recipes.put(expression, recipe);
        }
        if (!recipe.getChannelRecipes().isEmpty()) {
            try {
                dataSource.connect(recipe);
            } catch(Exception ex) {
                recipe.getChannelRecipes().iterator().next().getReadSubscription().getExceptionWriteFunction().setValue(ex);
            }
        }
    }
    
    public void disconnectExpression(DesiredRateExpression<?> expression) {
        DataRecipe recipe;
        synchronized(this) {
            recipe = recipes.remove(expression);
        }
        if (recipe == null) {
            log.log(Level.SEVERE, "Director was asked to disconnect expression '" + expression + "' which was not found.");
        }
        
        if (!recipe.getChannelRecipes().isEmpty()) {
            try {
                dataSource.disconnect(recipe);
            } catch(Exception ex) {
                recipe.getChannelRecipes().iterator().next().getReadSubscription().getExceptionWriteFunction().setValue(ex);
            }
        }
    }
    
    public void close() {
        while (!recipes.isEmpty()) {
            DesiredRateExpression<?> expression = recipes.keySet().iterator().next();
            disconnectExpression(expression);
        }
    }

    /**
     * Creates a new notifier. The new notifier will notifier the given pv
     * with new values calculated by the function, and will use onThread to
     * perform the notifications.
     * <p>
     * After construction, one MUST set the pvRecipe, so that the
     * dataSource is appropriately closed.
     *
     * @param pv the pv on which to notify
     * @param function the function used to calculate new values
     * @param notificationExecutor the thread switching mechanism
     */
    PVReaderDirector(PVReaderImpl<T> pv, Function<T> function, ScheduledExecutorService scannerExecutor,
            Executor notificationExecutor, DataSource dataSource) {
        this.pvRef = new WeakReference<>(pv);
        this.function = function;
        this.notificationExecutor = notificationExecutor;
        this.scannerExecutor = scannerExecutor;
        this.dataSource = dataSource;
    }

    /**
     * Determines whether the notifier is active or not.
     * <p>
     * The notifier becomes inactive if the PVReader is closed or is garbage collected.
     * The first time this function determines that the notifier is inactive,
     * it will ask the data source to close all channels relative to the
     * pv.
     *
     * @return true if new notification should be performed
     */
    boolean isActive() {
        // Making sure to get the reference once for thread safety
        final PVReader<T> pv = pvRef.get();
        if (pv != null && !pv.isClosed()) {
            return true;
        } else {
            close();
            return false;
        }
    }
    
    /**
     * Checks whether the pv is paused
     * 
     * @return true if paused
     */
    boolean isPaused() {
        final PVReader<T> pv = pvRef.get();
        if (pv == null || pv.isPaused()) {
            return true;
        } else {
            return false;
        }
    }
    
    private volatile boolean notificationInFlight = false;
    
    /**
     * Notifies the PVReader of a new value.
     */
    void notifyPv() {
        // Don't even calculate if notification is in flight.
        // This makes pvManager automatically throttle back if the consumer
        // is slower than the producer.
        if (notificationInFlight)
            return;
        
        // Calculate new value
        T newValue = null;
        boolean calculationSucceeded = false;
        try {
            // Tries to calculate the value
            newValue = function.getValue();
            calculationSucceeded = true;
        } catch(RuntimeException ex) {
            // Calculation failed
            exceptionCollector.setValue(ex);
        }
        
        // Calculate new connection
        final boolean connected = connCollector.getValue();
        List<Exception> exceptions = exceptionCollector.getValue();
        final Exception lastException;
        if (exceptions.isEmpty()) {
            lastException = null;
        } else {
            lastException = exceptions.get(exceptions.size() - 1);
        }
        
        // TODO: if payload is immutable, the difference test should be done here
        // and not in the runnable (to save SWT time)
        
        // Prepare values to ship to the other thread.
        // The data will be shipped as part of the task,
        // which is properly synchronized by the executor
        final T finalValue = newValue;
        final boolean finalCalculationSucceeded = calculationSucceeded;
        notificationInFlight = true;
        notificationExecutor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    PVReaderImpl<T> pv = pvRef.get();
                    // Proceed with notification only if PVReader was not garbage
                    // collected
                    if (pv != null) {
                        pv.setConnected(connected);
                        if (lastException != null) {
                            pv.setLastException(lastException);
                        }
                        
                        // XXX Are we sure that we should skip notifications if values are null?
                        if (finalCalculationSucceeded && finalValue != null) {
                            Notification<T> notification =
                                    NotificationSupport.notification(pv.getValue(), finalValue);
                            // Remember to notify anyway if an exception need to be notified
                            if (notification.isNotificationNeeded() || pv.isLastExceptionToNotify() || pv.isReadConnectionToNotify()) {
                                pv.setValue(notification.getNewValue());
                            }
                        } else {
                            // Remember to notify anyway if an exception need to be notified
                            if (pv.isLastExceptionToNotify() || pv.isReadConnectionToNotify()) {
                                pv.firePvValueChanged();
                            }
                        }
                    }
                } finally {
                    notificationInFlight = false;
                }
            }
        });
    }
    
    void startScan(TimeDuration duration) {
        scanTaskHandle = scannerExecutor.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                if (isActive()) {
                    // If paused, simply skip without stopping the scan
                    if (!isPaused()) {
                        notifyPv();
                    }
                } else {
                    stopScan();
                }
            }
        }, 0, duration.toNanosLong(), TimeUnit.NANOSECONDS);
    }
    
    void timeout(TimeDuration timeout, final String timeoutMessage) {
        scannerExecutor.schedule(new Runnable() {

            @Override
            public void run() {
                PVReaderImpl<T> pv = pvRef.get();
                if (pv != null && pv.getValue() == null) {
                    exceptionCollector.setValue(new TimeoutException(timeoutMessage));
                }
            }
        }, timeout.toNanosLong(), TimeUnit.NANOSECONDS);
    }
    
    void stopScan() {
        if (scanTaskHandle != null) {
            scanTaskHandle.cancel(false);
            scanTaskHandle = null;
        } else {
            throw new IllegalStateException("Scan was never started");
        }
    }

}
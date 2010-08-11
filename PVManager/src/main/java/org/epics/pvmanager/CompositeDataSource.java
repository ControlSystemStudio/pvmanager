/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package org.epics.pvmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A data source that can dispatch a request to multiple different
 * data sources.
 *
 * @author carcassi
 */
public class CompositeDataSource extends DataSource {

    // Stores all data sources by name
    private Map<String, DataSource> dataSources = new ConcurrentHashMap<String, DataSource>();

    private volatile String delimiter = "://";
    private volatile String defaultDataSource;

    /**
     * Returns the delimeter that divides the data source name from the
     * channel name. Default is "://" so that "epics://pv1" corresponds
     * to the "pv1" channel from the "epics" datasource.
     *
     * @return data source delimeter; can't be null
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Changes the data source delimiter.
     *
     * @param delimiter new data source delimiter; can't be null
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Adds/replaces the data source corresponding to the given name.
     *
     * @param name the name of the data source
     * @param dataSource the data source to add/replace
     */
    public void putDataSource(String name, DataSource dataSource) {
        dataSources.put(name, dataSource);
    }

    /**
     * Returns which data source is used if no data source is specified in the
     * channel name.
     *
     * @return the default data source, or null if it was never set
     */
    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    /**
     * Sets the data source to be used if the channel does not specify
     * one explicitely. The data source must have already been added.
     *
     * @param defaultDataSource the default data source
     */
    public void setDefaultDataSource(String defaultDataSource) {
        if (!dataSources.containsKey(defaultDataSource))
            throw new IllegalArgumentException("The data source " + defaultDataSource + " was not previously added, and therefore cannot be set as default");

        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public void monitor(MonitorRecipe connRecipe) {
        Map<String, Map<String, ValueCache>> routingCaches = new HashMap<String, Map<String, ValueCache>>();

        // Iterate through the recipe to understand how to distribute
        // the calls
        for (Map.Entry<String, ValueCache> entry : connRecipe.caches.entrySet()) {
            String name = entry.getKey();
            String dataSource = defaultDataSource;

            int indexDelimiter = name.indexOf(delimiter);
            if (indexDelimiter != -1) {
                dataSource = name.substring(0, indexDelimiter);
                name = name.substring(indexDelimiter + delimiter.length());
            }

            if (dataSource == null)
                throw new IllegalArgumentException("Channel " + name + " uses the default data source but one was never set.");

            // Add recipe for the target dataSource
            if (routingCaches.get(dataSource) == null)
                routingCaches.put(dataSource, new HashMap<String, ValueCache>());
            routingCaches.get(dataSource).put(name, entry.getValue());
        }

        // Dispatch calls to all the data sources
        for (Map.Entry<String, Map<String, ValueCache>> entry : routingCaches.entrySet()) {
            MonitorRecipe recipe = new MonitorRecipe();
            recipe.caches = entry.getValue();
            recipe.collector = connRecipe.collector;
            dataSources.get(entry.getKey()).monitor(recipe);
        }
    }

}

/*
 * Copyright 2008-2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager.extra;

import java.util.List;
import org.epics.pvmanager.DesiredRateExpression;
import org.epics.pvmanager.data.VDoubleArray;
import org.epics.pvmanager.data.VImage;

/**
 *
 * @author carcassi
 */
public class WaterfallPlot extends DesiredRateExpression<VImage> {

    WaterfallPlot(DesiredRateExpression<List<VDoubleArray>> queue, String name) {
        super(queue, new WaterfallPlotFunction(queue.getFunction(), WaterfallPlotParameters.defaults().internalCopy()), name);
    }
    
    private volatile WaterfallPlotParameters parameters = WaterfallPlotParameters.defaults();

    WaterfallPlotFunction getPlotter() {
        return (WaterfallPlotFunction) getFunction();
    }
    
    public WaterfallPlot with(WaterfallPlotParameters... newParameters) {
        parameters = new WaterfallPlotParameters(parameters, newParameters);
        WaterfallPlotParameters.InternalCopy copy = parameters.internalCopy();
        getPlotter().setParameters(copy);
        return this;
    }

    public int getMaxHeight() {
        return getPlotter().getParameters().height;
    }

    public boolean isAdaptiveRange() {
        return getPlotter().getParameters().adaptiveRange;
    }
    
    public boolean scrollDown() {
        return getPlotter().getParameters().scrollDown;
    }

    public ColorScheme getColorScheme() {
        return getPlotter().getParameters().colorScheme;
    }
}
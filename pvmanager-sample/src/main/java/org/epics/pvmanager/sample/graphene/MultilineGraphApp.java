/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.pvmanager.sample.graphene;

import org.epics.graphene.InterpolationScheme;
import org.epics.graphene.LineGraph2DRendererUpdate;
import org.epics.graphene.LineGraph2DRendererUpdate;
import org.epics.pvmanager.graphene.ScatterGraph2DExpression;
import static org.epics.pvmanager.formula.ExpressionLanguage.formula;
import static org.epics.pvmanager.graphene.ExpressionLanguage.*;
import org.epics.pvmanager.graphene.LineGraph2DExpression;
import org.epics.pvmanager.graphene.MultilineGraph2DExpression;

/**
 *
 * @author carcassi
 */
public class MultilineGraphApp extends BaseGraphApp<LineGraph2DRendererUpdate> {
    private InterpolationScheme interpolationScheme = InterpolationScheme.NEAREST_NEIGHBOR;

    public MultilineGraphApp() {
        dataFormulaField.setModel(new javax.swing.DefaultComboBoxModel<String>(
                new String[] { "=tableOf(column(\"Sine\", 'sim://sineWaveform(1,50,100,0.01)'), column(\"Triangle\", 'sim://triangleWaveform(2,50,100,0.01)'), column(\"Triangle\", 'sim://squareWaveform(3,50,100,0.01)'))",
                    "sim://gaussianWaveform",
                    "sim://sineWaveform",
                    "sim://triangleWaveform",
                    "=tableOf(column(\"X\", range(-5, 5)), column(\"Y\", 'sim://gaussianWaveform'))"}));
    }
    
    public InterpolationScheme getInterpolationScheme() {
        return interpolationScheme;
    }

    public void setInterpolationScheme(InterpolationScheme interpolationScheme) {
        this.interpolationScheme = interpolationScheme;
        if (graph != null) {
            graph.update(graph.newUpdate().interpolation(interpolationScheme));
        }
    }

    @Override
    protected MultilineGraph2DExpression createExpression(String dataFormula) {
        MultilineGraph2DExpression plot = multilineGraphOf(formula(dataFormula),
                    null,
                    null);
        plot.update(plot.newUpdate().interpolation(interpolationScheme));
        return plot;
    }

    @Override
    protected void openConfigurationDialog() {
        MultilineGraphDialog dialog = new MultilineGraphDialog(new javax.swing.JFrame(), true, this);
        dialog.setTitle("Configure...");
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    public static void main(String[] args) {
        main(MultilineGraphApp.class);
    }
    
}

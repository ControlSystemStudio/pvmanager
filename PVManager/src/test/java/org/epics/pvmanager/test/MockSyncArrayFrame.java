/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */

package org.epics.pvmanager.test;

import java.awt.Color;
import java.awt.BasicStroke;
import org.epics.pvmanager.ThreadSwitch;
import org.epics.pvmanager.sim.SimulationDataSource;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVValueChangeListener;
import java.util.Collections;
import org.epics.pvmanager.data.VDouble;
import org.epics.pvmanager.data.VMultiDouble;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import static org.epics.pvmanager.util.TimeDuration.*;
import static org.epics.pvmanager.data.ExpressionLanguage.*;

/**
 *
 * @author carcassi
 */
public class MockSyncArrayFrame extends javax.swing.JFrame {
    ChartPanel panel;

    /** Creates new form MockPVFrame */
    public MockSyncArrayFrame() {
        PVManager.setDefaultThread(ThreadSwitch.onSwingEDT());
        PVManager.setDefaultDataSource(SimulationDataSource.simulatedData());
        initComponents();
        panel = new ChartPanel(null, true, true, true, false, true);
        plotPanel.add(panel);
        plotPanel.revalidate();
    }

    private void updateChart() {
        if (pv.getValue() == null)
            return;
        JFreeChart chart = createChart();
        panel.setChart(chart);
    }

    private JFreeChart createChart() {

        final XYDataset data1 = createDataset();
        final XYItemRenderer renderer1 = new StandardXYItemRenderer();
        final NumberAxis rangeAxis1 = new NumberAxis();
        rangeAxis1.setRange(-1.5, 1.5);
        final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
        subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        subplot1.setDomainGridlineStroke(new BasicStroke());
        subplot1.setDomainGridlinePaint(new Color(240, 240, 240));
        subplot1.setRangeGridlineStroke(new BasicStroke());
        subplot1.setRangeGridlinePaint(new Color(240, 240, 240));

        NumberAxis hor = new NumberAxis();
        hor.setRange(0, pv.getValue().getValues().size() - 1);
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(hor);
        plot.setGap(10.0);

        plot.add(subplot1, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);

        // return a new chart containing the overlaid plot...
        return new JFreeChart(null,
                              JFreeChart.DEFAULT_TITLE_FONT, plot, true);
    }

    private XYDataset createDataset() {
        final XYSeries series1 = new XYSeries("Values at " + pv.getValue().getTimeStamp().asDate());
        int index = 0;
        if (pv.getValue() != null) {
            for (VDouble value : pv.getValue().getValues()) {
                if (value != null)
                    series1.add(index, value.getValue());
                index++;
            }
        }

        final XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series1);
        return collection;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        scanRateSpinner = new javax.swing.JSpinner();
        createPVButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        nPVSpinner = new javax.swing.JSpinner();
        plotPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        updateRateSpinner = new javax.swing.JSpinner();
        jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel6.setText("UI scan rate (Hz):");

        scanRateSpinner.setModel(new javax.swing.SpinnerNumberModel(25, 1, 50, 1));

        createPVButton.setText("Create ");
        createPVButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createPVButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("N PVs:");

        nPVSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(2), Integer.valueOf(1), null, Integer.valueOf(1)));

        plotPanel.setLayout(new java.awt.BorderLayout());

        jLabel3.setText("PV update rate (Hz):");

        updateRateSpinner.setModel(new javax.swing.SpinnerNumberModel(50, 1, 1000, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(plotPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                    .addComponent(createPVButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scanRateSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nPVSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 697, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateRateSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(plotPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(updateRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scanRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nPVSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createPVButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    PVReader<VMultiDouble> pv;

    private void createPVButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createPVButtonActionPerformed
        if (pv != null)
            pv.close();

        int nPvs = ((Integer) nPVSpinner.getModel().getValue()).intValue();
        double timeIntervalSec = (1.0 / ((Integer) updateRateSpinner.getModel().getValue()).intValue());
        String pvName = "ramp(-1.5, 1.5, 0.1, " + timeIntervalSec + ")";
        int scanRate = ((Integer) scanRateSpinner.getModel().getValue()).intValue();

        // Buffer depth has to be longest between the time between scan and
        // the time between sample multiplied by 5 (so you get at least 5 samples).
        double bufferDepth = Math.max(timeIntervalSec * 5.0, (1.0 / scanRate));

        pv = PVManager.read(synchronizedArrayOf(ms(75), ms((int) (bufferDepth * 1000.0)),
                vDoubles(Collections.nCopies(nPvs, pvName)))).atHz(scanRate);
        pv.addPVValueChangeListener(new PVValueChangeListener() {
            @Override
            public void pvValueChanged() {
                //printArray(pv.getValue());
                updateChart();
            }
        });
    }//GEN-LAST:event_createPVButtonActionPerformed


    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MockSyncArrayFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createPVButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSpinner nPVSpinner;
    private javax.swing.JPanel plotPanel;
    private javax.swing.JSpinner scanRateSpinner;
    private javax.swing.JSpinner updateRateSpinner;
    // End of variables declaration//GEN-END:variables

}

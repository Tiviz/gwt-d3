/**
 * Copyright (c) 2013, Anthony Schiochet and Eric Citaire
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * The names Anthony Schiochet and Eric Citaire may not be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL MICHAEL BOSTOCK BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.gwtd3.ui.chart;

import java.util.Arrays;

import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.scales.LinearScale;
import com.github.gwtd3.api.scales.Scale;
import com.github.gwtd3.api.svg.Axis.Orientation;
import com.github.gwtd3.ui.data.DefaultSelectionUpdater;
import com.github.gwtd3.ui.data.SelectionDataJoiner;
import com.github.gwtd3.ui.event.RangeChangeEvent;
import com.github.gwtd3.ui.event.RangeChangeEvent.RangeChangeHandler;
import com.github.gwtd3.ui.model.AxisModel;
import com.github.gwtd3.ui.model.BaseChartModel;
import com.github.gwtd3.ui.svg.GContainer;
import com.github.gwtd3.ui.svg.SVGDocumentContainer;
import com.github.gwtd3.ui.svg.SVGResources;
import com.github.gwtd3.ui.svg.SVGStyles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Random;

/**
 * A base class for a chart with one horizontal axis and one vertical axis,
 * and a data region.
 * <p>
 * 
 * @author SCHIOCA
 */
public class BaseChart extends SVGDocumentContainer implements ChartContext {

    protected static final int DEFAULT_TOP_POSITION = 20;
    protected static final int DEFAULT_BOTTOM_POSITION = 30;
    protected static final int DEFAULT_LEFT_POSITION = 35;
    protected static final int DEFAULT_RIGHT_POSITION = 15;

    protected final AxisModel<LinearScale> xModel;

    protected final AxisModel<LinearScale> yModel;

    // ==== children =========
    protected GContainer g;

    private ChartAxis<? extends Scale<?>> xAxis;

    private ChartAxis<? extends Scale<?>> yAxis;

    private final Styles styles;

    /**
     * Support for x or y sliding
     */
    private final DragSupport dragSupport = new DragSupport(this.xModel);
    /**
     *  
     */
    private final Options options = new Options(this);

    private final ClipPath dataRegionClipPath;

    /**
     * Configuration options the chart.
     * 
     * @author SCHIOCA
     * 
     */
    public class Options {
        private final BaseChart chart;

        public Options(final BaseChart chart) {
            super();
            this.chart = chart;
        }

        public Options enableXNavigation(final boolean enable) {
            if (enable) {
                chart.dragSupport.enable();
            }
            else {
                chart.dragSupport.disable();
            }
            return this;
        }

    }

    public interface Resources extends SVGResources {
        @Source("BaseChart.css")
        BaseChart.Styles chartStyles();
    }

    public interface Styles extends SVGStyles {
        /**
         * @return the classname applied to any axis
         */
        String axis();

        /**
         * class applied to all labels
         * 
         * @return
         */
        String label();

        /**
         * class applied to all element on the y axis
         * 
         * @return
         */
        String y();

        /**
         * class applied to all element on the x axis
         * 
         * @return
         */
        String x();
    }

    public BaseChart(final BaseChartModel<LinearScale> model) {
        this(model, (Resources) GWT.create(Resources.class));
    }

    public BaseChart(final BaseChartModel<LinearScale> model, final Resources resources) {
        super(resources);

        xModel = model.xModel();
        yModel = model.yModel();

        // getElement().setAttribute("viewBox", "0 0 500 400");
        styles = resources.chartStyles();
        styles.ensureInjected();

        dataRegionClipPath = new ClipPath("clip" + Random.nextInt(100000));

        createChildren();
    }

    @Override
    protected void onSelectionAttached() {
        super.onSelectionAttached();

        initModel();

        // register x drag interaction
        dragSupport.registerListeners(select()).enable();
    }

    protected void initModel() {
        RangeChangeHandler handler = new RangeChangeHandler() {
            @Override
            public void onRangeChange(final RangeChangeEvent event) {
                redrawSeries();
            }
        };
        xModel.addRangeChangeHandler(handler);
        yModel.addRangeChangeHandler(handler);
    }

    /**
     * Create the g container, and the axis components.
     */
    protected void createChildren() {
        // create G container
        g = new GContainer();
        add(g);
        g.transform().translate(DEFAULT_LEFT_POSITION, DEFAULT_TOP_POSITION);

        // X AXIS
        xAxis = new ChartAxis<LinearScale>(xModel, Orientation.BOTTOM);
        // FIXME
        // xAxis.setPixelSize(0, chartWidth());
        xAxis.addStyleName(styles.x());
        // FIXME let the user configure position at center ?
        // FIXME: or automate the process by a Y domain neg and pos
        // should be yRange.apply(0) instead of chart height ?
        g.add(xAxis);

        // Y AXIS
        // FIXME
        // yAxis.scale().range(chartHeight(), 0);
        // tickSize(6, 4, 2).
        yAxis = new ChartAxis<LinearScale>(yModel, Orientation.LEFT);

        yAxis.generator().ticks(4);// .tickSubdivide(1).tickSize(12, 6, 3);
        // append the axis to the svg
        // change styling, position, (left, right)
        // text label position / orientation
        yAxis.addStyleName(styles.y());
        g.add(yAxis);

        // SERIES RENDERER
    }

    // ============= drawing =============
    @Override
    public void redraw() {
        redrawClippath();
        redrawAxis();
        redrawSeries();
    }

    private void redrawClippath() {
        SelectionDataJoiner.update(g.select(), Arrays.asList(dataRegionClipPath),
                new DefaultSelectionUpdater<ClipPath>("#" + dataRegionClipPath.getId()) {
                    @Override
                    public String getElementName() {
                        return "clipPath";
                    }

                    @Override
                    public void afterEnter(final Selection selection) {
                        super.afterEnter(selection);
                        selection.attr("id", dataRegionClipPath.getId())
                                .append("rect");
                    }

                    @Override
                    public void onJoinEnd(final Selection selection) {
                        super.onJoinEnd(selection);
                        // set the width of the clippath to the width of the chart
                        selection.select("rect")
                                .attr("width", chartWidth())
                                .attr("height", chartHeight());

                    }

                    @Override
                    public String getKey(final ClipPath datum, final int index) {
                        return datum.getId();
                    }

                });

    }

    protected void redrawSeries() {

    }

    private void redrawAxis() {
        // TODO let the user customize the X axis position
        xAxis.transform().removeAll().translate(0, chartHeight());
        xAxis.setLength(chartWidth());
        yAxis.setLength(chartHeight());

    }

    // ============= getters =============
    /**
     * Width of the area displaying series data, bounded by axis. (excluding
     * space for Y axis labels or legend)
     * 
     * @return
     */
    public int chartWidth() {
        return getWidth() - DEFAULT_LEFT_POSITION - DEFAULT_RIGHT_POSITION;
    }

    /**
     * Width of the area displaying series data, bounded by axis.
     * 
     * @return
     */
    public int chartHeight() {
        // we add 1 pixel so we can see the line when its value is zero.
        return (getHeight() - DEFAULT_TOP_POSITION - DEFAULT_BOTTOM_POSITION) + 1;
    }

    public ChartAxis<? extends Scale<?>> xAxis() {
        return xAxis;
    }

    public ChartAxis<?> yAxis() {
        return yAxis;
    }

    public Options options() {
        return options;
    }

    @Override
    public ClipPath getSerieClipPath() {
        return dataRegionClipPath;
    }

    /**
     * Set a translation by x and y to the main container. <br>
     * The main container contains the x and y axis,
     * the data region and the series.
     * 
     * @param x position on the x axis
     * @param y position on the y axis
     */
    public void translateMainContainer(final int x, final int y) {
        g.transform().removeAll().translate(x, y);
    }

}

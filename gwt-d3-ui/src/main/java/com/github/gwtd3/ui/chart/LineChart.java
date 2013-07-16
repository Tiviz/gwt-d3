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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.scales.LinearScale;
import com.github.gwtd3.ui.chart.renderer.LineRenderer;
import com.github.gwtd3.ui.chart.renderer.Renderer;
import com.github.gwtd3.ui.data.DefaultSelectionUpdater;
import com.github.gwtd3.ui.data.SelectionDataJoiner;
import com.github.gwtd3.ui.event.SerieAddedEvent;
import com.github.gwtd3.ui.event.SerieAddedEvent.SerieAddedHandler;
import com.github.gwtd3.ui.event.SerieChangeEvent;
import com.github.gwtd3.ui.event.SerieChangeEvent.SerieChangeHandler;
import com.github.gwtd3.ui.event.SerieRemovedEvent;
import com.github.gwtd3.ui.event.SerieRemovedEvent.SerieRemovedHandler;
import com.github.gwtd3.ui.model.AxisCoordsBuilder;
import com.github.gwtd3.ui.model.LineChartModel;
import com.github.gwtd3.ui.model.NamedRange;
import com.github.gwtd3.ui.model.PointBuilder;
import com.github.gwtd3.ui.model.Serie;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.CssResource.ImportedWithPrefix;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * A line chart displaying several series on the same amount.
 * <p>
 * You can customize the styling by providing your own {@link Styles} instance during construction of the chart.
 * <p>
 * You can configure the chart behaviour using {@link #options()}.
 * <p>
 * User is able by default to navigate accross the X dimension domain. Call {@link Options#enableXNavigation(boolean)}
 * with false to disable it.
 * <p>
 * FIXME: styling lines (colors, etc...) FIXME: styling serie label (position, font, etc...)
 * 
 * FIXME: customize scaling functions (linear, log, etc...)
 * 
 * FIXME: configuring ticks
 * 
 * FIXME: slide in X => events to grab new data
 * 
 * FIXME: caching hidden data if slide is possible by presending events
 * 
 * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
 * 
 * @param <T>
 */
public class LineChart<T> extends BaseChart<T> implements SerieAddedHandler<T>, SerieRemovedHandler<T>,
        SerieChangeHandler<T> {

    // ========== Resources and Styles classes =====================

    private static Resources createDefaultResources() {
        return GWT.create(Resources.class);
    }

    public static interface Resources extends BaseChart.Resources {
        @Override
        @Source("LineChart.css")
        LineChart.Styles chartStyles();
    }

    // public static interface XAxisResources extends ChartAxis.Resources {
    // @Source("ChartAxis.css")
    // ChartAxis.Styles xStyles();
    // }
    //
    // public static interface YAxisResources extends ChartAxis.Resources {
    // @Source("ChartAxis.css")
    // ChartAxis.Styles yStyles();
    // }

    @ImportedWithPrefix("d3-line-chart")
    public static interface Styles extends BaseChart.Styles {

        /**
         * will be applied to any line serie.
         * 
         * @return
         */
        public String line();

        /**
         * a classna;e applied to series lines.
         * 
         * @return
         */
        public String serie();

        /**
         * class applied to range of values defined in any {@link NamedRange}.
         * 
         * @return the named class
         */
        public String namedRange();

    }

    /**
     * The model defining this chart
     */
    private LineChartModel<T, LinearScale> model;

    private LineChart.Styles styles;

    private PointBuilder<T> pointBuilder;

    public LineChart(final LineChartModel<T, LinearScale> model) {
        this(model, createDefaultResources());
    }

    public LineChart(final LineChartModel<T, LinearScale> model, final Resources resources) {
        super(model, resources);
        // getElement().setAttribute("viewBox", "0 0 500 400");
        styles = resources.chartStyles();
        styles.ensureInjected();

        this.model = model;
        // new LineChartModel<T, LinearScale>(xModel, yModel, domainBuilder);
    }

    // ============== initialization ========================
    @Override
    protected void initModel() {
        super.initModel();

        this.model.addSerieAddedHandler(this);
        this.model.addSerieRemovedHandler(this);

        // listens for range changed

        pointBuilder = new AxisCoordsBuilder<T>(xModel, yModel, model.coordsBuilder());
    }

    // ==================== redraw methods ================

    private final Map<Serie<T>, Renderer<T>> renderers = new HashMap<Serie<T>, Renderer<T>>();

    public void registerLineSerieRenderer(Serie<T> serie, PointBuilder<T> domainBuilder) {
        LineRenderer<T> renderer = new LineRenderer<T>(
                domainBuilder, xModel, yModel,
                getSerieClipPath(), g.getElement(),
                styles.serie(),
                styles.line());
        // store internally
        renderers.put(serie, renderer);
    }

    private Renderer<T> getRenderer(Serie<T> serie) {
        return renderers.get(serie);
    }

    @Override
    protected void redrawSeries() {
        super.redrawSeries();

        List<Serie<T>> series = model().series();
        for (Serie<T> serie : series) {
            Renderer<T> renderer = getRenderer(serie);
            if (renderer == null) {
                GWT.log("no renderer defined for the serie " + serie.id());
            }
            else {
                renderer.render(serie);
            }
        }

        // 2. NamedRanges
        // update the selection with a path for each NamedRange in a serie

        DefaultSelectionUpdater<NamedRange<T>> namedRangeDrawer =
                new DefaultSelectionUpdater<NamedRange<T>>("." + styles.namedRange()) {
                    // create a path
                    @Override
                    public String getElementName() {
                        return "path";
                    }

                    @Override
                    public String getKey(final NamedRange<T> datum, final int index) {
                        return datum.serie().id() + "." + datum.id();
                    }

                    // add the .namedRange class to the newly created
                    @Override
                    public void afterEnter(final Selection selection) {
                        super.afterEnter(selection);
                        selection.classed(styles.line(), true);
                        selection.classed(styles.namedRange(), true);
                        getSerieClipPath().apply(selection);
                    }

                    // update the d attribute
                    @Override
                    public void onJoinEnd(final Selection selection) {
                        super.onJoinEnd(selection);
                        // setting the attribute d of the path
                        selection.attr("d", new DatumFunction<String>() {
                            @Override
                            public String apply(final Element context, final Value d, final int index) {
                                NamedRange<T> namedRange = d.<NamedRange<T>> as();
                                // filter the points with the range
                                LineGenerator<T> lineGenerator = new LineGenerator<T>(pointBuilder, namedRange);
                                return lineGenerator.generate(namedRange.getValues());
                            }
                        });
                    }

                };
        // // create, update (and remove), a path element for each serie
        // SelectionDataJoiner.update(
        // g.select(), // inside the root G
        // model.series(),// the data is the series
        // serieDrawer
        // );

        // create, update (and remove), a path element for each named range of each serie
        series = model.series();
        for (Serie<T> serie : series) {
            List<NamedRange<T>> ranges = serie.getOverlappingRanges(xModel.visibleDomain());
            // draw with the appropriate renderer
            GWT.log("named ranges count:" + ranges.size());
            SelectionDataJoiner.update(g.select(), ranges, namedRangeDrawer);
        }

    }

    // ============= getters =============

    protected LineChart.Styles styles() {
        return styles;
    }

    /**
     * Return the model driving this chart.
     * 
     * @return the model
     */
    public LineChartModel<T, LinearScale> model() {
        return model;
    }

    // =========== listens to model events ==============

    Map<Serie<T>, HandlerRegistration> serieChangeRegistrations = new HashMap<Serie<T>, HandlerRegistration>();

    @Override
    public void onSerieRemoved(final SerieRemovedEvent<T> event) {
        HandlerRegistration registration = serieChangeRegistrations.remove(event.getSerie());
        if (registration != null) {
            registration.removeHandler();
        }
        redrawSeries();
    }

    @Override
    public void onSerieAdded(final SerieAddedEvent<T> event) {
        // attach listener on the serie
        serieChangeRegistrations.put(event.getSerie(), event.getSerie().addSerieChangeHandler(this));
        redrawSeries();
    }

    @Override
    public void onSerieChange(final SerieChangeEvent<T> event) {
        // TODO: find a way to redraw only the serie that changed
        redrawSeries();
    }

}

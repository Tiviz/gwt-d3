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
package com.github.gwtd3.ui.chart.renderer;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.UpdateSelection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.ui.chart.ClipPath;
import com.github.gwtd3.ui.model.AxisModel;
import com.github.gwtd3.ui.model.BarBuilder;
import com.github.gwtd3.ui.model.BarRectBuilder;
import com.github.gwtd3.ui.model.RectBuilder;
import com.github.gwtd3.ui.model.Serie;
import com.google.gwt.dom.client.Element;

public class BarRenderer<T, L> implements Renderer<T> {

    private final Element container;

    private final RectBuilder<T> rectBuilder;

    private final ClipPath clipPath;

    private String additionalStyleNames = "";

    public BarRenderer(final BarBuilder<T, Double> domainBuilder,
            final AxisModel<?> xModel, final AxisModel<?> yModel,
            final ClipPath clipPath, final Element container) {
        super();
        this.rectBuilder = new BarRectBuilder<T, Double>(xModel, yModel, domainBuilder);
        this.clipPath = clipPath;
        this.container = container;
    }

    @Override
    public void render(final Serie<T> serie) {
        // create or get a path element
        Selection rectangles = selectOrCreateRectangleSelection(serie);
        updateRectangle(rectangles);
    }

    /**
     * Select existing rectangles, create "missing" ones, delete "obsolete" rectangles.
     * <p>
     * 
     * @param serie
     * @return
     */
    private UpdateSelection selectOrCreateRectangleSelection(final Serie<T> serie) {
        // return D3.select(container).select("[name=serie_" + serie.id() + "]").node();
        // select all Rect with name=serie.id
        final UpdateSelection updatingRectangles =
                D3.select(container)
                        .selectAll("rect[name=\"serie_" + serie.id() + "\"]").data(serie.getValues());

        updatingRectangles.enter().append("rect")
                .attr("name", "serie_" + serie.id());
        // FIXME: default class name
        // .classed(additionalClassNames, true);

        // removing
        updatingRectangles.exit().remove();
        return updatingRectangles;
    }

    /**
     * Given a selection containing rect elements,
     * update their x, y, width and height attributes according to their data
     * transformed by the rectBuilder.
     * <p>
     * @param rectangles
     */
    private void updateRectangle(final Selection rectangles) {
        rectangles
                .attr("x", new DatumFunction<Double>() {
                    @Override
                    public Double apply(final Element context, final Value d, final int index) {
                        return rectBuilder.x(d.<T> as());
                    }
                })
                .attr("y", new DatumFunction<Double>() {
                    @Override
                    public Double apply(final Element context, final Value d, final int index) {
                        return rectBuilder.y(d.<T> as());
                    }
                })
                .attr("width", new DatumFunction<Double>() {
                    @Override
                    public Double apply(final Element context, final Value d, final int index) {
                        return rectBuilder.width(d.<T> as());
                    }
                })
                .attr("height", new DatumFunction<Double>() {
                    @Override
                    public Double apply(final Element context, final Value d, final int index) {
                        return rectBuilder.height(d.<T> as());
                    }
                })
                .attr("class", new DatumFunction<String>() {
                    @Override
                    public String apply(final Element context, final Value d, final int index) {
                        String styleNames = rectBuilder.styleNames(d.<T> as());
                        return additionalStyleNames + " " + styleNames;
                    }
                });
        clipPath.apply(rectangles);
    }

    public BarRenderer<T, L> addStyleNames(final String styleNames) {
        this.additionalStyleNames += " " + styleNames;
        return this;
    }

}

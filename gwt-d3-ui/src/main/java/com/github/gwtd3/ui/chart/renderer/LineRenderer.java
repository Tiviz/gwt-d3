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

import java.util.Arrays;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.ui.chart.ClipPath;
import com.github.gwtd3.ui.chart.LineGenerator;
import com.github.gwtd3.ui.model.AxisCoordsBuilder;
import com.github.gwtd3.ui.model.AxisModel;
import com.github.gwtd3.ui.model.PointBuilder;
import com.github.gwtd3.ui.model.RangeDomainFilter;
import com.github.gwtd3.ui.model.Serie;
import com.github.gwtd3.ui.svg.DOM;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Element;

public class LineRenderer<T> implements Renderer {

    private final Element container;
    private final LineGenerator<T> generator;
    private final AxisModel<?> xModel;
    private final AxisModel<?> yModel;

    private String additionalClassNames = "";
    private final ClipPath globalClipPath;
    private ClipPath localClipPath;

    private final Serie<T> serie;

    public LineRenderer(final Serie<T> serie, final PointBuilder<T> domainBuilder,
            final AxisModel<?> xModel, final AxisModel<?> yModel,
            final ClipPath clipPath, final Element container) {
        super();
        this.serie = serie;
        this.xModel = xModel;
        this.yModel = yModel;
        this.globalClipPath = clipPath;
        this.container = container;
        AxisCoordsBuilder<T> pointBuilder = new AxisCoordsBuilder<T>(xModel, yModel, domainBuilder);
        this.generator =
                new LineGenerator<T>(
                        pointBuilder,
                        new RangeDomainFilter<T>(domainBuilder,
                                xModel));
    }

    @Override
    public void render() {
        // create or get a path element
        Element path = getOrCreatePathElement();
        configurePathElement(path);
        path.setAttribute("d", generator.generate(serie.getValues()));

    }

    private Element getOrCreatePathElement() {
        Element e = findPath();
        if (e == null) {
            e = createPath();
        }
        return e;
    }

    private Element createPath() {
        Element pathElement = DOM.createSVGElement("path");
        pathElement.setAttribute("name", "serie_" + serie.id());
        container.appendChild(pathElement);
        return pathElement;
    }

    private Element findPath() {
        return D3.select(container).select("[name=\"serie_" + serie.id() + "\"]").node();
    }

    private void configurePathElement(final Element e) {
        // apply the cli path and the class names
        Selection select = D3.select(e);
        if (additionalClassNames != null) {
            select.classed(additionalClassNames, true);
        }
        applyClipPath(select);
    }

    private void applyClipPath(final Selection select) {
        if (localClipPath != null) {
            localClipPath.apply(select);
        }
        else {
            globalClipPath.apply(select);
        }
    }

    /**
     * Create a rectangular clip path that draw only the part of the lines
     * included in the given range.
     * <p>
     * The given range is expressed in terms of x domain space.
     * <p>
     * 
     * 
     * @param includedRange
     */
    public LineRenderer<T> include(final Range<Double> includedRange) {
        String id = "clip" + serie.id();

        // ENTER : create a clip path in the container (if needed)
        Selection containerSelection =
                D3.select(container)
                        .selectAll("#" + id);

        containerSelection
                .data(Arrays.asList(id))
                .enter()
                .append("clipPath")
                .attr("id", id)
                .attr("clip-path", "url(#" + globalClipPath.getId() + ")")
                .append("rect");

        // UPDATE the clip path
        double extent = Math.abs(includedRange.lowerEndpoint() - includedRange.upperEndpoint());
        containerSelection
                .selectAll("rect")
                .attr("x", xModel.toPixel(includedRange.lowerEndpoint()))
                .attr("y", 0)
                .attr("width", xModel.toPixelSize(extent))
                .attr("height", yModel.toPixelSize(yModel.visibleDomainLength()));
        // store the id to be used when path will be drawed
        if (localClipPath == null) {
            localClipPath = new ClipPath(id);
        }
        return this;
    }

    /**
     * Specify styleNames to be applied to the generated path lines
     * @param names
     * @return
     */
    public LineRenderer<T> addStyleNames(final String names) {
        this.additionalClassNames += " " + names;
        return this;
    }
}

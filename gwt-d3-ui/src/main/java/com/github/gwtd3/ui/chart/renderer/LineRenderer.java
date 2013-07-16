package com.github.gwtd3.ui.chart.renderer;

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
import com.google.gwt.user.client.Random;

public class LineRenderer<T> implements Renderer<T> {

    private final Element container;
    private final LineGenerator<T> generator;
    private final AxisModel<?> xModel;
    private final AxisModel<?> yModel;

    private String additionalClassNames = "";
    private ClipPath clipPath;

    public LineRenderer(final PointBuilder<T> domainBuilder,
            final AxisModel<?> xModel, final AxisModel<?> yModel,
            final ClipPath clipPath, final Element container) {
        super();
        this.xModel = xModel;
        this.yModel = yModel;
        this.clipPath = clipPath;
        this.container = container;
        AxisCoordsBuilder<T> pointBuilder = new AxisCoordsBuilder<T>(xModel, yModel, domainBuilder);
        this.generator =
                new LineGenerator<T>(
                        pointBuilder,
                        new RangeDomainFilter<T>(domainBuilder,
                                xModel));
    }

    @Override
    public void render(final Serie<T> serie) {
        // create or get a path element
        Element path = getOrCreatePathElement(serie);
        configurePathElement(path);
        path.setAttribute("d", generator.generate(serie.getValues()));

    }

    private Element getOrCreatePathElement(final Serie<T> serie) {
        Element e = findPath(serie);
        if (e == null) {
            e = createPath(serie);
        }
        return e;
    }

    private Element createPath(final Serie<T> serie) {
        Element pathElement = DOM.createSVGElement("path");
        pathElement.setAttribute("name", "serie_" + serie.id());
        container.appendChild(pathElement);
        return pathElement;
    }

    private Element findPath(final Serie<T> serie) {
        return D3.select(container).select("[name=\"serie_" + serie.id() + "\"]").node();
    }

    private void configurePathElement(final Element e) {
        // apply the cli path and the class names
        Selection select = D3.select(e);
        if (additionalClassNames != null) {
            select.classed(additionalClassNames, true);
        }
        clipPath.apply(select);
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
        // add a clip path in the container
        Selection inclusionClipPath =
                D3.select(container)
                        .append("clipPath")
                        .attr("id", "clip" + Random.nextInt(100000));
        // clip this clippath with the global clipath
        clipPath.apply(inclusionClipPath);
        double extent = Math.abs(includedRange.lowerEndpoint() - includedRange.upperEndpoint());
        inclusionClipPath.append("rect")
                .attr("x", xModel.toPixel(includedRange.lowerEndpoint()))
                .attr("y", 0)
                .attr("width", xModel.toPixelSize(extent))
                .attr("height", yModel.toPixelSize(yModel.visibleDomainLength()));
        // set the inclusion clip path to be applied on the serie at next redraw
        clipPath = new ClipPath(inclusionClipPath.attr("id"));
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

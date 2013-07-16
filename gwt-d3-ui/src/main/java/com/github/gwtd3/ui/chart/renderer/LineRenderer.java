package com.github.gwtd3.ui.chart.renderer;

import java.util.List;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.ui.chart.ClipPath;
import com.github.gwtd3.ui.chart.LineGenerator;
import com.github.gwtd3.ui.model.AxisCoordsBuilder;
import com.github.gwtd3.ui.model.AxisModel;
import com.github.gwtd3.ui.model.PointBuilder;
import com.github.gwtd3.ui.model.RangeDomainFilter;
import com.github.gwtd3.ui.model.Serie;
import com.github.gwtd3.ui.svg.DOM;
import com.google.gwt.dom.client.Element;

public class LineRenderer<T> implements Renderer<T> {

    private final PointBuilder<T> domainBuilder;

    private final PointBuilder<T> pointBuilder;

    private final ClipPath clipPath;

    private final AxisModel<?> xModel;

    private final Element container;

    private final String classNameSpecifier;

    private final String additionalClassNames;

    public LineRenderer(PointBuilder<T> domainBuilder, AxisModel<?> xModel, AxisModel<?> yModel, ClipPath clipPath, Element container, String classNameSpecifier, String additionalClassNames) {
        super();
        this.xModel = xModel;
        this.domainBuilder = domainBuilder;
        this.pointBuilder = new AxisCoordsBuilder<T>(xModel, yModel, domainBuilder);
        this.clipPath = clipPath;
        this.container = container;
        this.classNameSpecifier = classNameSpecifier;
        this.additionalClassNames = additionalClassNames;
    }

    @Override
    public void render(Serie<T> serie) {
        // create or get a path element
        Element path = getOrCreatePathElement(serie);
        configurePathElement(path);
        path.setAttribute("d", computePathDataAttribute(serie));

    }

    private Element getOrCreatePathElement(Serie<T> serie) {
        Element e = findPath(serie);
        if (e == null) {
            e = createPath(serie);
        }
        return e;
    }

    private Element createPath(Serie<T> serie) {
        Element pathElement = DOM.createSVGElement("path");
        container.appendChild(pathElement);
        container.setAttribute("name", "serie_" + serie.id());
        return pathElement;
    }

    private Element findPath(Serie<T> serie) {
        return D3.select(container).select("[name=serie_" + serie.id() + "]").node();
    }

    private void configurePathElement(Element e) {
        e.addClassName(additionalClassNames);
        e.addClassName(classNameSpecifier);
        clipPath.apply(e);
    }

    private String computePathDataAttribute(Serie<T> serie) {
        List<T> values = serie.getValues();
        // generate the "d" attribute with a linegenerator
        LineGenerator<T> generator =
                new LineGenerator<T>(
                        pointBuilder,
                        new RangeDomainFilter<T>(domainBuilder,
                                xModel));
        return generator.generate(values);
    }

}

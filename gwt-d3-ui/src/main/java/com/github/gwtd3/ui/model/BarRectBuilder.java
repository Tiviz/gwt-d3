package com.github.gwtd3.ui.model;

public class BarRectBuilder<T, L> implements RectBuilder<T> {

    private final AxisModel<?> xModel;
    private final AxisModel<?> yModel;
    private final BarBuilder<T, Double> domainBuilder;

    public BarRectBuilder(final AxisModel<?> xModel, final AxisModel<?> yModel, final BarBuilder<T, Double> domainBuilder) {
        super();
        this.xModel = xModel;
        this.yModel = yModel;
        this.domainBuilder = domainBuilder;
    }

    @Override
    public double x(final T value) {
        return xModel.toPixel(domainBuilder.location(value) - (domainBuilder.width(value) / 2));
    }

    @Override
    public double y(final T value) {
        return yModel.toPixel(domainBuilder.height(value));
        // return yModel.toPixelSize(yModel.visibleDomainLength()) - height(value);
    }

    @Override
    public double width(final T value) {
        return xModel.toPixelSize(domainBuilder.width(value));
    }

    @Override
    public double height(final T value) {
        double height = domainBuilder.height(value);
        System.out.println(height);
        return yModel.toPixelSize(height);
    }

    @Override
    public String styleNames(final T value) {
        return domainBuilder.styleNames(value);
    }
}

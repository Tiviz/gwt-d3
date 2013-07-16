package com.github.gwtd3.ui.model;

/**
 * Convert domain objects into bars, like in a bar chart.
 * <p>
 * A bar has a width and a height, one of which should encode the visualized domain dimension (for instance, for
 * vertical bars, the height should encode the visualized domain dimension, and the width should be constant).
 * <p>
 * The bar will be located on its axis using the value returned by location.
 * <p>
 * 
 * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
 * 
 * @param <T> type of the domain object
 * @param <L> type of the object used to locate the bar
 */
public interface BarBuilder<T, L> {
    double width(T value);

    double height(T value);

    L location(T value);

    String styleNames(T value);
}

package com.github.gwtd3.ui.chart.renderer;

import com.github.gwtd3.ui.model.Serie;

/**
 * Renderer allow a {@link Serie} to be drawn in a document.
 * <p>
 * 
 * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
 * 
 * @param <T>
 */
public interface Renderer<T> {

    /**
     * The renderer used in the frame of
     * @param serie
     */
    void render(Serie<T> serie);

}

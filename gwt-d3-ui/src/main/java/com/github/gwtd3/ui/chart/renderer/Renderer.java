package com.github.gwtd3.ui.chart.renderer;

import com.github.gwtd3.ui.model.Serie;

public interface Renderer<T> {

    public void render(Serie<T> serie);

}

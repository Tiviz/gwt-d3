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
        return yModel.toPixelSize(height);
    }

    @Override
    public String styleNames(final T value) {
        return domainBuilder.styleNames(value);
    }
}

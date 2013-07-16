package com.github.gwtd3.ui.model;

import java.util.List;

import com.github.gwtd3.ui.event.SerieChangeEvent;
import com.google.common.base.Predicate;
import com.google.common.collect.Range;

/**
 * A NamedRange provides a way of logically grouping contiguous values of a {@link Serie}, in order to apply on
 * these values specific formatting or
 * behavior.
 * <p>
 * The NamedRange is a {@link Predicate} allowing to filter domain values.
 * 
 * @author <a href="mailto:schiochetanthoni@gmail.com">Anthony Schiochet</a>
 * 
 */
public class NamedRange<T> implements ValueProvider<T>, DomainFilter<T> {
    /**
     * 
     */
    private final String id;
    private final Range<Double> range;
    private final Serie<T> serie;
    private int startIndex;
    private int endIndex;
    private String classNames;

    protected NamedRange(final Serie<T> serie, final String id, final Range<Double> range) {
        super();
        this.id = id;
        this.range = range;
        this.serie = serie;
    }

    // ============== styling ====================
    /**
     * @param classNames
     */
    public NamedRange<T> setClassNames(final String classNames) {
        this.classNames = classNames;
        serie.fireEvent(new SerieChangeEvent<T>(this.serie));
        return this;
    }

    public String getClassNames() {
        return classNames;
    }

    public String id() {
        return id;
    }

    public Range<Double> range() {
        return range;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "'" + id + "'[" + range.toString() + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NamedRange)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        NamedRange<T> other = (NamedRange<T>) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public Serie<T> serie() {
        return serie;
    }

    @Override
    public List<T> getValues() {
        return this.serie.values;
    }

    @Override
    public boolean accept(final T value) {
        return range.contains(this.serie.domainBuilder.x(value));
    }
}
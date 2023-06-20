package org.aksw.jenax.vaadin.component.grid.sparql;

import java.util.Objects;

public class SampleRange {
    protected final Long scanOffset;
    protected final Long scanLimit;
    protected final Long entityOffset;
    protected final Long entityLimit;

    public SampleRange(Long scanOffset, Long scanLimit, Long entityOffset, Long entityLimit) {
        super();
        this.scanOffset = scanOffset;
        this.scanLimit = scanLimit;
        this.entityOffset = entityOffset;
        this.entityLimit = entityLimit;
    }

    public Long getScanOffset() {
        return scanOffset;
    }

    public Long getScanLimit() {
        return scanLimit;
    }

    public Long getEntityOffset() {
        return entityOffset;
    }

    public Long getEntityLimit() {
        return entityLimit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityLimit, entityOffset, scanLimit, scanOffset);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SampleRange other = (SampleRange) obj;
        return Objects.equals(entityLimit, other.entityLimit) && Objects.equals(entityOffset, other.entityOffset)
                && Objects.equals(scanLimit, other.scanLimit) && Objects.equals(scanOffset, other.scanOffset);
    }

    @Override
    public String toString() {
        return "SampleRange [scanOffset=" + scanOffset + ", scanLimit=" + scanLimit + ", entityOffset=" + entityOffset
                + ", entityLimit=" + entityLimit + "]";
    }
}

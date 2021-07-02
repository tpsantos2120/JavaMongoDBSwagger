package com.mongodb.dtos;

import java.util.Objects;

public class AverageAge {

    private double averageAge;

    public double getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AverageAge that = (AverageAge) o;
        return Double.compare(that.averageAge, averageAge) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(averageAge);
    }
}

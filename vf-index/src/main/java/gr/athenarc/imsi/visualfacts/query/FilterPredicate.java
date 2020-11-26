package gr.athenarc.imsi.visualfacts.query;

import com.google.common.math.StatsAccumulator;

import java.util.function.Predicate;

public class FilterPredicate implements Predicate<Float> {

    private final FilterOperator operator;

    private final float constant;

    public FilterPredicate(FilterOperator operator, float constant) {
        this.operator = operator;
        this.constant = constant;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public float getConstant() {
        return constant;
    }

    @Override
    public boolean test(Float value) {
        if (value == null) {
            return false;
        }
        switch (operator) {
            case EQUAL:
                return value.equals(constant);
            case LESS_THAN:
                return value < constant;
            case NOT_EQUAL:
                return value != constant;
            case GREATER_THAN:
                return value > constant;
            case LESS_THAN_OR_EQUAL:
                return value <= constant;
            case GREATER_THAN_OR_EQUAL:
                return value >= constant;
            default:
                return false;
        }
    }

    /**
     * Test if objects of a tile satisfy the predicate
     *
     * @param tileStatsAcc
     * @return true if all objects satisfy, false if all objects do not satisfy, otherwise null
     */
    public Boolean testTile(StatsAccumulator tileStatsAcc) {
        double min = tileStatsAcc.min();
        double max = tileStatsAcc.max();
        switch (operator) {
            case LESS_THAN:
                if (constant < min)
                    return false;
                else if (constant > max)
                    return true;
            case GREATER_THAN:
                if (constant < min)
                    return true;
                else if (constant > max)
                    return false;
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "FilterPredicate{" +
                "operator=" + operator +
                ", constant=" + constant +
                '}';
    }
}

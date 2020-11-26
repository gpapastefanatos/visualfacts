package gr.athenarc.imsi.visualfacts;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import gr.athenarc.imsi.visualfacts.query.Query;

import java.util.Map;

import static gr.athenarc.imsi.visualfacts.config.IndexConfig.*;

public class CategoricalColumn {

    private int index;

    //private Map<String, Short> valueMap = new HashMap<>();
    private BiMap<String, Short> valueMap = HashBiMap.create();


    public CategoricalColumn(int index) {
        this.index = index;
    }

    public short getValueKey(String value) {
        return valueMap.computeIfAbsent(value, s -> (short) (valueMap.size()));
    }

    public String getValue(Short key) {
        return valueMap.inverse().get(key);
    }

    public Map<String, Short> getValueMap() {
        return valueMap;
    }

    public int getIndex() {
        return index;
    }

    public int getCardinality() {
        return valueMap.size();
    }

    @Override
    public String toString() {
        return "{" +
                "col=" + index +
                ", card=" + getCardinality() +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoricalColumn that = (CategoricalColumn) o;

        return index == that.index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    public double getScore(Query q0) {
        double score;
        if (q0.getCategoricalFilters() != null && q0.getCategoricalFilters().containsKey(this.getIndex())) {
            score = FILTER_SCORE;
        } else if (q0.getGroupByCol() != null && q0.getGroupByCol() == this.getIndex()) {
            score = GROUP_BY_SCORE;
        } else {
            score = DEFAULT_SCORE;
        }
        return score;
    }
}

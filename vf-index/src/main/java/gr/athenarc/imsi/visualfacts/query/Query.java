package gr.athenarc.imsi.visualfacts.query;

import gr.athenarc.imsi.visualfacts.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Query {

    private final Rectangle rect;

    // map from column index to filter value
    private final Map<Integer, String> categoricalFilters;

    private final Integer groupByCol;

    private final Integer measureCol;

    public Query(Rectangle rect, Map<Integer, String> categoricalFilters, Integer groupByCol, Integer measureCol) {
        this.rect = rect;
        this.categoricalFilters = categoricalFilters != null ? categoricalFilters : new HashMap<>();
        this.groupByCol = groupByCol;
        this.measureCol = measureCol;
    }

    public Rectangle getRect() {
        return rect;
    }

    public Map<Integer, String> getCategoricalFilters() {
        return categoricalFilters;
    }

    public Integer getGroupByCol() {
        return groupByCol;
    }

    public Integer getMeasureCol() {
        return measureCol;
    }

    @Override
    public String toString() {
        return "Query{" +
                "rect=" + rect +
                ", categoricalFilters=" + categoricalFilters +
                ", groupByCol=" + groupByCol +
                ", measureCol=" + measureCol +
                '}';
    }
}
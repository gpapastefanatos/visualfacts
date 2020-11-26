package gr.athenarc.imsi.visualfacts;

import gr.athenarc.imsi.visualfacts.query.FilterPredicate;

public class Filter {
    private int filterColumn;

    private FilterPredicate filterPredicate;

    public Filter(int filterColumn, FilterPredicate filterPredicate) {
        this.filterColumn = filterColumn;
        this.filterPredicate = filterPredicate;
    }

    public boolean test(Float value){
        return filterPredicate.test(value);
    }

    public int getFilterColumn() {
        return filterColumn;
    }

    public void setFilterColumn(int filterColumn) {
        this.filterColumn = filterColumn;
    }

    public FilterPredicate getFilterPredicate() {
        return filterPredicate;
    }

    public void setFilterPredicate(FilterPredicate filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "filterColumn=" + filterColumn +
                ", filterPredicate=" + filterPredicate +
                '}';
    }
}

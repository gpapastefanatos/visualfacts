package gr.athenarc.imsi.visualfacts.query;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryResults {

    private Query query;

    private Map<String, StatsAccumulator> stats;

    private int fullyContainedTileCount;

    private int tileCount;

    private int expandedNodeCount;

    private int ioCount;

    public QueryResults(Query query) {
        this.query = query;
        this.stats = new HashMap<>();
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Map<String, StatsAccumulator> getStats() {
        return stats;
    }

    public void adjustStats(String groupByValue, float measureValue) {
        stats.computeIfAbsent(groupByValue, (v) -> new StatsAccumulator()).add(measureValue);
    }

    public void adjustStats(String groupByValue, Stats stats) {
        this.stats.computeIfAbsent(groupByValue, (v) -> new StatsAccumulator()).addAll(stats);
    }


    public int getFullyContainedTileCount() {
        return fullyContainedTileCount;
    }

    public void setFullyContainedTileCount(int fullyContainedTileCount) {
        this.fullyContainedTileCount = fullyContainedTileCount;
    }

    public int getTileCount() {
        return tileCount;
    }

    public void setTileCount(int tileCount) {
        this.tileCount = tileCount;
    }

    public int getIoCount() {
        return ioCount;
    }

    public void setIoCount(int ioCount) {
        this.ioCount = ioCount;
    }

    public int getExpandedNodeCount() {
        return expandedNodeCount;
    }

    public void setExpandedNodeCount(int expandedNodeCount) {
        this.expandedNodeCount = expandedNodeCount;
    }

    @Override
    public String toString() {
        return "QueryResults{" +
                "query=" + query +
                ", stats=" + stats +
                ", fullyContainedTileCount=" + fullyContainedTileCount +
                ", tileCount=" + tileCount +
                ", expandedNodeCount=" + expandedNodeCount +
                ", ioCount=" + ioCount +
                '}';
    }
}

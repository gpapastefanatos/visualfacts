package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.Schema;
import gr.athenarc.imsi.visualfacts.Tile;
import gr.athenarc.imsi.visualfacts.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public class NaiveInitializationPolicy extends InitializationPolicy {
    private static final Logger LOG = LogManager.getLogger(NaiveInitializationPolicy.class);

    public NaiveInitializationPolicy(Query q0, int noOfSubTiles, Schema schema, Integer categoricalNodeBudget) {
        super(q0, noOfSubTiles, schema, categoricalNodeBudget);
    }

    public double initTileTreeCategoricalAttrs(List<Tile> leafTiles) {
        if (categoricalColumns == null || categoricalColumns.size() == 0)
            return -1d;
        Comparator<Tile> comparator = Comparator.comparingDouble(tile -> this.computeTileProbPerSurfaceArea(tile));
        leafTiles.sort(comparator.reversed());

        int treeCount = 0;
        double totalUtil = 0d;
        for (Tile leafTile : leafTiles) {
            int cost = computeTileTreeCostEstimate(leafTile, categoricalColumns);
            if (cost <= catNodeBudget) {
                leafTile.setCategoricalColumns(categoricalColumns);
                totalUtil += computeTileTreeUtil(leafTile, categoricalColumns);
                catNodeBudget -= cost;
                treeCount++;
            } else {
                break;
            }
        }
        LOG.debug("Initial NAIVE INIT assignments: " + treeCount);

        return totalUtil;
    }
}

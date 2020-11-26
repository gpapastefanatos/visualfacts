package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.CategoricalColumn;
import gr.athenarc.imsi.visualfacts.Schema;
import gr.athenarc.imsi.visualfacts.Tile;
import gr.athenarc.imsi.visualfacts.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class RandomInitializationPolicy extends InitializationPolicy {
    private static final Logger LOG = LogManager.getLogger(RandomInitializationPolicy.class);

    public RandomInitializationPolicy(Query q0, int noOfSubTiles, Schema schema, Integer categoricalNodeBudget) {
        super(q0, noOfSubTiles, schema, categoricalNodeBudget);
    }

    @Override
    public double initTileTreeCategoricalAttrs(List<Tile> leafTiles) {
        if (categoricalColumns == null || categoricalColumns.size() == 0)
            return -1d;

        Comparator<Tile> comparator = Comparator.comparingDouble(tile -> this.computeTileProbPerSurfaceArea(tile));
        leafTiles.sort(comparator.reversed());

        Random attrCountRandom = new Random(0);
        Random attrListShuffleRandom = new Random(0);

        int[] treesByAttrCount = new int[categoricalColumns.size()];
        double totalUtil = 0d;
        Iterator<Tile> it = leafTiles.iterator();
        while (catNodeBudget > 0 && it.hasNext()) {
            Tile tile = it.next();
            int treeAttrCount = attrCountRandom.nextInt(categoricalColumns.size()) + 1;
            Collections.shuffle(categoricalColumns, attrListShuffleRandom);
            List<CategoricalColumn> assignedCatAttrs = categoricalColumns.subList(0, treeAttrCount);
            int costEstimate = computeTileTreeCostEstimate(tile, assignedCatAttrs);
            if (catNodeBudget >= costEstimate) {
                //we sort the attrs in an assigned tree by their cardinality so that attrs with smaller domain go higher in the tree
                assignedCatAttrs.sort(Comparator.comparingInt(CategoricalColumn::getCardinality));
                tile.setCategoricalColumns(assignedCatAttrs);
                catNodeBudget -= costEstimate;
                totalUtil += computeTileTreeUtil(tile, assignedCatAttrs);
                treesByAttrCount[treeAttrCount - 1]++;
            }
        }

        LOG.debug("Initial RANDOM INIT assignments: " + Arrays.toString(treesByAttrCount));
        return totalUtil;
    }
}

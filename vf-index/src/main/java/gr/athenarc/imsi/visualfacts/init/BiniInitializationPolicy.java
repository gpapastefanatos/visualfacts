package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.CategoricalColumn;
import gr.athenarc.imsi.visualfacts.Schema;
import gr.athenarc.imsi.visualfacts.Tile;
import gr.athenarc.imsi.visualfacts.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class BiniInitializationPolicy extends InitializationPolicy {
    private static final Logger LOG = LogManager.getLogger(BiniInitializationPolicy.class);

    //private double[] treeUtils;
    //private double[] attrProbs;

    public BiniInitializationPolicy(Query q0, int noOfSubTiles, Schema schema, Integer categoricalNodeBudget) {
        super(q0, noOfSubTiles, schema, categoricalNodeBudget);

        /*double totalScore = 0;
        for (int i = 0; i < categoricalColumns.size(); i++) {
            totalScore += getAttrScore(q0, categoricalColumns.get(i));
        }
        double defaultAttrProb = 1d / totalScore;
        attrProbs = new double[categoricalColumns.size()];
        for (int i = 0; i < categoricalColumns.size(); i++) {
            attrProbs[i] = getAttrScore(q0, categoricalColumns.get(i)) * defaultAttrProb;
        }*/

/*        treeUtils = new double[categoricalColumns.size()];
        for (int i = 0; i < categoricalColumns.size(); i++) {
            treeUtils[i] = computeTreeProb(i + 1);
            LOG.debug((i + 1) + "   " + treeUtils[i]);
        }*/
    }

/*    private double computeTileTreeUtil(Tile tile, int treeAttrCount) {
        double expectedIO = 0;
        double objectCountEstimate = estimateTileObjectCount(tile);
        for (int i = 0; i < categoricalColumns.size(); i++) {
            CategoricalColumn categoricalColumn = categoricalColumns.get(i);
            expectedIO += i < treeAttrCount ? attrProbs[i] * objectCountEstimate / categoricalColumn.getCardinality() : attrProbs[i] * objectCountEstimate;
        }
        return computeTileProb(tile) * (objectCountEstimate - expectedIO) / Math.pow(tile.getBounds().getSurfaceArea(), 2);
    }*/


    @Override
    public double initTileTreeCategoricalAttrs(List<Tile> leafTiles) {
        if (categoricalColumns == null || categoricalColumns.size() == 0)
            return -1d;

        List<TileTreePair> tileTreePairs = new ArrayList<>(leafTiles.size() * categoricalColumns.size());

        double totalUtil = 0d;

        for (Tile tile : leafTiles) {
            for (int i = 1; i <= categoricalColumns.size(); i++) {
                List<CategoricalColumn> catAttrs = new ArrayList<>(categoricalColumns.subList(0, i));
                int costEstimate = computeTileTreeCostEstimate(tile, catAttrs);
                double util = computeTileTreeUtil(tile, catAttrs);
                TileTreePair tileTreePair = new TileTreePair(tile, catAttrs, costEstimate, util);
                tileTreePairs.add(tileTreePair);
            }
        }
        Comparator<TileTreePair> comparator = Comparator.comparingDouble(value -> value.getUtil());
        tileTreePairs.sort(comparator.reversed());

        Iterator<TileTreePair> it = tileTreePairs.iterator();

        int[] treesByAttrCount = new int[categoricalColumns.size()];
        while (catNodeBudget > 0 && it.hasNext()) {
            TileTreePair tileTreePair = it.next();
            Tile leafTile = tileTreePair.getTile();
            int costEstimate = tileTreePair.getCostEstimate();
            if (leafTile.getCategoricalColumns() == null && catNodeBudget >= costEstimate) {
                //LOG.debug("treeAttrCount: " + tileTreePair.getTreeAttrCount() + ", util: " + tileTreePair.getUtil() + ", tree cost estimate: " + costEstimate + ", distance " + q0.getRect().distanceFrom(leafTile.getBounds()));
                List<CategoricalColumn> assignedAttrs = tileTreePair.getCategoricalColumns();
                //we sort the attrs in an assigned tree by their cardinality so that attrs with smaller domain go higher in the tree
                assignedAttrs.sort(Comparator.comparingInt(CategoricalColumn::getCardinality));
                leafTile.setCategoricalColumns(assignedAttrs);
                catNodeBudget -= costEstimate;
                totalUtil += tileTreePair.getUtil();
                treesByAttrCount[assignedAttrs.size() - 1]++;
            }
        }
        LOG.debug("Initial BINI assignments: " + Arrays.toString(treesByAttrCount));

        return totalUtil;
    }
}

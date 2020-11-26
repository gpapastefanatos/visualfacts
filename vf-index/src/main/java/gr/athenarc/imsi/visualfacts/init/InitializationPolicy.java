package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.CategoricalColumn;
import gr.athenarc.imsi.visualfacts.Rectangle;
import gr.athenarc.imsi.visualfacts.Schema;
import gr.athenarc.imsi.visualfacts.Tile;
import gr.athenarc.imsi.visualfacts.query.Query;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class InitializationPolicy {

    private static final Logger LOG = LogManager.getLogger(InitializationPolicy.class);
    protected Integer catNodeBudget;
    protected List<CategoricalColumn> categoricalColumns;
    protected Query q0;
    protected double queryCenterX;
    protected double queryCenterY;
    protected double queryXSize;
    protected double queryYSize;
    Schema schema;
    private NormalDistribution distributionX;
    private NormalDistribution distributionY;
    private int noOfSubtiles;

    public InitializationPolicy(Query q0, int noOfSubTiles, Schema schema, Integer catNodeBudget) {
        this.q0 = q0;
        Rectangle rect = q0.getRect();
        queryCenterX = rect.getCenterX();
        queryCenterY = rect.getCenterY();
        queryXSize = rect.getXSize();
        queryYSize = rect.getYSize();
        this.distributionX = new NormalDistribution(queryCenterX, queryXSize);
        this.distributionY = new NormalDistribution(queryCenterY, queryYSize);

        // noOfSubtiles is the number of tiles distributed based on the prob.
        this.noOfSubtiles = noOfSubTiles;

        this.catNodeBudget = catNodeBudget;
        this.schema = schema;
        this.categoricalColumns = new ArrayList<>(schema.getCategoricalColumns());
        Comparator<CategoricalColumn> comparator = Comparator.comparingDouble(categoricalColumn ->
                categoricalColumn.getScore(q0) / categoricalColumn.getCardinality());

        this.categoricalColumns.sort(comparator.reversed());
    }

    public static InitializationPolicy getInitializationPolicy(String initMode, Query q0, int noOfSubTiles, Schema schema, Integer categoricalNodeBudget) {
        initMode = initMode == null ? "" : initMode;
        switch (initMode) {
            case "valinor":
                return new InitializationPolicyImpl(q0, noOfSubTiles, schema, categoricalNodeBudget);
            case "bini":
                return new BiniInitializationPolicy(q0, noOfSubTiles, schema, categoricalNodeBudget);
            case "naive":
                return new NaiveInitializationPolicy(q0, noOfSubTiles, schema, categoricalNodeBudget);
            case "random":
                return new RandomInitializationPolicy(q0, noOfSubTiles, schema, categoricalNodeBudget);
        }
        throw new IllegalArgumentException("Invalid init mode");
    }


    protected double computeTileProbPerSurfaceArea(Tile tile) {
        return computeRectProb(tile.getBounds()) / tile.getBounds().getSurfaceArea();
    }

    protected double computeTileTreeUtil(Tile tile, List<CategoricalColumn> categoricalColumns) {
        return (computeTileProbPerSurfaceArea(tile)) * computeTreeUtil(categoricalColumns);
    }

    protected double computeTreeUtil(List<CategoricalColumn> categoricalColumns) {
        double util = 0d;
        for (CategoricalColumn categoricalColumn : categoricalColumns) {
            double score = categoricalColumn.getScore(q0);
            util += score;
        }
        return util;
        /*double product = 1d;
        for (int i = 0; i < treeAttrCount; i++) {
            CategoricalColumn categoricalColumn = categoricalColumns.get(i);
            double score = getAttrScore(q0, categoricalColumn);
            product *= 1 - score;
        }
        return 1 - product;*/
    }

    protected int computeTileTreeCostEstimate(Tile tile, List<CategoricalColumn> categoricalColumns) {
        int objectCountEstimate = estimateTileObjectCount(tile);
        int cost = 0;
        for (CategoricalColumn categoricalColumn : categoricalColumns) {
            int currentLevelCost = cost == 0 ? categoricalColumn.getCardinality() : categoricalColumn.getCardinality() * cost;
            currentLevelCost = Math.min(objectCountEstimate, currentLevelCost);
            cost += currentLevelCost;
        }
        return cost;
    }

    protected int estimateTileObjectCount(Tile tile) {
        return (int) Math.ceil(schema.getObjectCount() * (tile.getBounds().getSurfaceArea() / schema.getBounds().getSurfaceArea()));
    }

    protected double computeRectProb(Rectangle rect) {
        return distributionX.probability(rect.getXRange().lowerEndpoint(), rect.getXRange().upperEndpoint()) *
                distributionY.probability(rect.getYRange().lowerEndpoint(), rect.getYRange().upperEndpoint());
    }

    public abstract double initTileTreeCategoricalAttrs(List<Tile> leafTiles);

    public int computeSplitSize(Rectangle rect) {
        int splitSize = (int) Math.floor(Math.sqrt(noOfSubtiles * this.computeRectProb(rect)));
        return splitSize;
    }
}

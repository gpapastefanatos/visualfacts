package gr.athenarc.imsi.visualfacts.init;

import gr.athenarc.imsi.visualfacts.CategoricalColumn;
import gr.athenarc.imsi.visualfacts.Tile;

import java.util.List;

public class TileTreePair {
    private final Tile tile;
    private final List<CategoricalColumn> categoricalColumns;
    private final int costEstimate;
    private final double util;

    public TileTreePair(Tile tile, List<CategoricalColumn> categoricalColumns, int costEstimate, double util) {
        this.tile = tile;
        this.categoricalColumns = categoricalColumns;
        this.costEstimate = costEstimate;
        this.util = util;
    }

    public Tile getTile() {
        return tile;
    }

    public List<CategoricalColumn> getCategoricalColumns() {
        return categoricalColumns;
    }

    public int getCostEstimate() {
        return costEstimate;
    }

    public double getUtil() {
        return util;
    }
}

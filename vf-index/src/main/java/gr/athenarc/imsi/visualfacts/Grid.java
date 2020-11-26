package gr.athenarc.imsi.visualfacts;

import com.google.common.collect.Range;
import gr.athenarc.imsi.visualfacts.init.InitializationPolicy;
import gr.athenarc.imsi.visualfacts.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Grid extends Tile {

    private static final Logger LOG = LogManager.getLogger(Grid.class);
    List<CategoricalColumn> categoricalColumns;
    InitializationPolicy initializationPolicy;
    private Tile[][] tiles;
    private Range<Float>[] xRanges;
    private Range<Float>[] yRanges;
    private int gridSize;
    private float rowSize;
    private float colSize;

    //private RangeMap<Float, Integer> xRangeMap = TreeRangeMap.create();
    //private RangeMap<Float, Integer> yRangeMap = TreeRangeMap.create();

    public Grid(InitializationPolicy initializationPolicy, Rectangle bounds, List<CategoricalColumn> categoricalColumns, int gridSize) {
        super(bounds);
        this.categoricalColumns = categoricalColumns;
        this.gridSize = gridSize;
        this.initializationPolicy = initializationPolicy;
    }


    public void split() {
        if (this.tiles != null)
            return;


        this.yRanges = createSubranges(this.bounds.getYRange(), gridSize);
        this.xRanges = createSubranges(this.bounds.getXRange(), gridSize);

        rowSize = yRanges[0].upperEndpoint() - yRanges[0].lowerEndpoint();
        colSize = xRanges[0].upperEndpoint() - xRanges[0].lowerEndpoint();

        tiles = new Tile[gridSize][gridSize];

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int splitSize = 0;
                Range<Float> colTileRange = xRanges[j], rowTileRange = yRanges[i];
                Rectangle rect = new Rectangle(colTileRange, rowTileRange);
                if (initializationPolicy != null) {
                    splitSize = initializationPolicy.computeSplitSize(rect);
                }
                if (splitSize > 1) {
                    Grid subGrid = new Grid(null, rect, categoricalColumns, splitSize);
                    subGrid.split();
                    tiles[i][j] = subGrid;
                    //LOG.debug("Initial split of tile " + colTileRange + rowTileRange + " : " + splitSize * splitSize);
                } else {
                    tiles[i][j] = new QuadTreeTile(rect);
                }
            }
        }

        if (this.root != null) {
            this.reAddPoints(root, new Stack<>());
            this.root = null;
        }
    }


    private Range[] createSubranges(Range<Float> range, int count) {
        Range<Float>[] subranges = new Range[count];
        float upper = range.lowerEndpoint();
        float rangeSize = (range.upperEndpoint() - range.lowerEndpoint()) / count;
        for (int i = 0; i < count; i++) {
            Range<Float> subrange;
            if (i == count - 1) {
                subrange = Range.closed(upper, range.upperEndpoint());
            } else {
                subrange = Range.closedOpen(upper, (upper += rangeSize));
            }
            subranges[i] = subrange;
        }
        return subranges;
    }

    private Integer getRowIndex(float y) {
        //return yRangeMap.get(y);

        float yMin = this.bounds.getYRange().lowerEndpoint();
        int i = (int) Math.floor((y - yMin) / rowSize);
        if (i == gridSize) {
            i = gridSize - 1;
        }
        return i;
    }

    private Integer getColIndex(float x) {
        //return xRangeMap.get(x);

        float xMin = this.bounds.getXRange().lowerEndpoint();
        int j = (int) Math.floor((x - xMin) / colSize);
        if (j == gridSize) {
            j = gridSize - 1;
        }
        return j;
    }


    //returns the index leaf tiles that contain the specified query. it is assumed for simplicity that the query ranges are open
    @Override
    public List<Tile> getOverlappedLeafTiles(Query query) {
        List leafTiles = new ArrayList();
        Range<Float> queryXRange, queryYRange;
        Rectangle rect = query.getRect();
        try {
            queryXRange = rect.getXRange().intersection(this.bounds.getXRange());
            queryYRange = rect.getYRange().intersection(this.bounds.getYRange());
        } catch (IllegalArgumentException e) {
            return leafTiles;
        }

        if (queryXRange.isEmpty() || queryYRange.isEmpty()) {
            return leafTiles;
        }

        int iMin = getRowIndex(queryYRange.lowerEndpoint());
        int iMax = getRowIndex(queryYRange.upperEndpoint());
        if (tiles[iMax][0].getBounds().getYRange().lowerEndpoint().equals(queryYRange.upperEndpoint()) && iMax > 0) {
            iMax--;
        }
        int jMin = getColIndex(queryXRange.lowerEndpoint());
        int jMax = getColIndex(queryXRange.upperEndpoint());
        if (tiles[0][jMax].getBounds().getXRange().lowerEndpoint().equals(queryXRange.upperEndpoint()) && jMax > 0) {
            jMax--;
        }
        for (int i = iMin; i <= iMax; i++) {
            for (int j = jMin; j <= jMax; j++) {
                leafTiles.addAll(tiles[i][j].getOverlappedLeafTiles(query));
            }
        }
        return leafTiles;
    }

    @Override
    public TreeNode addPoint(Point point, String[] row) {
        return this.getLeafTile(point).addPoint(point, row);
    }

    @Override
    public Tile getLeafTile(Point point) {
        if (this.tiles == null) {
            return this;
        } else {
            try {
                return tiles[getRowIndex(point.getY())][getColIndex(point.getX())].getLeafTile(point);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error(e);
                throw e;
            }
        }
    }

    @Override
    public List getLeafTiles() {
        List leafTiles = new ArrayList();
        if (this.tiles == null) {
            leafTiles.add(this);
        } else {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    leafTiles.addAll(tiles[i][j].getLeafTiles());
                }
            }
        }
        return leafTiles;
    }


    @Override
    public String toString() {
        return tiles.length + "";
    }

    public String printTiles() {
        return Arrays.deepToString(tiles);
    }

    public int getLeafTileCount() {
        if (tiles == null) {
            return 1;
        }

        int count = 0;
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                count += tile.getLeafTileCount();
            }
        }
        return count;
    }

    public int getMaxDepth() {
        int depth = 0;
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                depth = Integer.max(depth, tile.getMaxDepth() + 1);
            }
        }
        return depth;
    }
}
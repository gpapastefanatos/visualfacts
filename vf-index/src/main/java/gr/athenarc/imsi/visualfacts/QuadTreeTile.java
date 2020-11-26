package gr.athenarc.imsi.visualfacts;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import gr.athenarc.imsi.visualfacts.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class QuadTreeTile extends Tile {

    private static final Logger LOG = LogManager.getLogger(QuadTreeTile.class);

    private QuadTreeTile topLeft, topRight, bottomLeft, bottomRight;

    public QuadTreeTile(Rectangle bounds) {
        super(bounds);
    }

    @Override
    public List getLeafTiles() {
        List leafTiles = new ArrayList();
        if (this.topLeft == null) {
            leafTiles.add(this);
        } else {
            leafTiles.addAll(this.topLeft.getLeafTiles());
            leafTiles.addAll(this.topRight.getLeafTiles());
            leafTiles.addAll(this.bottomLeft.getLeafTiles());
            leafTiles.addAll(this.bottomRight.getLeafTiles());
        }
        return leafTiles;
    }

    @Override
    public List getOverlappedLeafTiles(Query query) {
        Rectangle rect = query.getRect();
        List leafTiles = new ArrayList();

        if (this.topLeft == null) {
            leafTiles.add(this);
        } else {
            if (rect.intersects(this.topRight.bounds))
                leafTiles.addAll(this.topRight.getOverlappedLeafTiles(query));
            if (rect.intersects(this.bottomRight.bounds))
                leafTiles.addAll(this.bottomRight.getOverlappedLeafTiles(query));
            if (rect.intersects(this.bottomLeft.bounds))
                leafTiles.addAll(this.bottomLeft.getOverlappedLeafTiles(query));
            if (rect.intersects(this.topLeft.bounds))
                leafTiles.addAll(this.topLeft.getOverlappedLeafTiles(query));
        }
        return leafTiles;
    }

    @Override
    public void split() {
        Range<Float> xRange = this.bounds.getXRange();
        float xMiddle = (xRange.upperEndpoint() + xRange.lowerEndpoint()) / 2f;
        Range<Float> yRange = this.bounds.getYRange();
        float yMiddle = (yRange.upperEndpoint() + yRange.lowerEndpoint()) / 2f;
        Range rangeLeft = Range.range(xRange.lowerEndpoint(), xRange.lowerBoundType(),
                xMiddle, BoundType.CLOSED);
        Range rangeRight = Range.range(xMiddle, BoundType.OPEN, xRange.upperEndpoint(), xRange.upperBoundType());
        Range rangeBottom = Range.range(yRange.lowerEndpoint(), yRange.lowerBoundType(),
                yMiddle, BoundType.CLOSED);
        Range rangeTop = Range.range(yMiddle, BoundType.OPEN, yRange.upperEndpoint(), yRange.upperBoundType());
        this.topLeft = new QuadTreeTile(new Rectangle(rangeLeft, rangeTop));
        this.topLeft.setCategoricalColumns(this.getCategoricalColumns());
        this.topRight = new QuadTreeTile(new Rectangle(rangeRight, rangeTop));
        this.topRight.setCategoricalColumns(this.getCategoricalColumns());
        this.bottomLeft = new QuadTreeTile(new Rectangle(rangeLeft, rangeBottom));
        this.bottomLeft.setCategoricalColumns(this.getCategoricalColumns());
        this.bottomRight = new QuadTreeTile(new Rectangle(rangeRight, rangeBottom));
        this.bottomRight.setCategoricalColumns(this.getCategoricalColumns());

        this.reAddPoints(root, new Stack<>());
        this.root = null;

    }

    @Override
    public Tile getLeafTile(Point point) {
        if (this.topLeft == null) {
            return this;
        } else {
            boolean left = point.getX() <= this.bottomLeft.bounds.getXRange().upperEndpoint();
            boolean bottom = point.getY() <= this.bottomLeft.bounds.getYRange().upperEndpoint();
            QuadTreeTile tmp;
            if (left) {
                if (bottom) {
                    tmp = this.bottomLeft;
                } else {
                    tmp = this.topLeft;
                }
            } else {
                if (bottom) {
                    tmp = this.bottomRight;
                } else {
                    tmp = this.topRight;
                }
            }
            return tmp;
        }
    }

    @Override
    public int getLeafTileCount() {
        if (topLeft == null) {
            return 1;
        }
        return topLeft.getLeafTileCount() + topRight.getLeafTileCount()
                + bottomLeft.getLeafTileCount() + bottomRight.getLeafTileCount();
    }

    @Override
    public TreeNode addPoint(Point point, String[] row) {
        if (topLeft != null) {
            return getLeafTile(point).addPoint(point, row);
        }
        return super.addPoint(point, row);
    }

    @Override
    public TreeNode addPoint(Point point, Stack<Short> labels) {
        if (topLeft != null) {
            return getLeafTile(point).addPoint(point, labels);
        }
        return super.addPoint(point, labels);
    }

    @Override
    public int getMaxDepth() {
        if (topLeft == null) {
            return 0;
        }
        int depth = 0;
        depth = Integer.max(depth, topLeft.getMaxDepth() + 1);
        depth = Integer.max(depth, topRight.getMaxDepth() + 1);
        depth = Integer.max(depth, bottomLeft.getMaxDepth() + 1);
        depth = Integer.max(depth, bottomRight.getMaxDepth() + 1);
        return depth;
    }
}
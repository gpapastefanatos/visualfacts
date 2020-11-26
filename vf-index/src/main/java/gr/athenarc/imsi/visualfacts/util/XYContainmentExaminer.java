package gr.athenarc.imsi.visualfacts.util;


import com.google.common.collect.Range;
import gr.athenarc.imsi.visualfacts.Point;

public class XYContainmentExaminer implements ContainmentExaminer {

    private Range<Float> xRange;
    private Range<Float> yRange;

    public XYContainmentExaminer(Range<Float> xRange, Range<Float> yRange) {
        this.xRange = xRange;
        this.yRange = yRange;
    }

    @Override
    public boolean contains(Point point) {
        return xRange.contains(point.getX()) && yRange.contains(point.getY());
    }
}

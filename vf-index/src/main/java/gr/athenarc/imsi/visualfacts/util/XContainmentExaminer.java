package gr.athenarc.imsi.visualfacts.util;


import com.google.common.collect.Range;
import gr.athenarc.imsi.visualfacts.Point;

public class XContainmentExaminer implements ContainmentExaminer {

    private Range<Float> xRange;

    public XContainmentExaminer(Range<Float> xRange) {
        this.xRange = xRange;
    }

    @Override
    public boolean contains(Point point) {
        return xRange.contains(point.getX());
    }
}

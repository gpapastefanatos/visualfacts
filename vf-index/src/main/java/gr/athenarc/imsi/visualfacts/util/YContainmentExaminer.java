package gr.athenarc.imsi.visualfacts.util;

import com.google.common.collect.Range;
import gr.athenarc.imsi.visualfacts.Point;

public class YContainmentExaminer implements ContainmentExaminer {

    private Range<Float> yRange;

    public YContainmentExaminer(Range<Float> yRange) {
        this.yRange = yRange;
    }

    @Override
    public boolean contains(Point point) {
        return yRange.contains(point.getY());
    }
}

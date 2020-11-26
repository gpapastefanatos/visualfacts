package gr.athenarc.imsi.visualfacts;

import com.google.common.collect.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Rectangle implements Serializable {

    private final Range<Float> xRange;
    private final Range<Float> yRange;

    public Rectangle(Range<Float> xRange, Range<Float> yRange) {
        this.xRange = xRange;
        this.yRange = yRange;
    }

    public Range<Float> getXRange() {
        return xRange;
    }

    public Range<Float> getYRange() {
        return yRange;
    }

    public boolean contains(float x, float y) {
        return xRange.contains(x) && yRange.contains(y);
    }

    public boolean contains(Point point) {
        return contains(point.getX(), point.getY());
    }

    public boolean intersects(Rectangle other) {
        return this.xRange.isConnected(other.getXRange()) && !this.xRange.intersection(other.getXRange()).isEmpty()
                && this.yRange.isConnected(other.getYRange()) && !this.yRange.intersection(other.getYRange()).isEmpty();
    }

    public boolean encloses(Rectangle other) {
        return this.xRange.encloses(other.getXRange()) && this.yRange.encloses(other.getYRange());
    }

    public double getCenterX() {
        return (xRange.lowerEndpoint() + xRange.upperEndpoint())/2d;
    }

    public double getCenterY() {
        return (yRange.lowerEndpoint() + yRange.upperEndpoint())/2d;
    }

    public float getXSize() {
        return xRange.upperEndpoint() - xRange.lowerEndpoint();
    }

    public float getYSize() {
        return yRange.upperEndpoint() - yRange.lowerEndpoint();
    }

    public List toList() {
        List<Range<Float>> list = new ArrayList<>(2);
        list.add(this.xRange);
        list.add(this.yRange);
        return list;
    }

    public double distanceFrom(Rectangle other){
        double centerX = (xRange.lowerEndpoint() + xRange.upperEndpoint()) / 2d;
        double centerY = (yRange.lowerEndpoint() + yRange.upperEndpoint()) / 2d;
        double otherCenterX = (other.xRange.lowerEndpoint() + other.xRange.upperEndpoint()) / 2d;
        double otherCenterY = (other.yRange.lowerEndpoint() + other.yRange.upperEndpoint()) / 2d;
        return Math.hypot(Math.abs(centerX - otherCenterX), Math.abs(centerY - otherCenterY));
    }

    public double getSurfaceArea(){
        return getXSize() * getYSize();
    }


    @Override
    public String toString() {
        return xRange.toString() + "," + yRange.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rectangle rectangle = (Rectangle) o;
        return Objects.equals(xRange, rectangle.xRange) &&
                Objects.equals(yRange, rectangle.yRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xRange, yRange);
    }
}

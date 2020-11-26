package gr.athenarc.imsi.visualfacts;

import java.io.Serializable;

/**
 * Class that represents a 2d data point containing an x and y value
 */
public class Point implements Serializable {

    private float x;
    private float y;
    private long fileOffset;

    public Point(float x, float y, long fileOffset) {
        this.x = x;
        this.y = y;
        this.fileOffset = fileOffset;
    }

    public long getFileOffset() {
        return fileOffset;
    }

    public void setFileOffset(long fileOffset) {
        this.fileOffset = fileOffset;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", fileOffset=" + fileOffset +
                '}';
    }
}

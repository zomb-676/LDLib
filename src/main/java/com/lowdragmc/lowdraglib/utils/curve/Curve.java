package com.lowdragmc.lowdraglib.utils.curve;

import com.lowdragmc.lowdraglib.utils.Vector3;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/6/16
 * @implNote Curve
 */
public abstract class Curve {

    private final int division;
    private final boolean cache;
    protected boolean needUpdate = true;
    private DoubleList lengths;

    public Curve(int lengthDivision) {
        this.division = lengthDivision;
        this.cache = true;
    }

    public Curve(int lengthDivision, boolean cache) {
        this.division = lengthDivision;
        this.cache = cache;
    }

    /**
     * @param t parameter range in [0,1] according to percent
     */
    public abstract Vector3 getPoint(float t);

    /**
     * @param u parameter range in [0,1] according to curve length
     */
    public Vector3 getPointAt(float u) {
        var t = this.getUfromT(u);
        return this.getPoint(t);
    }


    /**
     * @return based on t, the distance between each point may be different
     */
    List<Vector3> getPointsAt() {
        List<Vector3> points = new ArrayList<>(division);
        for (int i = 0; i < division; i++) {
            points.add(getPointAt(i * 1f / division));
        }
        return points;
    }

    /**
     * @return based on u, the distance between each point will be close
     */
    List<Vector3> getPoints() {
        List<Vector3> points = new ArrayList<>(division);
        for (int i = 0; i < division; i++) {
            points.add(getPoint(i * 1f / division));
        }
        return points;
    }

    /**
     * @return store the distance from the first point to each based on t
     */
    public DoubleList getLengths() {
        if (!needUpdate) {
            return lengths;
        }
        needUpdate = false;

        if (lengths == null) {
            lengths = new DoubleArrayList(division);
        }

        //first point
        lengths.add(0);
        var sum = 0;
        var lastPoint = this.getPoint(0);

        //other point
        for (int i = 1; i <= division; i++) {
            var currentPoint = this.getPoint(i * 1.0f / division);
            var distance = currentPoint.distanceTo(lastPoint);
            sum += distance;
            lengths.add(sum);
        }

        return lengths;
    }

    abstract float getUfromT(float u);

    double getArcLength() {
        var lengths = this.getLengths();
        return lengths.getDouble(lengths.size() - 1);
    }

    double getUtoT(float u) {
        var lengths = this.getLengths();
        var targetLength = u * getArcLength();
        return ordinarySearchWithInterpolation(lengths, targetLength);
    }

    protected double binarySearchWithInterpolation(DoubleList list, double target) {
        int left = -1;
        int right = list.size();
        do {
            int checkIndex = (left + right) / 2; //floor
            if (list.getDouble(checkIndex) <= target) {
                left = checkIndex;
            } else {
                right = checkIndex;
            }
        } while (left + 1 != right);

        double leftValue = list.getDouble(left);
        double rightValue = list.getDouble(right);

        double percent = (target - leftValue) / (rightValue - leftValue);

        return (left + percent) / division;
    }

    protected double ordinarySearchWithInterpolation(DoubleList list, double target) {
        int index = 0;
        while (true) {
            var nextLength = list.getDouble(index);

            if (nextLength > target) break;

            index++;

            if (index > list.size()) break;
        }

        double rightValue = list.getDouble(index);
        double leftValue = list.getDouble(index - 1);

        double percent = (target - leftValue) / (rightValue - leftValue);

        return (index - 1 + percent) / division;

    }

    public Vector3 getTangent(float t) {
        var t1 = t + 0.001f;
        var t2 = t - 0.001f;

        if (t1 < 0) t1 = 0;
        if (t2 > 1) t2 = 1;

        var p1 = this.getPoint(t1);
        var p2 = this.getPoint(t2);

        var tangent = p2.copy().subtract(p1).normalize();

        return tangent;
    }

    public Vector3 getTangentAt(float u) {
        var t = this.getUfromT(u);
        return this.getTangent(t);
    }




}

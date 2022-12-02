package com.lowdragmc.lowdraglib.utils.curve;

import com.lowdragmc.lowdraglib.utils.Quat;
import com.lowdragmc.lowdraglib.utils.Vector3;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2022/6/16
 * @implNote Curve
 */
public abstract class Curve {

    private static double EPSILON = 1.0E-7D;

    protected final int division;

    public Curve(int division) {
        this.division = division;
    }

    /**
     * @param t parameter in [0,1]<br>
     *          may not be uniformly distributed
     */
    public abstract Vector3 getPoint(float t);

    public List<Vector3> getPoints() {
        List<Vector3> points = new ArrayList<>();
        for (int i = 0; i < division; i++) {
            points.add(getPoint(((float) i) / division));
        }
        return points;
    }

    /**
     * @param u parameter in [0,1]<br>
     *          should be uniformly distributed
     */
    public Vector3 getPointAt(float u) {
        return this.getPoint(this.mapU2t(u));
    }

    public List<Vector3> getPointsAt() {
        List<Vector3> points = new ArrayList<>();
        for (int i = 0; i < division; i++) {
            points.add(getPointAt(((float) i) / division));
        }
        return points;
    }

    /**
     * @return the length from begin to each division point based on t
     */
    public FloatList calculateLength() {
        var lengths = new FloatArrayList();
        var current = this.getPoint(0);
        var last = current.copy();

        float sum = 0;

        lengths.push(0);

        for (int i = 1; i <= division; i++) {
            current = this.getPoint(((float) i) / division);
            sum += current.distanceTo(last);
            lengths.push(sum);
            last = current;
        }

        return lengths;

    }

    public float curveLength() {
        FloatList lengths = this.calculateLength();
        return lengths.getFloat(lengths.size() - 1);
    }


    public float mapU2t(float u) {
        var lengths = this.calculateLength();

        return 0;
        //TODO

    }

    public Vector3 getTangent(float t) {
        final double delta = 0.0001;

        final var p1 = this.getPoint((float) Math.max(0, t - delta));
        final var p2 = this.getPoint((float) Math.min(1, t + delta));

        return p2.subtract(p1).normalize();
    }

    public Vector3 getTangentAt(float u) {
        return this.getTangent(this.mapU2t(u));
    }

    public void calculateFrenetFrames(int segDivision) {

        Vector3[] tangents = new Vector3[segDivision];
        Vector3[] normals = new Vector3[segDivision];
        Vector3[] binormals = new Vector3[segDivision];
        for (int i = 0; i < segDivision; i++) {
            float u = ((float) i) / segDivision;
            tangents[i] = this.getTangentAt(u);
        }

        Vector3 normal = new Vector3(0, 0, 0);
        {
            final var tx = Math.abs(tangents[0].x);
            final var ty = Math.abs(tangents[0].y);
            final var tz = Math.abs(tangents[0].z);

            var min = Double.MAX_VALUE;

            if (tx <= min) {
                min = tx;
                normal.set(1, 0, 0);
            }
            if (ty <= min) {
                min = ty;
                normal.set(0, 1, 0);
            }
            if (tz <= min) {
                normal.set(0, 0, 1);
            }
        }

        var vec = tangents[0].copy().crossProduct(normal).normalize();
        normals[0] = tangents[0].copy().crossProduct(vec);
        binormals[0] = tangents[0].copy().crossProduct(normals[0]);

        for (int i = 1; i <= segDivision; i++) {
            normals[i] = normals[i - 1].copy();
            binormals[i] = binormals[i - 1].copy();

            vec.set(tangents[i-1]).crossProduct(tangents[i]);

            if (vec.magSquared() > EPSILON) {
                vec.normalize();

                var theta = Math.acos(Mth.clamp(tangents[i-1].copy().dotProduct(tangents[i]),-1,1));
                //TODO
            }

            binormals[i] = tangents[i].copy().crossProduct(normals[i]);
        }


    }

}

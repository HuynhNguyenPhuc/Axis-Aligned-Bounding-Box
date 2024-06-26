package com.example.aabb;

import java.util.Arrays;

class BVHNode {
    public AABB aabb;
    public int id;
    public BVHNode left;
    public BVHNode right;

    public BVHNode(AABB aabb, int id) {
        this.aabb = aabb;
        this.id = id;
        this.left = null;
        this.right = null;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }
}


public class BVH {
    /*
     * This class represents a Bounding Volume Hierarchy
     */
    public BVHNode root;
    public int numAABBs;

    public BVH() {
        this.root = null;
        this.numAABBs = 0;
    }

    public BVH(BVH other) {
        this.root = other.root;
        this.numAABBs = other.numAABBs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100000);
        toString(sb, root, 0);
        return sb.toString();
    }

    private void toString(StringBuilder sb, BVHNode node, int depth) {
        if (node == null) {
            return;
        }
        char[] indent = new char[depth * 2];
        Arrays.fill(indent, ' ');
        sb.append(indent);
        if (node.isLeaf()) {
            sb.append("Leaf Node - ").append(node.aabb.toString()).append("\n");
        } else {
            sb.append("Internal Node - ").append(node.aabb.toString()).append("\n");
            toString(sb, node.left, depth + 1);
            toString(sb, node.right, depth + 1);
        }
    }

    class Pair{
        public AABB aabb;
        public int id;
        public Pair(AABB aabb, int id){
            this.aabb = aabb;
            this.id = id;
        }
    }

    private void sortAABBs(AABB[] aabbs, int start, int end, int axis, int[] indices) {
        Pair[] pairs = new Pair[end - start];
        for (int i = start; i < end; i++) {
            pairs[i - start] = new Pair(aabbs[i], indices[i]);
        }

        Arrays.sort(pairs, (p1, p2) -> Float.compare(p1.aabb.getCenter().getCoordinates()[axis], p2.aabb.getCenter().getCoordinates()[axis]));

        for (int i = start; i < end; i++) {
            aabbs[i] = pairs[i - start].aabb;
            indices[i] = pairs[i - start].id;
        }
    }

    public void build(AABB[] aabbs){
        int[] indices = new int[aabbs.length];
        for (int i = 0; i < aabbs.length; i++) {
            indices[i] = i;
        }
        root = buildRecursive(aabbs, 0, aabbs.length, indices);
    }

    private BVHNode buildRecursive(AABB[] aabbs, int start, int end, int[] indices){
        if (start >= end) return null;
        if (end - start == 1) return new BVHNode(aabbs[start], indices[start]);

        /* Expand the bounding volume of the AABBs */
        AABB boundingVolume = AABB.expand(aabbs, 0, aabbs.length);

        /* Get the longest extent axis to split */
        int axis = boundingVolume.getLongestAxis();

        /* Sort the AABBs based on the longest extent axis */
        sortAABBs(aabbs, start, end, axis, indices);

        /* Median split strategy */
        int splitIndex = strategy("sah", aabbs, start, end);

        BVHNode leftChild = buildRecursive(aabbs, start, splitIndex, indices);
        BVHNode rightChild = buildRecursive(aabbs, splitIndex, end, indices);

        BVHNode node = new BVHNode(boundingVolume, -1);
        node.left = leftChild;
        node.right = rightChild;

        return node;
    }

    private int strategy(String mode, Object... agrs){
        switch(mode){
            case "median":
                return medianStrategy((int) agrs[0], (int) agrs[1]);
            case "sah":
                return sahStrategy((AABB[]) agrs[0], (int) agrs[1], (int) agrs[2]);
            default:
                return 0;
        }
    }

    private int medianStrategy(int start, int end){
        return start + (end - start) / 2;
    }

    private int sahStrategy(AABB[] aabbs, int start, int end){
        int n = end - start;
        float[] sahCosts = new float[n - 1];
        AABB combinedAABB = AABB.expand(aabbs, start, end);

        float totalSurfaceArea = combinedAABB.getSurfaceArea();

        for (int i = start + 1; i < end; i++) {
            AABB leftAABB = AABB.expand(aabbs, start, i);
            AABB rightAABB = AABB.expand(aabbs, i, end);
            float leftSurfaceArea = leftAABB.getSurfaceArea();
            float rightSurfaceArea = rightAABB.getSurfaceArea();
            sahCosts[i - start - 1] = (i - start) * (leftSurfaceArea / totalSurfaceArea) +
                    (end - i) * (rightSurfaceArea / totalSurfaceArea);
        }

        float minCost = Float.POSITIVE_INFINITY;
        int minIndex = start + 1;
        for (int i = start + 1; i < end; i++) {
            if (sahCosts[i - start - 1] < minCost) {
                minCost = sahCosts[i - start - 1];
                minIndex = i;
            }
        }

        return minIndex;
    }

    public void insert(AABB aabb, int id) {
        numAABBs++;
        if (root == null) {
            root = new BVHNode(aabb, id);
        } else {
            insertRecursive(root, aabb, id);
        }
    }

    private void insertRecursive(BVHNode node, AABB aabb, int id) {
        if (node.isLeaf()) {
            node.left = new BVHNode(node.aabb, node.id);
            node.right = new BVHNode(aabb, id);
            node.aabb = node.aabb.expand(aabb);
            node.id = -1;
        } else {
            float leftVolumeIncrease = node.left.aabb.expand(aabb).getSurfaceArea() - node.left.aabb.getSurfaceArea();
            float rightVolumeIncrease = node.right.aabb.expand(aabb).getSurfaceArea() - node.right.aabb.getSurfaceArea();

            if (leftVolumeIncrease < rightVolumeIncrease){
                insertRecursive(node.left, aabb, id);
                node.aabb = node.aabb.expand(node.left.aabb);
            } else {
                insertRecursive(node.right, aabb, id);
                node.aabb = node.aabb.expand(node.right.aabb);
            }
        }
    }

    public boolean[] checkIntersectWithRay(Ray r) {
        boolean[] intersections = new boolean[numAABBs];
        checkIntersectWithRayRecursive(root, r, intersections);
        return intersections;
    }

    private void checkIntersectWithRayRecursive(BVHNode node, Ray r, boolean[] intersections) {
        if (node == null) return;
        if (node.aabb.checkIntersectWithRay(r)) {
            if (node.isLeaf()) {
                intersections[node.id] = true;
            } else {
                checkIntersectWithRayRecursive(node.left, r, intersections);
                checkIntersectWithRayRecursive(node.right, r, intersections);
            }
        }
    }

    class ArrayWrapper{
        public int[] array;
        public ArrayWrapper(int initialSize){
            array = new int[initialSize];
        }
    }

    public int[] getIntersectWithRay(Ray r, int initialSize) {
        ArrayWrapper arrayWrapper = new ArrayWrapper(initialSize);
        int count = getIntersectWithRayRecursive(root, r, arrayWrapper, 0);
        int[] result = Arrays.copyOf(arrayWrapper.array, count);
        return result;
    }

    private int getIntersectWithRayRecursive(BVHNode node, Ray r, ArrayWrapper arrayWrapper, int index) {
        if (node == null) return index;
        if (node.aabb.checkIntersectWithRay(r)) {
            if (node.isLeaf()) {
                if (index >= arrayWrapper.array.length) {
                    arrayWrapper.array = Arrays.copyOf(arrayWrapper.array, 2 * index);
                }
                arrayWrapper.array[index++] = node.id;
            } else {
                index = getIntersectWithRayRecursive(node.left, r, arrayWrapper, index);
                index = getIntersectWithRayRecursive(node.right, r, arrayWrapper, index);
            }
        }
        return index;
    }
}

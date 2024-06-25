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

    public int[] getIntersectWithRay(Ray r, int initialSize) {
        int[] intersections = new int[initialSize];
        int count = getIntersectWithRayRecursive(root, r, intersections, 0);
        return Arrays.copyOf(intersections, count);
    }

    private int getIntersectWithRayRecursive(BVHNode node, Ray r, int[] intersections, int index) {
        if (node == null) return index;
        if (node.aabb.checkIntersectWithRay(r)) {
            if (node.isLeaf()) {
                intersections[index++] = node.id;
            } else {
                index = getIntersectWithRayRecursive(node.left, r, intersections, index);
                index = getIntersectWithRayRecursive(node.right, r, intersections, index);
            }
        }
        return index;
    }
}

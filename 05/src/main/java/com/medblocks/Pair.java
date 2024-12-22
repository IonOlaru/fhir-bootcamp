package com.medblocks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Pair<L, R> {

    private final L left;
    private final R right;

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public static <L, R> Pair<L, R> ofNulls() {
        return new Pair<>(null, null);
    }

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @JsonCreator
    public static <L, R> Pair<L, R> beanCreator(@JsonProperty("left") L left, @JsonProperty("right") R right) {
        return Pair.of(left, right);
    }

    public L getLeft() {
        return left;
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean noLeft() {
        return left == null;
    }

    public R getRight() {
        return right;
    }

    public boolean hasRight() {
        return right != null;
    }

    public boolean noRight() {
        return right == null;
    }

    public boolean equalPair() {
        return left != null && left.equals(right);
    }

    public boolean isNull() {
        return left == null && right == null;
    }

    @Override
    public String toString() {
        return "<" + left + ", " + right + '>';
    }
}
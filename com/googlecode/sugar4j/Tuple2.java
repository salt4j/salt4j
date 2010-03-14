package com.googlecode.sugar4j;

/**
 * @author Seun Osewa
 */
public class Tuple2<E, F> {
    public final E _1;
    public final F _2;
    public Tuple2(E first, F second) { this._1 = first; this._2 = second; }
    public String toString() { return "(" + _1 + ", " + _2 + ")"; }
}

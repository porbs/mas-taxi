package com.mas.lab2.Util;

import java.io.Serializable;

public class Point<T extends Number> implements Serializable {
    T x;
    T y;

    public Point(T x, T y) {
        this.x = x;
        this.y = y;
    }
}

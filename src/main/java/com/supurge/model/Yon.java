package com.supurge.model;

/**
 * Robotun hareket yönlerini tanımlayan enum.
 */
public enum Yon {
    KUZEY(0, -1),
    GUNEY(0, 1),
    DOGU(1, 0),
    BATI(-1, 0);

    private final int dx;
    private final int dy;

    Yon(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() { return dx; }
    public int getDy() { return dy; }
}

package com.example.vladimir.snake;

import android.graphics.Point;

class Mouse {
    private Point point1;
    private Point point2;
    private Point point3;
    private Point point4;
    private Boolean isBig;
    private int[] mouseXs;
    private int[] mouseYs;
    public final int RANDOM_SPAWN_NUMBER = 5;

    public Mouse(Point point) {
        point1 = new Point(point);
        isBig = false;
    }

    public void generateBigMouse() {
        point2 = new Point(point1.x + 1, point1.y);
        point3 = new Point(point1.x + 1, point1.y - 1);
        point4 = new Point(point1.x, point1.y - 1);
        isBig = true;
    }

    public Boolean isBitten(Point snakeXY) {
        if (isBig() && (point1.equals(snakeXY) ||
                        point2.equals(snakeXY) ||
                        point3.equals(snakeXY) ||
                        point4.equals(snakeXY))) {
            return true;
        } else {
            return point1.equals(snakeXY);
        }
    }

    public Boolean isBig() {
        return isBig;
    }

    public int[] getMouseXs() {
        if (isBig()) {
            mouseXs = new int[4];
            mouseXs[0] = point1.x;
            mouseXs[1] = point2.x;
            mouseXs[2] = point3.x;
            mouseXs[3] = point4.x;
        } else {
            mouseXs = new int[1];
            mouseXs[0] = point1.x;
        }
        return mouseXs;
    }

    public int[] getMouseYs() {
        if (isBig()) {
            mouseYs = new int[4];
            mouseYs[0] = point1.y;
            mouseYs[1] = point2.y;
            mouseYs[2] = point3.y;
            mouseYs[3] = point4.y;
        } else {
            mouseYs = new int[1];
            mouseYs[0] = point1.y;
        }
        return mouseYs;
    }
}

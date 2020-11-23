package model;

public class Checker {
    public Color color;
    public double oldX;
    public double oldY;
    public int moveType;
    public boolean isDamka;

    public Checker(int x, int y, Color color, int moveType, boolean isDamka) {
        this.color = color;
        this.moveType = moveType;
        this.isDamka = isDamka;
        go(x, y);
    }

    public void go(int newX, int newY) {
        oldX = (newX + 0.1) * 100;
        oldY = (newY + 0.1) * 100;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }
}
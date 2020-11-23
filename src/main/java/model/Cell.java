package model;

public class Cell {
    public int x;
    public int y;
    public Color color;
    public Checker checker;

    public Cell(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void setChecker(Checker checker) {
        this.checker = checker;
    }

    public Checker getChecker() {
        return checker;
    }

    public boolean hasChecker() {
        return getChecker() != null;
    }
}
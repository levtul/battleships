package battleships.ship;

import battleships.cell.Cell;

import java.util.Locale;

public class Ship {

    public ShipType type;
    public Orientation orientation;
    public Cell[] cells;
    public boolean sunk = false;

    public Ship(ShipType type, Orientation orientation) {
        this.type = type;
        this.orientation = orientation;
        cells = new Cell[type.ordinal() + 1];
    }

    /**
     * method that checks if ship cannot be placed near other
     *
     * @param other - other ship
     */
    public boolean hasSameBorder(Ship other) {
        Rectangle r1 = new Rectangle(this), r2 = new Rectangle(other);
        return r1.hasSameBorder(r2);
    }

    /**
     * method that returns coords of ship
     */
    public int[] getCoords() {
        Rectangle r1 = new Rectangle(this);
        return new int[]{r1.x1, r1.y1, r1.x2, r1.y2};
    }

    String name() {
        return type.name();
    }

    /**
     * method that checks is ship is sunk and shows message if it is
     */
    public void checkSunk() {
        boolean hasUnspotted = false;
        for (Cell cell : cells) {
            if (!cell.spotted) {
                hasUnspotted = true;
                break;
            }
        }
        if (!hasUnspotted) {
            sink();
        }
    }

    void sink() {
        sunk = true;
        System.out.printf("You have just sunk a %s.\n", this.name().toLowerCase(Locale.ROOT));
    }

    /**
     * method that checks is ship is sunk
     */
    public boolean isSunk() {
        return sunk;
    }

    /**
     * method that makes ship cells unspotted
     */
    public void recover() {
        for (Cell cell : cells) {
            cell.spotted = false;
        }
    }

    /**
     * method that marks cells as spotted and shows ship type message
     */
    public void destroy() {
        for (Cell cell : cells) {
            cell.spotted = true;
        }
        sink();
    }

    /**
     * method that marks cells as spotted and does not show ship type message
     */
    public void destroyWithoutMessage() {
        for (Cell cell : cells) {
            cell.spotted = true;
        }
        sunk = true;
    }
}

class Rectangle {

    // координаты верхнего левого угла
    final int x1, y1;
    // координаты правого нижнего угла
    final int x2, y2;

    Rectangle(Ship ship) {
        x1 = ship.cells[0].x;
        y1 = ship.cells[0].y;
        if (ship.orientation == Orientation.HORIZONTAL) {
            x2 = x1;
            y2 = y1 + ship.type.ordinal();
        } else {
            x2 = x1 + ship.type.ordinal();
            y2 = y1;
        }
    }

    boolean hasSameBorder(Rectangle other) {
        return !(other.x1 - 1 > this.x2 || this.x1 > other.x2 + 1 ||
                other.y1 - 1 > this.y2 || this.y1 > other.y2 + 1);
    }
}

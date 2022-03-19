package battleships.cell;

import battleships.ship.Ship;

public class ShipCell extends Cell {
    public Ship ship;

    public ShipCell(int x, int y, Ship ship) {
        super(x, y);
        this.ship = ship;
    }


    /**
     * method that makes cell spotted and checks if ship is sunk
     */
    public void shot() {
        if (ship.sunk) {
            System.out.println("You're trying to hit sunk ship.");
            return;
        }
        System.out.println("It's a hit");
        spotted = true;
        ship.checkSunk();
    }

    /**
     * method that destroys ship and makes cells spotted
     */
    public void shotTorpedo() {
        ship.destroy();
        spotted = true;
    }
}

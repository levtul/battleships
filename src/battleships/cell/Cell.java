package battleships.cell;

public class Cell {

  public boolean spotted;
  public Integer x;
  public Integer y;

  public Cell(int x, int y) {
    spotted = false;
    this.x = x;
    this.y = y;
  }

  /**
   * method that makes cell spotted
   */
  public void shot() {
    System.out.println("It's a miss.");
    spotted = true;
  }


  /**
   * method that makes cell spotted by torpedo
   */
  public void shotTorpedo() {
    spotted = true;
  }
}


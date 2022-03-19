package battleships.manager;

import battleships.cell.Cell;
import battleships.cell.EmptyCell;
import battleships.cell.ShipCell;
import battleships.ship.Ship;
import battleships.ship.Orientation;
import battleships.ship.ShipType;
import java.util.Random;
import java.util.Scanner;

public class Manager {

  static final Scanner reader = new Scanner(System.in);
  static final Random random = new Random();
  Cell[][] field;
  int[] shipsCount = new int[5];
  boolean torpedoMode = false;
  boolean shipRecoveryMode = false;
  int n, m;
  int torpedoShotsRemaining = 0;
  int shotsCount = 0, torpedoShotsCount = 0;
  Ship lastShottedShip = null;

  /**
   * method that starts game
   */
  public void start() {
    while (true) {
      try {
        if (initialize()) {
          break;
        }
      } catch (ExitAppException e) {
        System.out.println("Quitting the game.");
        return;
      } catch (PlacingErrorException ignored) {
      }
    }
    play();
  }

  /**
   * method that starts game from cmd args
   */
  public void start(String[] args) {
    while (true) {
      try {
        if (initialize(args)) {
          break;
        }
      } catch (ExitAppException e) {
        System.out.println("Quitting the game.");
        return;
      } catch (PlacingErrorException e) {
        continue;
      }
      System.out.println("Wrong arguments, can't start the game, stopping.");
      return;
    }
    play();
  }

  boolean initialize() throws ExitAppException, PlacingErrorException {
    System.out.println("Starting Battleships game!");
    getSizes();
    field = new Cell[n][m];
    getShipsCount();
    if (!placeShips()) {
      return false;
    }
    getTorpedoShots();
    getGameMode();
    fillField();
    return true;
  }

  boolean initialize(String[] args) throws ExitAppException, PlacingErrorException {
    System.out.println("Starting Battleships game!");
    if (!getSizes(args)) {
      return false;
    }
    field = new Cell[n][m];
    if (!getShipsCount(args)) {
      return false;
    }
    if (!placeShips()) {
      return false;
    }
    if (!getTorpedoShots(args)) {
      return false;
    }
    if (!getGameMode(args)) {
      return false;
    }
    fillField();
    return true;
  }

  void getSizes() {
    System.out.println("Enter field sizes, two integers separated by space or newline:");
    try {
      n = reader.nextInt();
      m = reader.nextInt();
      if (n > 100 || m > 100) {
        System.out.println("Field size cannot be greater than 100.");
        getSizes();
      }
    } catch (Exception e) {
      System.out.println("Sorry, can't read sizes, try again.");
      reader.nextLine();
      getSizes();
    }
  }

  boolean getSizes(String[] args) {
    try {
      n = Integer.parseInt(args[0]);
      m = Integer.parseInt(args[1]);
    } catch (Exception e) {
      System.out.println("Incorrect field sizes.");
      return false;
    }
    return true;
  }

  void getShipsCount() {
    System.out.println("Enter five integers in the order,"
        + " which corresponds to ship types sizes, the smallest first:");
    try {
      for (int i = 0; i < 5; i++) {
        shipsCount[i] = reader.nextInt();
      }
    } catch (Exception e) {
      System.out.println("Sorry, can't read ships count, try again.");
      reader.nextLine();
      getShipsCount();
    }
  }

  boolean getShipsCount(String[] args) {
    try {
      for (int i = 2; i < 7; i++) {
        shipsCount[i - 2] = Integer.parseInt(args[i]);
      }
    } catch (Exception e) {
      System.out.println("Incorrect ships count.");
      return false;
    }
    return true;
  }

  void getTorpedoShots() {
    System.out.println("Enter torpedoes count, or zero, if you want to turn off torpedo mode:");
    try {
      torpedoShotsRemaining = reader.nextInt();
      if (torpedoShotsRemaining > 0) {
        torpedoMode = true;
      }
    } catch (Exception e) {
      System.out.println("Sorry, can't torpedoes count, try again.");
      reader.nextLine();
      getTorpedoShots();
    }
  }

  boolean getTorpedoShots(String[] args) {
    try {
      torpedoShotsRemaining = Integer.parseInt(args[7]);
      if (torpedoShotsRemaining < 0) {
        throw new Exception();
      }
      torpedoMode = torpedoShotsRemaining > 0;
    } catch (Exception e) {
      System.out.println("Incorrect torpedoes count.");
      return false;
    }
    return true;
  }

  void getGameMode() {
    System.out.println("Do you want to turn ship recovery mode? Answer y or n");
    shipRecoveryMode = readYesNo();
  }

  boolean getGameMode(String[] args) {
    try {
      if (args[8].equals("y")) {
        shipRecoveryMode = true;
        return true;
      } else if (args[8].equals("n")) {
        shipRecoveryMode = false;
      } else {
        throw new Exception();
      }
    } catch (Exception e) {
      System.out.println("Incorrect game mode");
      return false;
    }
    return false;
  }

  boolean placeShips() throws ExitAppException, PlacingErrorException {
    boolean success = placeShipsRandomly();
    if (!success) {
      System.out.print("""
          Sorry, can't place ships on this type of field.
          Do you want to retry? Answer y or n.
          """);
      boolean retry = readYesNo();
      if (!retry) {
        throw new ExitAppException();
      } else {
        throw new PlacingErrorException();
      }
    }
    return true;
  }

  boolean placeShipsRandomly() {
    boolean success = false;
    int sum = 0;
    for (int count : shipsCount) {
      sum += count;
    }
    Ship[] permutation = new Ship[sum];
    for (int i = 0; i < 10000 && !success; i++) {
      success = tryPlaceShipsRandomly(shipsCount.clone(), permutation);
    }
    if (success) {
      for (Ship ship : permutation) {
        int[] coords = ship.getCoords();
        int index = 0;
        for (int x = coords[0]; x <= coords[2]; x++) {
          for (int y = coords[1]; y <= coords[3]; y++) {
            field[x][y] = new ShipCell(x, y, ship);
            ship.cells[index++] = field[x][y];
          }
        }
      }
    }
    return success;
  }

  boolean tryPlaceShipsRandomly(int[] shipsCount, Ship[] permutation) {
    int generatedCount = 0;
    for (int i = 4; i >= 0; ) {
      if (shipsCount[i] == 0) {
        i--;
        continue;
      }
      boolean success = false;
      for (int count = 0; count < n * m * 4; count++) {
        ShipType type = ShipType.values()[i];
        Orientation orientation = Orientation.values()[random.nextInt(2)];
        Cell cell;
        if (orientation == Orientation.HORIZONTAL) {
          if (m - type.ordinal() < 0) {
            continue;
          }
          cell = new Cell(random.nextInt(n), random.nextInt(m - type.ordinal()));
        } else {
          if (n - type.ordinal() <= 0) {
            continue;
          }
          cell = new Cell(random.nextInt(n - type.ordinal()), random.nextInt(m));
        }
        Ship ship = new Ship(type, orientation);
        ship.cells[0] = cell;
        permutation[generatedCount] = ship;
        generatedCount++;
        if (checkCorrectnessOfPermutation(permutation, generatedCount)) {
          shipsCount[i]--;
          success = true;
          break;
        } else {
          generatedCount--;
        }
      }
      if (!success) {
        return false;
      }
      if (shipsCount[i] == 0) {
        i--;
      }
    }
    return true;
  }

  boolean checkCorrectnessOfPermutation(Ship[] permutation, int generatedCount) {
    Ship added = permutation[generatedCount - 1];
    for (int i = 0; i < generatedCount - 1; i++) {
      if (added.hasSameBorder(permutation[i])) {
        return false;
      }
    }
    return true;
  }

  void fillField() {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        if (field[i][j] == null) {
          field[i][j] = new EmptyCell(i, j);
        }
      }
    }
  }

  void play() {
    System.out.println("Successfully started the game!");
      label:
      while (true) {
        boolean success = false;
        Command command = null;
        while (!success) {
          try {
            command = readCommand();
            success = true;
          } catch (Exception e) {
            System.out.println(e.getMessage());
          }
        }
          switch (command) {
              case QuitCommand quitCommand:
                  break label;
              case ShowFieldCommand cmd:
                  handleCommand(cmd);
                  break;
              case TorpedoCommand cmd:
                  handleCommand(cmd);
                  break;
              case FireCommand cmd:
                  handleCommand(cmd);
                  break;
              case null:
              default:
                  handleCommand(command);
                  break;
          }
        showField();
        boolean hasAlive = false;
        for (int i = 0; i < 5; i++) {
          if (shipsCount[i] > 0) {
            hasAlive = true;
            break;
          }
        }
        if (!hasAlive) {
          System.out.println("Congratulations! You won.");
          break;
        }
      }
      System.out.printf("""
        You have done %d regular shots and %d torpedo shots.
        Goodbye!
        """, shotsCount, torpedoShotsCount);
  }

  Command readCommand() throws Exception {
    showDialog();
    try {
      String commandStr = reader.next();
      if (commandStr.equals("q")) {
        return new QuitCommand();
      } else if (commandStr.equals("f")) {
        int x = reader.nextInt();
        int y = reader.nextInt();
        return new FireCommand(x, y);
      } else if (commandStr.equals("t") && torpedoMode) {
        int x = reader.nextInt();
        int y = reader.nextInt();
        return new TorpedoCommand(x, y);
      }
      throw new Exception();
    } catch (Exception e) {
      reader.nextLine();
      throw new Exception("Wrong command format, try again.");
    }
  }

  boolean readYesNo() {
    try {
      String ans = reader.next();
      if (ans.equals("y") || ans.equals("Y")) {
        return true;
      } else if (ans.equals("n") || ans.equals("N")) {
        return false;
      }
      throw new Exception();
    } catch (Exception e) {
      System.out.println("Can't read answer, so let it be no");
      return false;
    }
  }

  void handleCommand(TorpedoCommand torpedoCommand) {
    if (torpedoCommand.isNotCorrect(n, m)) {
      System.out.println("Torpedo shot is out of bounds.");
      return;
    }
    if (torpedoShotsRemaining == 0) {
      System.out.println("Sorry, you don't have enough torpedoes.");
      return;
    }
    torpedoShotsRemaining--;
    torpedoShotsCount++;
    field[torpedoCommand.x - 1][torpedoCommand.y - 1].shotTorpedo();
    if (field[torpedoCommand.x - 1][torpedoCommand.y - 1] instanceof ShipCell shipCell) {
      shipsCount[shipCell.ship.type.ordinal()]--;
      if (shipRecoveryMode && lastShottedShip != shipCell.ship && lastShottedShip != null) {
        lastShottedShip.recover();
      }
    } else {
      if (shipRecoveryMode && lastShottedShip != null) {
        lastShottedShip.recover();
      }
    }
    lastShottedShip = null;
  }

  void handleCommand(FireCommand fireCommand) {
    if (fireCommand.isNotCorrect(n, m)) {
      System.out.println("Shot is out of bounds.");
      return;
    }
    field[fireCommand.x - 1][fireCommand.y - 1].shot();
    shotsCount++;
    if (field[fireCommand.x - 1][fireCommand.y - 1] instanceof ShipCell shipCell) {
      if (shipCell.ship.isSunk()) {
        shipsCount[shipCell.ship.type.ordinal()]--;
      }
      if (shipRecoveryMode && shipCell.ship != lastShottedShip && lastShottedShip != null) {
        lastShottedShip.recover();
      } else if (!shipCell.ship.isSunk()) {
        lastShottedShip = shipCell.ship;
      }
    } else if (shipRecoveryMode && lastShottedShip != null) {
      lastShottedShip.recover();
    }
  }

  void handleCommand(ShowFieldCommand command) {
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        if (field[i][j] instanceof ShipCell shipCell) {
          shipCell.ship.destroyWithoutMessage();
        }
      }
    }
  }

  void handleCommand(Command command) {
    System.out.println("Unimplemented command.");
  }

  void showDialog() {
    System.out.print("""
        Enter command:
           quit:            q
           fire:            f <x> <y>
        """);
    if (torpedoMode) {
      System.out.printf("""
             torpedo fire:    t <x> <y>   (%d remaining)
          """, torpedoShotsRemaining);
    }
  }

  void showField() {
    int len = Integer.toString(n).length();
    String spaces = " ".repeat(len);
    System.out.printf("  %s", spaces);
    for (int i = 1; i <= m; i++) {
      System.out.printf(" %d%s", i, " ".repeat(len - Integer.toString(i).length() + 1));
    }
    System.out.println();
    for (int i = 1; i <= n; i++) {
      System.out.printf(" %d%s", i, " ".repeat(len - Integer.toString(i).length() + 1));
      for (int j = 1; j <= m; j++) {
        spaces = " ".repeat(len);
        if (field[i - 1][j - 1].spotted) {
          if (field[i - 1][j - 1] instanceof ShipCell shipCell) {
            if (shipCell.ship.sunk) {
              System.out.printf(" #%s", spaces);
            } else {
              System.out.printf(" x%s", spaces);
            }
          } else {
            System.out.printf(" *%s", spaces);
          }
        } else {
          System.out.printf(" o%s", spaces);
        }
      }
      System.out.println();
    }
  }

}

class Command {

}

class FireCommand extends Command {

  public int x, y;

  FireCommand(int x, int y) {
    this.x = x;
    this.y = y;
  }

  boolean isNotCorrect(int n, int m) {
    return x < 1 || y < 1 || x > n || y > m;
  }
}

class TorpedoCommand extends FireCommand {

  TorpedoCommand(int x, int y) {
    super(x, y);
  }
}

class QuitCommand extends Command {

}

class ShowFieldCommand extends Command {

}

class ExitAppException extends Exception {

}

class PlacingErrorException extends Exception {

}
package battleships;

import battleships.manager.Manager;

public class Main {

  public static void main(String[] args) {
    try {
      Manager manager = new Manager();
      if (args.length == 0) {
        manager.start();
      } else {
        manager.start(args);
      }
    } catch (Exception e) {
      System.out.println("Internal error, closing app");
    }
  }
}

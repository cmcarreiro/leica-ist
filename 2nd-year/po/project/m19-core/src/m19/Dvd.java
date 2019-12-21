package m19;

import m19.Category;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Dvd extends Work {
  private static final long serialVersionUID = 3L;

  public Dvd(int id, int copies, String title, int price, Category category, String director, String igac) {
      super(id, copies, "DVD", title, price, category, director, igac);
  }

  public String getDirector() {
    return super.getCreator();
  }

  public String getIgac() {
    return super.getStdNum();
  }
}

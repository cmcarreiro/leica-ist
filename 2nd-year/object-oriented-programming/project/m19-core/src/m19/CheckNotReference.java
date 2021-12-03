package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class CheckNotReference extends Rule {
  public CheckNotReference() {
    super.setId(5);
  }

  public boolean ok(User u, Work w) {
    if(w.getCategory() == Category.REFERENCE)
      return false;
    return true;
  }
}

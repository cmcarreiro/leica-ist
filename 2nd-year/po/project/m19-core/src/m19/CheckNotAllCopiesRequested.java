package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class CheckNotAllCopiesRequested extends Rule {
  public CheckNotAllCopiesRequested() {
    super.setId(3);
  }

  public boolean ok(User u, Work w) {
    if(w.getAvailable() == 0)
      return false;
    return true;
  }
}

package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class CheckNotRequestedTwice extends Rule {
  public CheckNotRequestedTwice() {
    super.setId(1);
  }

  public boolean ok(User u, Work w) {
    return !u.requestExists(w.getId());
  }
}

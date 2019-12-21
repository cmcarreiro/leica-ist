package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class CheckNotMaxUserRequests extends Rule {
  public CheckNotMaxUserRequests() {
    super.setId(4);
  }

  public boolean ok(User u, Work w) {
    if(u.getNumRequests() == u.getMaxRequests())
      return false;
    return true;
  }
}

package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */
 
public class CheckPriceNotAbove25 extends Rule {
  public CheckPriceNotAbove25() {
    super.setId(6);
  }

  public boolean ok(User u, Work w) {
    if(w.getPrice() >= 25)
      return false;
    return true;
  }
}

package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class CheckUserNotSuspended extends Rule {
  public CheckUserNotSuspended() {
    super.setId(2);
  }

  public boolean ok(User u, Work w) {
    if(u.getUserState() == UserState.SUSPENDED)
      return false;
    return true;
  }
}

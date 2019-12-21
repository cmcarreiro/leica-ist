package m19;

import java.io.Serializable;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Normal implements UserClassification, Serializable {

  private static final long serialVersionUID = 5L;

  public int timeOneCopy() {
    return 3;
  }

  public int timeFiveOrLessCopies() {
    return 8;
  }

  public int timeMoreFiveCopies() {
    return 15;
  }

  public int maxRequests() {
    return 3;
  }

  @Override
  public String toString(){
    return "NORMAL";
  }
}

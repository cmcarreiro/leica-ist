package m19;

import java.io.Serializable;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Abiding implements UserClassification, Serializable {
  private static final long serialVersionUID = 7L;

  public int timeOneCopy() {
    return 8;
  }

  public int timeFiveOrLessCopies() {
    return 15;
  }

  public int timeMoreFiveCopies() {
    return 30;
  }

  public int maxRequests() {
    return 5;
  }

  @Override
  public String toString(){
    return "CUMPRIDOR";
  }
}

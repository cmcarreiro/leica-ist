package m19;

import java.io.Serializable;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Delinquent implements UserClassification, Serializable {
  private static final long serialVersionUID = 6L;

  public int timeOneCopy() {
    return 2;
  }

  public int timeFiveOrLessCopies() {
    return 2;
  }

  public int timeMoreFiveCopies() {
    return 2;
  }

  public int maxRequests() {
    return 1;
  }

  @Override
  public String toString(){
    return "FALTOSO";
  }
}

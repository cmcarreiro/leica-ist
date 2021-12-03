package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public interface UserClassification {
  public int timeOneCopy();
  public int timeFiveOrLessCopies();
  public int timeMoreFiveCopies();
  public int maxRequests();
  public String toString();
}

package m19;

import java.io.Serializable;
import m19.Work;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Notification implements Serializable {
  private String _type;
  private Work _w;

  public Notification(String type, Work w) {
    _type = type;
    _w = w;
  }

  public String toString() {
    return _type + ": " + _w.toString();
  }
}

package m19;

import java.io.Serializable;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public abstract class Rule implements Serializable {
  private static final long serialVersionUID = 8L;

  private int _id;

  public int getId() {
    return _id;
  }

  public void setId(int id) {
    _id = id;
  }

  public abstract boolean ok(User u, Work w);
}

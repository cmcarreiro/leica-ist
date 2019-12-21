package m19;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class History implements Serializable {
  private ArrayList<Boolean> _history;

  public History() {
    _history = new ArrayList<Boolean>();
  }

  public void addEntry(boolean b) {
    _history.add(b);
  }

  public boolean abideLast5() {
    if(_history.size() < 5)
      return false;
    for(int i=_history.size()-5; i<_history.size(); i++)
      if(_history.get(i) == false)
        return false;
    return true;
  }

  public boolean lateLast3() {
    if(_history.size() < 3)
      return false;
    for(int i=_history.size()-3; i<_history.size(); i++)
      if(_history.get(i) == true)
        return false;
    return true;
  }

  public ArrayList<Boolean> getHistory() {
    return _history;
  }
}

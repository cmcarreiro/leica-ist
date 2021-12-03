package m19;

import java.io.Serializable;

public class Date implements Serializable {

  private int _date;

  public Date() {
    _date = 0;
  }

  public int getDate() {
    return _date;
  }

  public void advanceDate(int daysToAdvance) {
    _date += daysToAdvance;
    //fix me
    //user updates
  }
}

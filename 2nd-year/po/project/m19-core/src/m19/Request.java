package m19;

import java.io.Serializable;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Request implements Serializable {
  private User _u;
  private Work _w;
  private int _deadline;
  private int _fine;
  private boolean _pendingFine;

  public Request(User u, Work w, int currentDate) {
    _u = u;
    _w = w;
    _deadline = currentDate + timeAllowed();
    _fine = 0;
    _pendingFine = false;
  }

  public int timeAllowed() {
    if(_w.getCopies() == 1)
      return _u.getTimeOneCopy();
    else if(_w.getCopies() <= 5)
      return _u.getTimeFiveOrLessCopies();
    else
      return _u.getTimeMoreFiveCopies();
  }

  public int getUid() {
    return _u.getId();
  }

  public User getUser() {
    return _u;
  }

  public int getWid() {
    return _w.getId();
  }

  public Work getWork() {
    return _w;
  }

  public int getDeadline() {
    return _deadline;
  }

  public void pendingFine(){
    _pendingFine = true;
  }

  public boolean finePending(){
    return _pendingFine;
  }

  public void updateFine(int currDate) {
    _fine = currDate > _deadline ? (currDate-_deadline)*5 : 0;
  }

  public int getFine(int currDate) {
    updateFine(currDate);
    return _fine;
  }
}

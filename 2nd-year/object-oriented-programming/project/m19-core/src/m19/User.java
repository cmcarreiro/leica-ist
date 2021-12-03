package m19;

import java.io.Serializable;
import java.util.ArrayList;
import m19.Work;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class User implements Comparable<User>, Serializable {
  private static final long serialVersionUID = 4L;

  private Integer _id;
  private String _name;
  private String _email;
  private UserClassification _userClassification;
  private UserState _userState;
  private ArrayList<Integer> _requestedWorkIds;
  private ArrayList<Notification> _notifications;
  private History _history;
  private int _fine;
  private boolean _hasFine;

  public User(int id, String name, String email) {
    _id = id;
    _name = name;
    _email = email;
    _userClassification = new Normal();
    _userState = UserState.ACTIVE;
    _requestedWorkIds = new ArrayList<Integer>();
    _notifications = new ArrayList<Notification>();
    _history = new History();
    _fine = 0;
    _hasFine = false;
  }

  public Integer getId() {
    return _id;
  }

  public String getName() {
    return _name;
  }

  public String getEmail() {
    return _email;
  }

  public UserClassification getUserClassification() {
    return _userClassification;
  }

  public int getTimeOneCopy() {
    return _userClassification.timeOneCopy();
  }

  public int getTimeFiveOrLessCopies() {
    return _userClassification.timeFiveOrLessCopies();
  }

  public int getTimeMoreFiveCopies() {
    return _userClassification.timeMoreFiveCopies();
  }

  public int getMaxRequests() {
    return _userClassification.maxRequests();
  }

  public UserState getUserState() {
    return _userState;
  }

  public void addRequest(Integer wid) {
    _requestedWorkIds.add(wid);
  }

  public void rmvRequest(Integer wid) {
    _requestedWorkIds.remove(wid);
  }

  public boolean requestExists(Integer wid) {
    for(Integer i : _requestedWorkIds)
      if(i.equals(wid))
        return true;
    return false;
  }

  public int getNumRequests() {
    return _requestedWorkIds.size();
  }

  public void addNotification(Work w) {
    _notifications.add(new Notification("ENTREGA", w));
  }

  public void clearNotifications() {
    _notifications.clear();
  }

  public void addEntry(boolean b) {
    _history.addEntry(b);
  }

  public void updateClassification() {
    if(_history.abideLast5())
      _userClassification = new Abiding();
    else if(_history.lateLast3())
      _userClassification = new Delinquent();
  }

  public ArrayList<Boolean> getHistory() {
    return _history.getHistory();
  }

  public void setFine(int fine) {
    _fine = fine;
  }

  public void increaseFine(int fine) {
    _fine += fine;
  }

  public void clearFine(){
    _fine = 0;
  }

  public boolean hasFine() {
    return _hasFine;
  }

  public void setHasFine(boolean bool) {
    _hasFine = bool;
  }

  public void updateFine(int daysToAdvance, int countPending){
    if(_hasFine && _fine != 0)
      _fine += daysToAdvance * 5 * countPending;
  }

  public int getFine() {
    return _fine;
  }

  public void suspend() {
    _userState = UserState.SUSPENDED;
  }

  public void activate() {
    _userState = UserState.ACTIVE;
  }

  public boolean isSuspended() {
    return _userState == UserState.SUSPENDED;
  }

  public ArrayList<String> showNotifications() {
    ArrayList<String> _notifShow = new ArrayList<String>();
    for(Notification n : _notifications)
      _notifShow.add(n.toString());
    return _notifShow;
  }

  public boolean haveNotifications() {
    return _notifications.size() > 0;
  }

  @Override
  public String toString() {
    if(_hasFine)
      return _id+" - "+_name+" - "+_email+" - "+_userClassification.toString()+" - "+_userState.toString()+" - EUR "+_fine;
    return _id+" - "+_name+" - "+_email+" - "+_userClassification.toString()+" - "+_userState.toString();
  }

  @Override
  public int compareTo(User o) {
    int nameDiff = getName().compareTo(o.getName());
    if(nameDiff != 0)
      return nameDiff;
    else
      return getId().compareTo(o.getId());
  }
}

package m19;

import java.io.Serializable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import m19.exceptions.BadEntrySpecificationException;

import m19.Category;
import m19.Rule;
import m19.CheckNotRequestedTwice;
import m19.CheckUserNotSuspended;
import m19.CheckNotAllCopiesRequested;
import m19.CheckNotMaxUserRequests;
import m19.CheckNotReference;
import m19.CheckPriceNotAbove25;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */


/**
 * Class that represents the library as a whole.
 */
public class Library implements Serializable {

  /** Serial number for serialization. */
  private static final long serialVersionUID = 201901101348L;
  private int _id_work;
  private TreeMap<Integer, Work> _works;
  private int _id_user;
  private TreeMap<Integer, User> _users;
  private int _currDate;
  private ArrayList<Request> _requests;
  private ArrayList<Rule> _rules;

  public Library() {
    _id_work = 0;
    _works = new TreeMap<Integer, Work>();
    _id_user = 0;
    _users = new TreeMap<Integer, User>();
    _currDate = 0;
    _requests = new ArrayList<Request>();
    _rules = new ArrayList<Rule>();
    _rules.add(new CheckNotRequestedTwice());
    _rules.add(new CheckUserNotSuspended());
    _rules.add(new CheckNotAllCopiesRequested());
    _rules.add(new CheckNotReference());
    _rules.add(new CheckPriceNotAbove25());
  }

   /**
   * @param name
   *          name of the user to be created
   * @param email
   *          email of the user to be created
   */
   public int createUser(String name, String email) {
     _users.put(_id_user, new User(_id_user, name, email));
     return _id_user++;
   }

   /**
   * @param id
   *          necessary to identify and return user
   * @return library user
   */
   public User getUser(int id) {
     return _users.get(id);
   }

   /**
   * @return list of library users
   */
   public ArrayList<User> getUsers() {
     return new ArrayList<User>(_users.values());
   }


  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   * @return request deadline
   */
  public int addRequest(int uid, int wid) {
    Request req = new Request(getUser(uid), getWork(wid), getDate());
    _requests.add(req);                 //add to Lib
    _users.get(uid).addRequest(wid);    //add to User
    _works.get(wid).decNumAvailable();
    return req.getDeadline();
  }



  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   */
  public void returnWork(int uid, int wid) {
    Request req = getRequest(uid, wid);
    _users.get(uid).addEntry(getDate()<=req.getDeadline());
    _users.get(uid).updateClassification();
    _users.get(uid).rmvRequest(wid);    //remove from User
    _requests.remove(req);              //remove from Lib
    _works.get(wid).incNumAvailable();
    //notify all interested users
    for(int u_id : _works.get(wid).getInterestedUsers())
      _users.get(u_id).addNotification(_works.get(wid));
  }

  /**
   * @return ArrayList of Strings that shows all users
   */
  public ArrayList<String> showUsers() {
    ArrayList<User> _usersArray = getUsers();
    Collections.sort(_usersArray);
    ArrayList<String> _usersShow = new ArrayList<String>();
    for(User u : _usersArray)
      _usersShow.add(u.toString());
    return _usersShow;
  }

  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   */
  public void returnWorkFineNotPaid(int uid, int wid) {
    Request req = getRequest(uid, wid);
    req.pendingFine();
    _users.get(uid).addEntry(getDate()<=req.getDeadline());
    _users.get(uid).updateClassification();
    _users.get(uid).rmvRequest(wid);
    for(int u_id : _works.get(wid).getInterestedUsers())
      _users.get(u_id).addNotification(_works.get(wid));
    _works.get(wid).incNumAvailable();
  }

  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   */
  public void returnWorkPayFine(int uid, int wid) {
    Request req = getRequest(uid, wid);
    req.getUser().clearFine();
    returnWork(uid, wid);
    updateUserState(getUser(uid), true);
  }

  /**
   * @param uid
   *          user identifier
   */

  public void payFine(int uid){
    _requests.removeIf(r -> (r.getUser().getId() == uid && r.getDeadline() < _currDate && r.finePending()));
    _users.get(uid).clearFine();
    updateUserState(getUser(uid), true);
  }

  /**
   * @param uid
   *          user identifier
   */
  public int countPending(int uid){
    int count = 0;
    for (Request req : _requests){
      if(req.getUser().getId() == uid && req.getDeadline() < _currDate && req.finePending()){
        count++;
      }
    }
    return count;
  }


  /**
   * @param id
   *          necessary to identify and return work
   * @return library work
   */
  public Work getWork(int id) {
    return _works.get(id);
  }

  /**
   * @return list of library works
   */
  public Collection<Work> getWorks() {
    return _works.values();
  }

  /**
   * @return current date
   */
  public int getDate() {
    return _currDate;
  }

  /**
   * @param daysToAdvance
   *          number of days date should be advanced
   */
  public void advanceDate(int daysToAdvance) {
    _currDate += daysToAdvance;
    updateUsersState();
    updateUsersFines(daysToAdvance);
  }

  /**
   * @param daysToAdvance
   *          number of days date should be advanced
   */
  public void updateUsersFines(int daysToAdvance) {
    for(User u : _users.values())
      u.updateFine(daysToAdvance, countPending(u.getId()));
  }


  public void updateUsersState() {
    for(User u : _users.values())
      updateUserState(u, false);
  }

  /**
   * @param u
   *          user
   * @param finePayed
   *          is true if a fine has been paid
   */
  public void updateUserState(User u, boolean finePaid) {
    for(Request r : _requests)
      if(r.getUid() == u.getId() && r.getDeadline() < _currDate) {
          u.suspend();
          return;
      }
    if(finePaid){
      u.activate();
      u.setHasFine(false);
    }
  }

  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   * @return true if user is delivering the work late
   */
  public boolean deliveryLate(int uid, int wid) {
    for(Request r : _requests)
      if(r.getUid() == uid && r.getWid() == wid && r.getDeadline() < _currDate){
        r.getUser().setHasFine(true);
        r.getUser().increaseFine(r.getFine(_currDate));
        return true;
      }
    return false;
  }


  /**
   * @param uid
   *          user identifier
   * @return user's fine
   */
  public int getUserFine(int uid) {
    return getUser(uid).getFine();
  }

  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   * @return request associated with that uid and wid
   */
  public Request getRequest(int uid, int wid) {
    for(Request r : _requests)
      if(r.getUser().getId() == uid && r.getWork().getId() == wid)
        return r;
    return null;
  }

  /**
   * @param u
   *          user
   * @param w
   *          work
   * @return id of any rule that fails, 0 if none fail
   */
  public int checkRules(User u, Work w) {
    for(Rule r : _rules)
      if(r.ok(u, w) == false)
        return r.getId();
    return 0;
  }

  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   */
  public void addInterestedUser(int uid, int wid) {
    _works.get(wid).addInterestedUser(uid);
  }

  /**
   * @param uid
   *          user identifier
   * @return true if user exists
   */
  public boolean userExists(int uid) {
    return _users.get(uid) != null;
  }

  /**
   * @param wid
   *          work identifier
   * @return true if work exists
   */
  public boolean workExists(int wid) {
    return _works.get(wid) != null;
  }

  /**
   * @param uid
   *          user identifier
   * @param wid
   *          work identifier
   * @return true if any request associated with the uid and wid exists
   */
  public boolean requestExists(int uid, int wid) {
    for(Request r : _requests)
      if(r.getUser().getId() == uid && r.getWork().getId() == wid)
        return true;
    return false;
  }

  /**
   * @param uid
   *          user identifier
   * @return true if user is suspended
   */
  public boolean userIsSuspended(int uid) {
    return _users.get(uid).isSuspended();
  }

  /**
   * @param uid
   *          user identifier
   * @return ArrayList of Strings showing the user's notifications
   */
  public ArrayList<String> showUserNotifications(int uid) {
    return _users.get(uid).showNotifications();
  }

  /**
   * @param uid
   *          user identifier
   */
  public void clearUserNotifications(int uid) {
    _users.get(uid).clearNotifications();
  }

  /**
   * @param uid
   *          user identifier
   * @return true if user has notifications
   */
  public boolean userHasNotifications(int uid) {
    return _users.get(uid).haveNotifications();
  }

  /**
   * @return ArrayList of Strings that shows all Works
   */
  public ArrayList<String> showWorks() {
    ArrayList<String> _worksShow = new ArrayList<String>();
    for(Work w : getWorks())
      _worksShow.add(w.toString());
    return _worksShow;
  }

  /**
   * @param searchTerm
   *           string to search in works
   * @return ArrayList of Strings that shows all Works that contain searchTerm
   */
  public ArrayList<String> searchWorks(String searchTerm) {
    ArrayList<String> _worksSearch = new ArrayList<String>();
    for(Work w : getWorks())
      if(w.getTitle().contains(searchTerm) || w.getCreator().contains(searchTerm))
        _worksSearch.add(w.toString());
    return _worksSearch;
  }

  /**
   * @param args
   *          arguments needed to import book
   */
  public void importBook(String[] args) {
    String book_title = args[1];
    String author = args[2];
    int book_price = Integer.parseInt(args[3]);
    Category book_category = Category.valueOf(args[4]);
    String isbn = args[5];
    int book_copies = Integer.parseInt(args[6]);
    _works.put(_id_work, new Book(_id_work, book_copies, book_title, book_price, book_category, author, isbn));
    _id_work++;
  }

  /**
   * @param args
   *          arguments needed to import dvd
   */
  public void importDvd(String[] args) {
    String dvd_title = args[1];
    String director = args[2];
    int dvd_price = Integer.parseInt(args[3]);
    Category dvd_category = Category.valueOf(args[4]);
    String igac = args[5];
    int dvd_copies = Integer.parseInt(args[6]);
    _works.put(_id_work, new Dvd(_id_work, dvd_copies, dvd_title, dvd_price, dvd_category, director, igac));
    _id_work++;
  }

  /**
   * @param args
   *          arguments needed to import user
   */
  public void importUser(String[] args) {
    String name = args[1];
    String email = args[2];
    createUser(name, email);
  }

  /**
   * Read the text input file at the beginning of the program and populates the
   * instances of the various possible types (books, DVDs, users).
   *
   * @param filename
   *          name of the file to load
   * @throws BadEntrySpecificationException
   * @throws IOException
   */
  void importFile(String filename) throws BadEntrySpecificationException, IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line;
    while ((line=reader.readLine()) != null) {
      String[] parts = line.split(":");
      String type = parts[0];
      switch(type) {
          case "DVD":
            importDvd(parts);
            break;
          case "BOOK":
            importBook(parts);
            break;
          case "USER":
            importUser(parts);
            break;
      }
    }
    reader.close();
  }
}

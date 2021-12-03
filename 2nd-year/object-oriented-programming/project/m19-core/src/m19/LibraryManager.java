package m19;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

import m19.exceptions.MissingFileAssociationException;
import m19.exceptions.ImportFileException;
import m19.exceptions.BadEntrySpecificationException;
import m19.exceptions.FailedToOpenFileException;

import java.util.Collection;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * The façade class.
 */
public class LibraryManager {
  private Library _library;
  private String _filename;

  public LibraryManager() {
     _library = new Library();
  }

  public int addRequest(int uid, int wid) {
    return _library.addRequest(uid, wid);
  }

  public void returnWork(int uid, int wid) {
    _library.returnWork(uid, wid);
  }

  public Work getWork(int id) {
    return _library.getWork(id);
  }

  public Collection<Work> getWorks() {
    return _library.getWorks();
  }

  public User getUser(int id) {
    return _library.getUser(id);
  }

  public int createUser(String name, String email) {
    return _library.createUser(name, email);
  }

  public String getFilename() {
      return _filename;
  }

  public int checkRules(int uid, int wid) {
    return _library.checkRules(getUser(uid), getWork(wid));
  }

  public void addInterestedUser(int uid, int wid) {
    _library.addInterestedUser(uid, wid);
  }

  public boolean userExists(int uid) {
    return _library.userExists(uid);
  }

  public boolean workExists(int wid) {
    return _library.workExists(wid);
  }

  public boolean requestExists(int uid, int wid) {
    return _library.requestExists(uid, wid);
  }

  public boolean userIsSuspended(int uid) {
    return _library.userIsSuspended(uid);
  }

  public ArrayList<String> showUserNotifications(int uid) {
    return _library.showUserNotifications(uid);
  }

  public void clearUserNotifications(int uid) {
    _library.clearUserNotifications(uid);
  }

  public boolean userHasNotifications(int uid) {
    return _library.userHasNotifications(uid);
  }

  public ArrayList<String> showUsers() {
    return _library.showUsers();
  }

  public ArrayList<String> showWorks() {
    return _library.showWorks();
  }

  public ArrayList<String> searchWorks(String searchTerm) {
    return _library.searchWorks(searchTerm);
  }

  public boolean deliveryLate(int uid, int wid) {
    return _library.deliveryLate(uid, wid);
  }

  public int getUserFine(int uid) {
    return _library.getUserFine(uid);
  }

  public void returnWorkFineNotPaid(int uid, int wid) {
    _library.returnWorkFineNotPaid(uid, wid);
  }

  public void returnWorkPayFine(int uid, int wid) {
    _library.returnWorkPayFine(uid, wid);
  }

  public void payFine(int uid) {
    _library.payFine(uid);
  }


  /**
   * @throws MissingFileAssociationException
   * @throws IOException
   * @throws FileNotFoundException
   */
  public void save() throws MissingFileAssociationException, IOException {
    try {
      if(_filename == null) {
        throw new MissingFileAssociationException();
      }
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(_filename));
      out.writeObject(_library);
      out.close();
    } catch(IOException e) {
      e.printStackTrace();
    } catch(MissingFileAssociationException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param filename
   * @throws MissingFileAssociationException
   * @throws IOException
   */
  public void saveAs(String filename) throws MissingFileAssociationException, IOException {
    _filename = filename;
    save();
  }

  /**
   * @param filename
   * @throws FailedToOpenFileException
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public void load(String filename) throws FailedToOpenFileException, IOException, ClassNotFoundException {
    _filename = filename;
    try {
      ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
      _library = (Library) in.readObject();
      in.close();
    } catch(FileNotFoundException fnfe) {
      throw new FailedToOpenFileException(filename);
    } catch(IOException e) {
      e.printStackTrace();
    } catch(ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param datafile
   * @throws ImportFileException
   */
  public void importFile(String datafile) throws ImportFileException {
    try {
      _library.importFile(datafile);
    } catch (IOException | BadEntrySpecificationException e) {
      throw new ImportFileException(e);
    }
  }

  public int getDate() {
    return _library.getDate();
  }

  public void advanceDate(int daysToAdvance) {
    _library.advanceDate(daysToAdvance);
  }
}

package m19;

import java.io.Serializable;
import m19.Category;
import java.util.ArrayList;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public abstract class Work implements Serializable {
  private static final long serialVersionUID = 1L;

  private int _id;
  private int _copies;
  private int _available;
  private String _type;
  private String _title;
  private int _price;
  private Category _category;
  private String _creator;
  private String _stdNum;
  private ArrayList<Integer> _interestedUsers;

  public Work(int id, int copies, String type, String title, int price, Category category, String creator, String stdNum) {
    _id = id;
    _copies = copies;
    _available = copies;
    _type = type;
    _title = title;
    _price = price;
    _category = category;
    _creator = creator;
    _stdNum = stdNum;
    _interestedUsers = new ArrayList<Integer>();
  }

  public int getId() {
    return _id;
  }

  public int getAvailable() {
    return _available;
  }

  public String getTitle() {
    return _title;
  }

  public String getCreator() {
    return _creator;
  }

  public int getCopies() {
    return _copies;
  }

  public String getStdNum() {
    return _stdNum;
  }

  public int getPrice() {
    return _price;
  }

  public Category getCategory() {
    return _category;
  }

  public void decNumAvailable() {
    _available--;
  }

  public void incNumAvailable() {
    _available++;
  }

  public void addInterestedUser(int uid) {
    _interestedUsers.add(uid);
  }

  public ArrayList<Integer> getInterestedUsers() {
    return _interestedUsers;
  }

  @Override
  public String toString() {
    return _id + " - " + _available + " de  " + _copies + " - " + _type + " - " + _title + " - " + _price + " - " + _category.toString()  + " - " + _creator + " - " + _stdNum;
  }
}

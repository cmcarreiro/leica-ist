package m19;

import m19.Category;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public class Book extends Work {
  private static final long serialVersionUID = 2L;

  public Book(int id, int copies, String title, int price, Category category, String author, String isbn){
    super(id, copies, "Livro", title, price, category, author, isbn);
  }

  public String getAuthor() {
    return super.getCreator();
  }

  public String getIsbn() {
    return super.getStdNum();
  }
}

package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public enum Category {
  FICTION   ("Ficção"),
  REFERENCE ("Referência"),
  SCITECH   ("Técnica e Científica");

  private final String value;

  Category(String value) {
    this.value = value;
  }
  public String toString() {
    return value;
  }
}

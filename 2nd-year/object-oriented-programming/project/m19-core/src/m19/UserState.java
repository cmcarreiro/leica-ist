package m19;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

public enum UserState {
  ACTIVE    ("ACTIVO"),
  SUSPENDED ("SUSPENSO");

  private final String value;

  UserState(String value) {
    this.value = value;
  }
  public String toString() {
    return value;
  }
}

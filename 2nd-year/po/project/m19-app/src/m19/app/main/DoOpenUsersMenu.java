package m19.app.main;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.1.4. Command to open the users menu.
 */
public class DoOpenUsersMenu extends Command<LibraryManager> {

  /**
   * @param receiver
   */
  public DoOpenUsersMenu(LibraryManager receiver) {
    super(Label.OPEN_USERS_MENU, receiver);
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    m19.app.users.Menu menu = new m19.app.users.Menu(_receiver);
    menu.open();
  }

}

package m19.app.main;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.1.4. Command to open the requests menu.
 */
public class DoOpenRequestsMenu extends Command<LibraryManager> {

  /**
   * @param receiver
   */
  public DoOpenRequestsMenu(LibraryManager receiver) {
    super(Label.OPEN_REQUESTS_MENU, receiver);
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    m19.app.requests.Menu menu = new m19.app.requests.Menu(_receiver);
    menu.open();
  }
}

package m19.app.works;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.3.2. Display all works.
 */
public class DoDisplayWorks extends Command<LibraryManager> {

  /**
   * @param receiver
   */
  public DoDisplayWorks(LibraryManager receiver) {
    super(Label.SHOW_WORKS, receiver);
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    for(String s : _receiver.showWorks())
      _display.addLine(s);
    _display.display();
  }
}

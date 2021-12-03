package m19.app.works;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;
import m19.app.exceptions.NoSuchWorkException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.3.1. Display work.
 */
public class DoDisplayWork extends Command<LibraryManager> {
  Input<Integer> _wid;

  /**
   * @param receiver
   */
  public DoDisplayWork(LibraryManager receiver) {
    super(Label.SHOW_WORK, receiver);
    _wid = _form.addIntegerInput(Message.requestWorkId());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException {
    _form.parse();
    try {
      _display.popup(_receiver.getWork(_wid.value()));
    } catch(NullPointerException e) {
        throw new NoSuchWorkException(_wid.value());
    }
  }
}

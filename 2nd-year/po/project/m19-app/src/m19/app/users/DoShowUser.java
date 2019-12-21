package m19.app.users;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;
import m19.app.exceptions.NoSuchUserException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.2.2. Show specific user.
 */
public class DoShowUser extends Command<LibraryManager> {
  Input<Integer> _uid;

  /**
   * @param receiver
   */
  public DoShowUser(LibraryManager receiver) {
    super(Label.SHOW_USER, receiver);
    _uid = _form.addIntegerInput(Message.requestUserId());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException {
    _form.parse();
    if(!_receiver.userExists(_uid.value()))
      throw new NoSuchUserException(_uid.value());
    _display.popup(_receiver.getUser(_uid.value()));
  }
}

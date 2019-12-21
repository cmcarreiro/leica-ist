package m19.app.users;


import pt.tecnico.po.ui.Input;
import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;
import m19.app.exceptions.UserIsActiveException;
import m19.app.exceptions.NoSuchUserException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.2.5. Settle a fine.
 */
public class DoPayFine extends Command<LibraryManager> {
  Input<Integer> _uid;

  /**
   * @param receiver
   */
  public DoPayFine(LibraryManager receiver) {
    super(Label.PAY_FINE, receiver);
    _uid = _form.addIntegerInput(Message.requestUserId());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException {
    _form.parse();
    if(!_receiver.userExists(_uid.value()))
      throw new NoSuchUserException(_uid.value());
    else if(!_receiver.userIsSuspended(_uid.value()))
      throw new UserIsActiveException(_uid.value());
    _receiver.payFine(_uid.value());
  }

}

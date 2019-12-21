package m19.app.users;

import pt.tecnico.po.ui.Input;
import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import m19.app.exceptions.NoSuchUserException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.2.3. Show notifications of a specific user.
 */
public class DoShowUserNotifications extends Command<LibraryManager> {
  Input<Integer> _uid;

  /**
   * @param receiver
   */
  public DoShowUserNotifications(LibraryManager receiver) {
    super(Label.SHOW_USER_NOTIFICATIONS, receiver);
    _uid = _form.addIntegerInput(Message.requestUserId());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException {
    _form.parse();
    if(!_receiver.userExists(_uid.value()))
      throw new NoSuchUserException(_uid.value());
    else if(_receiver.userHasNotifications(_uid.value())) {
      for(String s : _receiver.showUserNotifications(_uid.value()))
        _display.addLine(s);
      _display.display();
      _receiver.clearUserNotifications(_uid.value());
    }
  }
}

package m19.app.users;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import pt.tecnico.po.ui.Input;
import m19.app.exceptions.UserRegistrationFailedException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.2.1. Register new user.
 */
public class DoRegisterUser extends Command<LibraryManager> {
  Input<String> _name;
  Input<String> _email;

  /**
   * @param receiver
   */
  public DoRegisterUser(LibraryManager receiver) {
    super(Label.REGISTER_USER, receiver);
    _name = _form.addStringInput(Message.requestUserName());
    _email = _form.addStringInput(Message.requestUserEMail());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws UserRegistrationFailedException {
    _form.parse();
    if(_name.value().isEmpty() || _email.value().isEmpty())
      throw new UserRegistrationFailedException(_name.value(), _email.value());
    int id = _receiver.createUser(_name.value(), _email.value());
    _display.popup(Message.userRegistrationSuccessful(id));
  }
}

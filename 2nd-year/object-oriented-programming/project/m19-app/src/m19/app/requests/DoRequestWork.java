package m19.app.requests;

import pt.tecnico.po.ui.Input;
import pt.tecnico.po.ui.Form;
import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import m19.app.exceptions.NoSuchUserException;
import m19.app.exceptions.NoSuchWorkException;
import m19.app.exceptions.RuleFailedException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.4.1. Request work.
 */
public class DoRequestWork extends Command<LibraryManager> {
  Input<Integer> _uid;
  Input<Integer> _wid;
  Form _formFollowUp = new Form();
  Input<String> _notifPref;

  /**
   * @param receiver
   */
  public DoRequestWork(LibraryManager receiver) {
    super(Label.REQUEST_WORK, receiver);
    _uid = _form.addIntegerInput(Message.requestUserId());
    _wid = _form.addIntegerInput(Message.requestWorkId());
    _notifPref = _formFollowUp.addStringInput(Message.requestReturnNotificationPreference());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException {
    _form.parse();
    if(!_receiver.userExists(_uid.value()))
      throw new NoSuchUserException(_uid.value());
    else if(!_receiver.workExists(_wid.value()))
      throw new NoSuchWorkException(_wid.value());
    int retVal = _receiver.checkRules(_uid.value(), _wid.value());
    if(retVal == 3) {
      _formFollowUp.parse();
      if(_notifPref.value().equals("s")) {
        _receiver.addInterestedUser(_uid.value(), _wid.value());
      }
    }
    else if(retVal > 0)
      throw new RuleFailedException(_uid.value(), _wid.value(), retVal);
    else
      _display.popup(Message.workReturnDay(_wid.value(), _receiver.addRequest(_uid.value(), _wid.value())));
  }
}

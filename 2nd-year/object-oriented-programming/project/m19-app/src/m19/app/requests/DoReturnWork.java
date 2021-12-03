package m19.app.requests;

import pt.tecnico.po.ui.Input;
import pt.tecnico.po.ui.Form;
import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.DialogException;
import m19.app.exceptions.NoSuchUserException;
import m19.app.exceptions.NoSuchWorkException;
import m19.app.exceptions.WorkNotBorrowedByUserException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.4.2. Return a work.
 */
public class DoReturnWork extends Command<LibraryManager> {
  Input<Integer> _uid;
  Input<Integer> _wid;
  Form _formFollowUp = new Form();
  Input<String> _payPref;

  /**
   * @param receiver
   */
  public DoReturnWork(LibraryManager receiver) {
    super(Label.RETURN_WORK, receiver);
    _uid = _form.addIntegerInput(Message.requestUserId());
    _wid = _form.addIntegerInput(Message.requestWorkId());
    _payPref = _formFollowUp.addStringInput(Message.requestFinePaymentChoice());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() throws DialogException {
    _form.parse();
    if(!_receiver.userExists(_uid.value()))
      throw new NoSuchUserException(_uid.value());
    else if(!_receiver.workExists(_wid.value()))
      throw new NoSuchWorkException(_wid.value());
    else if(!_receiver.requestExists(_uid.value(), _wid.value()))
      throw new WorkNotBorrowedByUserException(_wid.value(), _uid.value());
    else if(_receiver.deliveryLate(_uid.value(), _wid.value())){
      _display.popup(Message.showFine(_uid.value(), _receiver.getUserFine(_uid.value())));
      _formFollowUp.parse();
      if(_payPref.value().equals("n"))                //fine payed
        _receiver.returnWorkFineNotPaid(_uid.value(), _wid.value());
      else
        _receiver.returnWorkPayFine(_uid.value(), _wid.value());
  } else
    _receiver.returnWork(_uid.value(), _wid.value()); //no fine
  }
}

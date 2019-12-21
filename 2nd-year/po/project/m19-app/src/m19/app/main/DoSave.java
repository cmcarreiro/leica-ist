package m19.app.main;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;
import m19.exceptions.MissingFileAssociationException;
import java.io.IOException;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */

/**
 * 4.1.1. Save to file under current name (if unnamed, query for name).
 */
public class DoSave extends Command<LibraryManager> {
  Input<String> _filename;

  /**
   * @param receiver
   */
  public DoSave(LibraryManager receiver) {
    super(Label.SAVE, receiver);
    _filename = _form.addStringInput(Message.newSaveAs());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    try {
      if(_receiver.getFilename() != null) {     //check for existing filename association from open
        _filename.set(_receiver.getFilename());
      }
      else {
        _form.parse();
        _receiver.saveAs(_filename.value());
      }
    } catch(MissingFileAssociationException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
  }


/*
  @Override
  public final void execute() {
    try {
      if(_filename != null) {
        _receiver.save();
      } else {
        _filename = _form.addStringInput(Message.newSaveAs());
        //_form.parse();
        _receiver.saveAs(_filename.value());
      }
    } catch(MissingFileAssociationException e){
      e.printStackTrace();
    } catch(IOException e){
      e.printStackTrace();
    }
  }
*/
}

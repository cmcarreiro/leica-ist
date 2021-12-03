package m19.app.works;

import m19.LibraryManager;
import pt.tecnico.po.ui.Command;
import pt.tecnico.po.ui.Input;

/**
 * @author      Catarina Carreiro   92438
 * @author      Cristiano Clemente  92440
 */
 
/**
 * 4.3.3. Perform search according to miscellaneous criteria.
 */
public class DoPerformSearch extends Command<LibraryManager> {
  Input<String> _searchTerm;

  /**
   * @param m
   */
  public DoPerformSearch(LibraryManager m) {
    super(Label.PERFORM_SEARCH, m);
    _searchTerm = _form.addStringInput(Message.requestSearchTerm());
  }

  /** @see pt.tecnico.po.ui.Command#execute() */
  @Override
  public final void execute() {
    _form.parse();
    for(String s : _receiver.searchWorks(_searchTerm.value()))
      _display.addLine(s);
    _display.display();
  }
}

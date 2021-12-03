import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import ItemStatementAnswerDetails from '@/models/statement/questions/ItemStatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemCombinationStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public correctItems !: ItemStatementAnswerDetails[];

  constructor(jsonObj?: ItemCombinationStatementCorrectAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.correctItems = jsonObj.correctItems || [];
    }
  }
}

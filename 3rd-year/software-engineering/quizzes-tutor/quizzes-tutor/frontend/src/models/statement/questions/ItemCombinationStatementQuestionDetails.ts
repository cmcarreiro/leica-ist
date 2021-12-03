import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import ItemStatementQuestionDetails from '@/models/statement/questions/ItemStatementQuestionDetails';
import { _ } from 'vue-underscore';

export default class ItemCombinationStatementQuestionDetails extends StatementQuestionDetails {
  itemStatements: ItemStatementQuestionDetails[] = [];

  constructor(jsonObj?: ItemCombinationStatementQuestionDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      if (jsonObj.itemStatements) {
        this.itemStatements = jsonObj.itemStatements.map(
            (item: ItemStatementQuestionDetails) => new ItemStatementQuestionDetails(item)
          );
      }
    }
  }
}

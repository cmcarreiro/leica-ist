import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import StatementOption from '@/models/statement/StatementOption';
import { _ } from 'vue-underscore';

export default class MultipleChoiceStatementQuestionDetails extends StatementQuestionDetails {
  listOfStatementOptionDtos: StatementOption[] = [];

  constructor(jsonObj?: MultipleChoiceStatementQuestionDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      if (jsonObj.listOfStatementOptionDtos) {
        this.listOfStatementOptionDtos = _.shuffle(
          jsonObj.listOfStatementOptionDtos.map(
            (option: StatementOption) => new StatementOption(option)
          )
        );
      }
    }
  }
}

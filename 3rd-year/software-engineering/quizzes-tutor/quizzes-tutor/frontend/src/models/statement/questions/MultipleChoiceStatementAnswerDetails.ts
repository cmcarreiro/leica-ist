import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import MultipleChoiceStatementCorrectAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementCorrectAnswerDetails';

export default class MultipleChoiceStatementAnswerDetails extends StatementAnswerDetails {
  public listOfOptionIds: number[] = [];

  constructor(jsonObj?: MultipleChoiceStatementAnswerDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.listOfOptionIds = jsonObj.listOfOptionIds;
    }
  }

  isQuestionAnswered(): boolean {
    return this.listOfOptionIds != null;
  }

  isAnswerCorrect(
    correctAnswerDetails: MultipleChoiceStatementCorrectAnswerDetails
  ): boolean {
    let areListsTheSameLength = correctAnswerDetails.listOfCorrectOptionIds.length === this.listOfOptionIds.length;
    let doListsContainTheSameElementsInTheSameOrder = true;
    if (areListsTheSameLength) {
      for (let i = 0; i < correctAnswerDetails.listOfCorrectOptionIds.length; i++) {
        if (this.listOfOptionIds[i] !== correctAnswerDetails.listOfCorrectOptionIds[i]) {
          doListsContainTheSameElementsInTheSameOrder = false;
        }
      }
    }
    return (areListsTheSameLength && doListsContainTheSameElementsInTheSameOrder);
  }
}

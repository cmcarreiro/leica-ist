import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import OpenAnswerStatementCorrectAnswerDetails from '@/models/statement/questions/OpenAnswerStatementCorrectAnswerDetails';

export default class OpenAnswerStatementAnswerDetails extends StatementAnswerDetails {
  public answer: string | undefined;

  constructor(jsonObj?: OpenAnswerStatementAnswerDetails) {
    super(QuestionTypes.OpenAnswer);
    this.answer = jsonObj?.answer;
  }

  isQuestionAnswered(): boolean {
    return this.answer != null;
  }

  isAnswerCorrect(
    correctAnswerDetails: OpenAnswerStatementCorrectAnswerDetails
  ): boolean {
    return this.answer == correctAnswerDetails.correctAnswer;
  }
}

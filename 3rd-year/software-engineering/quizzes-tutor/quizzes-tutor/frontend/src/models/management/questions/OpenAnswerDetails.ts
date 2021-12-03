import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';

export default class OpenAnswerDetails extends AnswerDetails {
  answer!: string;

  constructor(jsonObj?: OpenAnswerDetails) {
    super(QuestionTypes.OpenAnswer);
    if (jsonObj) {
      this.answer = jsonObj.answer;
    }
  }

  isCorrect(questionDetails: OpenAnswerQuestionDetails): boolean {
    return this.answer === questionDetails.answer;
  }

  answerRepresentation(): string {
    if (this.answer.length > 10) {
      return this.answer.substring(0, 10) + '...';
    }
    return this.answer;
  }
}

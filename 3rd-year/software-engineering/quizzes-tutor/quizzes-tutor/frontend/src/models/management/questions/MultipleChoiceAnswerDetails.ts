import Option from '@/models/management/Option';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';

export default class MultipleChoiceAnswerType extends AnswerDetails {
  listOfOptionDtos!: Option[];

  constructor(jsonObj?: MultipleChoiceAnswerType) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.listOfOptionDtos = jsonObj.listOfOptionDtos
        .map(
          (option: Option) => new Option(option)
        );
    }
  }

  isCorrect(): boolean {
    return this.listOfOptionDtos.every(option => option.correct);
  }
  answerRepresentation(): string {
    if (this.listOfOptionDtos === null) return "-";
    return this.listOfOptionDtos.map((o: Option) => convertToLetter(o.sequence)).join('|');
  }
}
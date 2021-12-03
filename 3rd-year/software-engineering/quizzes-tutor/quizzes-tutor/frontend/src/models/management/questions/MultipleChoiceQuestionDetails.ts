import Option from '@/models/management/Option';
import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class MultipleChoiceQuestionDetails extends QuestionDetails {
  listOfOptions: Option[] = [new Option(), new Option(), new Option(), new Option()];
  orderMatters: boolean = false;
  isEditMode: boolean = false;

  constructor(jsonObj?: MultipleChoiceQuestionDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.listOfOptions = jsonObj.listOfOptions
        .map(
          (option: Option) => new Option(option)
        );
      if (this.listOfOptions.every(option => option.sequence != null)) {
        this.listOfOptions = this.listOfOptions.sort((optionA, optionB) => (optionA.sequence > optionB.sequence) ? 1 : -1);
      }
      this.orderMatters = jsonObj.listOfOptions.some(option => option.relevance != 0);
      this.isEditMode = jsonObj.listOfOptions.every(option => option.id != null);
    }
  }

  setAsNew(): void {
    this.listOfOptions.forEach(option => {
      option.id = null;
    });
  }
}

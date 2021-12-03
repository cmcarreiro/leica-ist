import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import ItemAnswer from '@/models/management/questions/ItemAnswer';

export default class ItemCombinationAnswerDetails extends AnswerDetails {
  itemAnswers: ItemAnswer[] = [];

  constructor(jsonObj?: ItemCombinationAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.itemAnswers = jsonObj.itemAnswers.map(
        (item: ItemAnswer) =>
          new ItemAnswer(item)
      );
    }
  }

  isCorrect(): boolean {
    if(!!this.itemAnswers){
      for(let i = 0; i < this.itemAnswers.length; i ++){
        if(!this.itemAnswers[i].correct) return false;
      }
    }
    return true;
  }
  answerRepresentation(): string {
    var answer :string = "";
    for(let i = 0; i < this.itemAnswers.length; i ++){
      answer += this.itemAnswers[i].content + ": " + this.itemAnswers[i].combinations;
      if(i != this.itemAnswers.length - 1) answer += " , ";
    }
    return answer;
  }
}
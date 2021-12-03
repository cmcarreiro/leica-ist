import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import ItemStatementAnswerDetails from '@/models/statement/questions/ItemStatementAnswerDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';

export default class ItemCombinationStatementAnswerDetails extends StatementAnswerDetails {
  public itemStatements !: ItemStatementAnswerDetails[];

  constructor(jsonObj?: ItemCombinationStatementAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.itemStatements = jsonObj.itemStatements || [];
    }
  }

  isQuestionAnswered(): boolean {
    return this.itemStatements != null  && this.itemStatements.length > 0;
  }

  isAnswerCorrect(
    correctAnswerDetails: ItemCombinationStatementCorrectAnswerDetails
  ): boolean {
    for (const i in this.itemStatements){
      for(const j in correctAnswerDetails.correctItems){
        if(this.itemStatements[i].itemId === correctAnswerDetails.correctItems[j].itemId){
          
          var combinations  = Object.assign([], this.itemStatements[i].combinations);
          combinations.sort();
          var correctCombinations = Object.assign([], correctAnswerDetails.correctItems[j].combinations);
          correctCombinations.sort();
          
          if(combinations?.length !== correctCombinations?.length){
            return false;
          }
          if(!!combinations && !!correctCombinations){
            for(var k = 0; k < combinations?.length; k++){
              if(!(combinations[k]=== correctCombinations[k])){
                return false;
              }
            }
          }
          break;
        }
      }
    }
    return true;
  }
}
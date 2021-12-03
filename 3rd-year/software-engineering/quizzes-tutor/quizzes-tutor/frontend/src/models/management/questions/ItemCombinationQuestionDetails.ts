import Item from '@/models/management/Item';
import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemCombinationQuestionDetails extends QuestionDetails {
  items: Item[] = [new Item(), new Item(), new Item(), new Item()];

  constructor(jsonObj?: ItemCombinationQuestionDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.items = jsonObj.items.map(
        (item: Item) => new Item(item)
      );
    }
  }

  setAsNew(): void {
    this.items.forEach(item => {
      item.id = null;
    });
  }
}

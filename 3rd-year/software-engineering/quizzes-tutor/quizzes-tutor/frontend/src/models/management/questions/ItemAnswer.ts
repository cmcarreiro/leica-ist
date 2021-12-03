export default class ItemAnswer {
  itemId: number | null = null;
  combinations: String[] | null = null;
  correct: boolean = false;
  sequence: number | null = null;
  content: string | null = null;

  constructor(jsonObj?: ItemAnswer) {
    if (jsonObj) {
      this.itemId = jsonObj.itemId;
      this.combinations = jsonObj.combinations;
      this.correct = jsonObj.correct;
      this.sequence = jsonObj.sequence;
      this.content = jsonObj.content;
    }
  }
}

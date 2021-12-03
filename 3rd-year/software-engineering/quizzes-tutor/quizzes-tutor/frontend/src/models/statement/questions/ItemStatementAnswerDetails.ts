export default class ItemStatementAnswerDetails {
  itemId: number | null = null;
  groupId: number | null = null;
  combinations: string[] | null = null;

  constructor(jsonObj?: ItemStatementAnswerDetails) {
    if (jsonObj) {
      this.itemId = jsonObj.itemId || this.itemId;
      this.combinations = jsonObj.combinations || this.combinations;
    }
  }
}

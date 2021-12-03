export default class StatementItem {
  itemId!: number;
  content!: string;
  //TODO: decide if combiantions should be in the statement

  constructor(jsonObj?: StatementItem) {
    if (jsonObj) {
      this.itemId = jsonObj.itemId;
      this.content = jsonObj.content;
    }
  }
}

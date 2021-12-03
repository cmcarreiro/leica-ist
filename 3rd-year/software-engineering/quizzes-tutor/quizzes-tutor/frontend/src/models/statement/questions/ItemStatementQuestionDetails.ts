export default class ItemStatementQuestionDetails {
  itemId: number | null = null;
  content: string = '';
  groupId: number | null = null;
  combinations: string[] = [];

  constructor(jsonObj?: ItemStatementQuestionDetails) {
    if (jsonObj) {
      this.itemId = jsonObj.itemId || this.itemId;
      this.content = jsonObj.content || this.content;
      this.groupId = jsonObj.groupId || this.groupId;

    }
  }
}

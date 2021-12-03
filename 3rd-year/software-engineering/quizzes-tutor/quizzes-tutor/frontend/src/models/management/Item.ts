export default class Item {
  id: number | null = null;
  sequence!: number | null;
  content: string = '';
  groupId!: number;
  combinations: string[] = [];

  constructor(jsonObj?: Item) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.sequence = jsonObj.sequence;
      this.content = jsonObj.content;
      this.groupId = jsonObj.groupId;
      this.combinations = jsonObj.combinations;
    }
  }
}

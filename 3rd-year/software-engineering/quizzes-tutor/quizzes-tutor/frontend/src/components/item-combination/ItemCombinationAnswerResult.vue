<!--
Used on:
  - ResultComponent.vue
-->

<template>
  <div class="item-combination-list">
  <v-row align="center" 
      v-for="(item, index) in answerDetails.itemStatements.filter(item => item.groupId ==2)"
      :key="index"
      data-cy="questionItemCombinationsInput"
    >
        <v-col
          class="d-flex justify-center"
          cols="12"
          sm="6"
        >
          <v-subheader v-text="questionDetails.itemStatements.find(x=>x.itemId == item.itemId).content"></v-subheader>
        </v-col>
        <v-col class="d-flex justify-center"
          cols="12"
          sm="6"
        >
        <span
        v-if=" verifyAnswer(item) "
        class="fas fa-check"
        />

        <span
        v-else class="fas fa-times"
        />
        <v-select
          disabled
          v-model="item.combinations"
          :items="getGroup1()"
          :menu-props="{ maxHeight: '400' }"
          label="Selected"
          :data-cy="`ItemCombination${index + 1}`"
          multiple
          :hint="getCorrectCombinations(item.itemId)"
          persistent-hint
        ></v-select>
        </v-col>
    </v-row>
  </div>
</template>


<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import ItemCombinationStatementQuestionDetails from '@/models/statement/questions/ItemCombinationStatementQuestionDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';
import ItemCombinationStatementAnswerDetails from '@/models/statement/questions/ItemCombinationStatementAnswerDetails';
import ItemStatementAnswerDetails from '@/models/statement/questions/ItemStatementAnswerDetails';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
@Component
export default class ItemCombinationAnswer extends Vue {
  @Prop(ItemCombinationStatementQuestionDetails)
  readonly questionDetails!: ItemCombinationStatementQuestionDetails;
  @Prop(ItemCombinationStatementAnswerDetails)
  readonly answerDetails!: ItemCombinationStatementAnswerDetails;
  @Prop(ItemCombinationStatementCorrectAnswerDetails)
  readonly correctAnswerDetails?: ItemCombinationStatementCorrectAnswerDetails;
    public getGroup1(): string[] {
    var group: string[] = new Array();
    for (var item of this.questionDetails.itemStatements) {
      if (item.groupId == 1)
        group.push(item.content);
    }
    return group;
  }
  public verifyAnswer(item: ItemStatementAnswerDetails):boolean {
    for(var i = 0; i < this.correctAnswerDetails.correctItems.length; i++){
      if(item.itemId === this.correctAnswerDetails.correctItems[i].itemId){
        var combinations  = Object.assign([], item.combinations);
        combinations.sort();
        var correctCombinations  = Object.assign([], this.correctAnswerDetails.correctItems[i].combinations);
        correctCombinations.sort();
        if(combinations.length !== correctCombinations.length){
          return false;
        }
        for(var j = 0; j < combinations.length; j++){
          if(!(combinations[j]===correctCombinations[j])){
            return false;
          }
        }
        break;
      }
    }
    return true;
  }
  public getCorrectCombinations(id: number){
    var correct: string[];
    for(var i = 0; i < this.correctAnswerDetails.correctItems.length; i++){
      if(this.correctAnswerDetails.correctItems[i].itemId === id){
        return "Correct Combinations: " + `${this.correctAnswerDetails.correctItems[i].combinations.toString()}`;
      }
    }
    return "No combinations";
  }
  itemClass(item: ItemStatementAnswerDetails) {
    if (!!this.correctAnswerDetails && this.verifyAnswer(item)) {
        return 'correct';
    }
    else {
      return 'wrong';
    }
  }
}
</script>

<style lang="scss" scoped>
.unanswered {
  .correct {
    .item-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }
  }
}
.correct-question {
  .correct {
    .item-content {
      background-color: #299455;
      color: rgb(255, 255, 255) !important;
    }
  }
}
.incorrect-question {
  .wrong {
    .item-content {
      background-color: #cf2323;
      color: rgb(255, 255, 255) !important;
    }
  }
  .correct {
    .item-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }
  }
}
</style>
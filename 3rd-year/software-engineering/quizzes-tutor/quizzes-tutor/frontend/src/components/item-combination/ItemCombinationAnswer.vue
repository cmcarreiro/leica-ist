<!--
Used on:
  - QuestionComponent.vue
-->

<template>  
  <div class="item-combination-list">
    <p>{{ init() }}</p>
  <v-row align="center" 
      v-for="(item, index) in questionDetails.itemStatements.filter(item => item.groupId == 2)"
      v-bind:key=" item.content"
      data-cy="questionItemCombinationsInput"
    >
        <v-col
          class="d-flex justify-center"
          cols="12"
          sm="6"
        >
          <v-subheader v-text="item.content"></v-subheader>
        </v-col>
        <v-col class="d-flex justify-center"
          cols="12"
          sm="6"
        >
          <v-select
            v-model="item.combinations"
            :items="getGroup1()"
            :menu-props="{ maxHeight: '400' }"
            label="Select"
            :data-cy="`ItemCombination${index + 1}`"
            multiple
            hint="Pick your combinations"
            persistent-hint
            @change="answerItem(item.itemId, item.combinations)"
          ></v-select>
        </v-col>
    </v-row>
  </div>
</template>


<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import ItemCombinationStatementQuestionDetails from '@/models/statement/questions/ItemCombinationStatementQuestionDetails';
import ItemCombinationStatementAnswerDetails from '@/models/statement/questions/ItemCombinationStatementAnswerDetails';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import ItemStatementQuestionDetails from '@/models/statement/questions/ItemStatementQuestionDetails';
import ItemStatementAnswerDetails from '@/models/statement/questions/ItemStatementAnswerDetails';


@Component
export default class ItemCombinationAnswer extends Vue {
  @Prop(ItemCombinationStatementQuestionDetails)
  readonly questionDetails!: ItemCombinationStatementQuestionDetails;
  @Prop(ItemCombinationStatementAnswerDetails)
  answerDetails!: ItemCombinationStatementAnswerDetails;


  public init() {
    if(this.answerDetails.itemStatements.length ==0){
      for(var i = 0; i< this.questionDetails.itemStatements.length; i++){
        var itemStatement = new ItemStatementAnswerDetails();
        itemStatement.itemId = this.questionDetails.itemStatements[i].itemId;
        itemStatement.groupId = this.questionDetails.itemStatements[i].groupId;
        itemStatement.combinations = [];
        this.answerDetails.itemStatements[i] = itemStatement;
      }
    }
  }

  public getGroup1(): string[] {
    var group: string[] = new Array();
    for (var item of this.questionDetails.itemStatements) {
      if (item.groupId == 1)
        group.push(item.content);
    }
    return group.sort();
  }

  public answerItem(id:number, combinations:string[]){
    var answer = this.answerDetails.itemStatements.find(x => x.itemId == id);
    answer.combinations = combinations;
  }
}
</script>
<template>
  <div v-if = "!answerDetails">
    <ul>
      <li v-for="item in questionDetails.items" :key="item.id">
        <span
          v-html="
            convertMarkDown(
            'Group '+ item.groupId+ ', Content: ' + item.content + ', Combinations: ['+ item.combinations +']'
            )
          "
        />
      </li>
    </ul>
  </div>
  <div v-else> 
    <ul>
      <li v-for="item in answerDetails.itemAnswers" :key="item.itemId">
        <span
          v-html="
            convertMarkDown(
            'Group '+ getGroup(item.itemId)+ ', Content: ' + getContent(item.itemId) + ', Combinations: ['+ item.combinations +'] ' + checkResult(item.correct)
            )
          "
        />
      </li>
    </ul>
  </div>
  
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import ItemCombinationAnswerDetails from '@/models/management/questions/ItemCombinationAnswerDetails';
@Component
export default class ItemCombinationView extends Vue {
  @Prop() readonly questionDetails!: ItemCombinationQuestionDetails;
  @Prop() readonly answerDetails?: ItemCombinationAnswerDetails;
  getContent(itemId: number): string{
    let item = this.questionDetails?.items.find(x => x.id == itemId);
    return item.content;
  }
  getGroup(itemId: number): string{
    let item = this.questionDetails?.items.find(x => x.id == itemId);
    return item.groupId;
  }
  checkResult(correct: boolean): string{
    if(correct) return '✔';
    else return '✖';
  }
  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
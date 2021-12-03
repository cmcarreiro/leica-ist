<template>
  <div>
    <span v-html="convertMarkDown(question.content, question.image)" />
    <br />
    <component
      :is="question.questionDetailsDto.type"
      :questionDetails="question.questionDetailsDto"
      :answerDetails="answer"
    />
    <br />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import MultipleChoiceView from '@/components/multiple-choice/MultipleChoiceView.vue';
import CodeFillInView from '@/components/code-fill-in/CodeFillInView.vue';
import CodeOrderView from '@/components/code-order/CodeOrderView.vue';
import OpenAnswerView from '@/components/open-answer/OpenAnswerView.vue';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import ItemCombinationView from '@/components/item-combination/ItemCombinationView.vue';
@Component({
  components: {
    multiple_choice: MultipleChoiceView,
    code_fill_in: CodeFillInView,
    code_order: CodeOrderView,
    item_combination: ItemCombinationView,
    open_answer: OpenAnswerView
  }
})
export default class ShowQuestion extends Vue {
  @Prop({ type: Question, required: true }) readonly question!: Question;
  @Prop() readonly answer?: AnswerDetails;
  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>

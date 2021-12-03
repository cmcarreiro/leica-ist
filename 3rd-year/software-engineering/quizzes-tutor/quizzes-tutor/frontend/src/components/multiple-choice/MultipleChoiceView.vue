<template>
  <div
    v-if="questionDetails.listOfOptions.some((option) => option.relevance != 0)"
  >
    <ul>
      <li v-for="option in questionDetails.listOfOptions" :key="option.id">
        <span
          v-if="option.correct"
          v-html="
            convertMarkDown(
              studentAnswered(option.id) +
                '**[' +
                option.relevance +
                ']** ' +
                option.content
            )
          "
          v-bind:class="[option.correct ? 'font-weight-bold' : '']"
        />
        <span
          v-else
          v-html="convertMarkDown(studentAnswered(option.id) + option.content)"
        />
      </li>
    </ul>
  </div>
  <div v-else>
    <ul>
      <li v-for="option in questionDetails.listOfOptions" :key="option.id">
        <span
          v-if="option.correct"
          v-html="
            convertMarkDown(
              studentAnswered(option.id) + '**[★]** ' + option.content
            )
          "
          v-bind:class="[option.correct ? 'font-weight-bold' : '']"
        />
        <span
          v-else
          v-html="convertMarkDown(studentAnswered(option.id) + option.content)"
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
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';
import MultipleChoiceAnswerDetails from '@/models/management/questions/MultipleChoiceAnswerDetails';

@Component
export default class MultipleChoiceView extends Vue {
  @Prop() readonly questionDetails!: MultipleChoiceQuestionDetails;
  @Prop() readonly answerDetails?: MultipleChoiceAnswerDetails;

  studentAnswered(id: number) {
    return this.answerDetails &&
      this.answerDetails?.listOfOptionDtos.map((o) => o.id).includes(id)
      ? '**[S]** '
      : '';
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>

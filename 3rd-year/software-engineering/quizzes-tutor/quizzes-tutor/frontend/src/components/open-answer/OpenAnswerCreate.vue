<template>
  <div class="open-answer-create">
    <v-textarea
      outlined
      label="Correct answer example"
      v-model="sQuestionDetails.answer"
      data-cy="textInput"
    >
    </v-textarea>
  </div>
</template>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';

@Component
export default class OpenAnswerCreate extends Vue {
  @PropSync('questionDetails', { type: OpenAnswerQuestionDetails })
  sQuestionDetails!: OpenAnswerQuestionDetails;
}
</script>

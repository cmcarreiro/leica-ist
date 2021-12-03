<template>
  <div>
    <v-textarea
      v-if="isAnswered"
      v-bind:value="answerDetails.answer"
      v-bind:readonly="true"
      data-cy="studentAnswer"
    >
    </v-textarea>
    <v-card data-cy="answerText">
      <v-card-subtitle class="text-left">
        Correct answer example:
      </v-card-subtitle>
      <v-card-text data-cy="answer" class="font-weight-bold">
        {{ questionDetails.answer }}
      </v-card-text>
    </v-card>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';
import OpenAnswerDetails from '@/models/management/questions/OpenAnswerDetails';

@Component
export default class OpenAnswerView extends Vue {
  @Prop() readonly questionDetails!: OpenAnswerQuestionDetails;
  @Prop() readonly answerDetails?: OpenAnswerDetails;

  get isAnswered() {
    return !!this.answerDetails;
  }
}
</script>

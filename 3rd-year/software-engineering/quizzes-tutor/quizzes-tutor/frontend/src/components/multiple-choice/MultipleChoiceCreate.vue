<template>
  <div class="multiple-choice-options">
    <v-row>
      <template
        v-if="
          sQuestionDetails.listOfOptions.filter((option) => option.correct).length >=
          2
        "
      >
        <v-col cols="1"> Order </v-col>
        <v-col cols="1">
          <v-switch
            v-model="sQuestionDetails.orderMatters"
            inset
            data-cy="orderMattersSwitch"
            @click="onOrderUpdate()"
          />
        </v-col>
        <v-col cols="1" offset="8"> Correct </v-col>
      </template>
      <template v-else>
        <v-col cols="1" offset="10"> Correct </v-col>
      </template>
    </v-row>

    <v-row
      v-for="(option, index) in sQuestionDetails.listOfOptions"
      :key="index"
      data-cy="questionOptionsInput"
    >
      <template
        v-if="
          sQuestionDetails.listOfOptions.filter((option) => option.correct).length >=
            2 && sQuestionDetails.orderMatters
        "
      >
        <v-col v-if="option.relevance > 0" cols="1">
          {{ option.relevance }}
        </v-col>
        <v-col v-else cols="1"> </v-col>
        <v-col cols="9">
          <v-textarea
            v-model="option.content"
            :label="`Option ${index + 1}`"
            :data-cy="`Option${index + 1}`"
            rows="1"
            auto-grow
          ></v-textarea>
        </v-col>
      </template>
      <template v-else>
        <v-col cols="10">
          <v-textarea
            v-model="option.content"
            :label="`Option ${index + 1}`"
            :data-cy="`Option${index + 1}`"
            rows="1"
            auto-grow
          ></v-textarea>
        </v-col>
      </template>
      <v-col cols="1">
        <v-switch
          v-model="option.correct"
          inset
          :data-cy="`Switch${index + 1}`"
          @click="onCorrectnessUpdate(index)"
        />
      </v-col>
      <v-col v-if="sQuestionDetails.listOfOptions.length > 2 && !sQuestionDetails.isEditMode">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              :data-cy="`Delete${index + 1}`"
              small
              class="ma-1 action-button"
              v-on="on"
              @click="removeOption(index)"
              color="red"
              >close</v-icon
            >
          </template>
          <span>Remove Option</span>
        </v-tooltip>
      </v-col>
    </v-row>

    <v-row v-if="!sQuestionDetails.isEditMode">
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addOption"
        data-cy="addOptionMultipleChoice"
        >Add Option</v-btn
      >
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';
import Option from '@/models/management/Option';

@Component
export default class MultipleChoiceCreate extends Vue {
  @PropSync('questionDetails', { type: MultipleChoiceQuestionDetails })
  sQuestionDetails!: MultipleChoiceQuestionDetails;

  addOption() {
    this.sQuestionDetails.listOfOptions.push(new Option());
  }

  removeOption(index: number) {
    const toBeRemovedOption: Option = this.sQuestionDetails.listOfOptions[index];
    if (toBeRemovedOption.correct) {
      this.sQuestionDetails.listOfOptions.forEach((option: Option) => {
        if (option.relevance > toBeRemovedOption.relevance) option.relevance--;
      });
    }  
    this.sQuestionDetails.listOfOptions.splice(index, 1);
  }

  onCorrectnessUpdate(index: number) {
    const updatedOption: Option = this.sQuestionDetails.listOfOptions[index];
    if (updatedOption.correct) {
      const numberOfCorrectOptions: number = this.sQuestionDetails.listOfOptions.filter(
        (option: Option) => option.correct
      ).length;
      updatedOption.relevance = numberOfCorrectOptions;
    } else if ( !updatedOption.correct ) {
        this.sQuestionDetails.listOfOptions.forEach((option: Option) => {
          if (option.relevance > updatedOption.relevance) option.relevance--;
        });
        updatedOption.relevance = 0;
    }
  }

  onOrderUpdate() {
    let i = 1;
    this.sQuestionDetails.listOfOptions.forEach((option: Option) => {
      if (option.correct && option.relevance == 0) {
        option.relevance = i;
        i++;
      } 
    });
  }

}
</script>

<template>
  <div class="item-combination-groups">

    <v-row
      v-for="(item, index) in sQuestionDetails.items"
      :key="index"
      data-cy="questionItemsInput"
    >
      <v-col cols="9" >
        <v-textarea
          v-model="item.content"
          :label="`Item ${index + 1}`"
          :data-cy="`Item${index + 1}`"
          rows="1"
          auto-grow
          @input="updateCombinations(index)"
        ></v-textarea>
      </v-col>
      <v-col
        class="d-flex"
        cols="12"
        sm="2"
      >
        <v-select
          v-model="item.groupId"
          :items="[1,2]"
          :data-cy="`ItemGroup${index + 1}`"
          label="Group"
          @change="updateItem(index)"
        />
      </v-col>
      <v-col v-if="sQuestionDetails.items.length > 2">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              :data-cy="`Delete${index + 1}`"
              small
              class="ma-1 action-button"
              v-on="on"
              @click="removeItem(index)"
              color="red"
              >close</v-icon
            >
          </template>
          <span>Remove Item</span>
        </v-tooltip>
      </v-col>
    </v-row>

    <v-row>
      <v-btn
        class="ma-auto"
        color="blue darken-1"
        @click="addItem"
        data-cy="addItemCombination"
        >Add Item</v-btn
      >
    </v-row>
    <v-row align="center" 
      v-for="(item, index) in sQuestionDetails.items.filter(item => item.groupId ==2)"
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
          ></v-select>
        </v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import Item from '@/models/management/Item';
@Component
export default class ItemCombinationCreate extends Vue {
  public getGroup1(): string[] {
    var group: string[] = new Array();
    for (var item of this.sQuestionDetails.items) {
      if (item.groupId == 1)
        group.push(item.content);
    }
    
    return group;
  }
  @PropSync('questionDetails', { type: ItemCombinationQuestionDetails })
  sQuestionDetails!: ItemCombinationQuestionDetails;
  addItem() {
    this.sQuestionDetails.items.push(new Item());
  }
  removeItem(index: number) {
    var content : string = this.sQuestionDetails.items[index].content;
    this.sQuestionDetails.items.forEach(items => {
      if(items.combinations.includes(content)){
          const i = items.combinations.indexOf(content, 0);
          if (index > -1) items.combinations.splice(i, 1);
      }
    });
    this.sQuestionDetails.items.splice(index, 1);
  }
  updateCombinations(index: number){
    if(this.sQuestionDetails.items[index].groupId == 1){
      for(let i = 0; i < this.sQuestionDetails.items.length; i++){
        this.sQuestionDetails.items[i].combinations = []
      }
    }
  }
  updateItem(index: number) {
    var content : string = this.sQuestionDetails.items[index].content;
    if(this.sQuestionDetails.items[index].groupId == 1){
      this.sQuestionDetails.items[index].combinations = [];
    }
    else{
      this.$nextTick(() => {
        this.sQuestionDetails.items[index].groupId = 2;
      })
      this.sQuestionDetails.items.forEach(items => {
        if(items.combinations.includes(content)){
          const i = items.combinations.indexOf(content, 0);
          if (i > -1) items.combinations.splice(i, 1);
        }
      });
    }
  }
}
</script>
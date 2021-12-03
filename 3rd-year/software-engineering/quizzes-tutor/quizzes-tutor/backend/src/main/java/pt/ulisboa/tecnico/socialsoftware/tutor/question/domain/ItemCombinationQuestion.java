package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<Item> items = new ArrayList<>();
    
    public ItemCombinationQuestion() {
        super();
    }

    public ItemCombinationQuestion(Question question, ItemCombinationQuestionDto questionDto) {
        super(question);
        setItems(questionDto.getItems());
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<ItemDto> itemsDto){
        int count1 = 0;
        int count2 = 0;
        for(ItemDto itemDto: itemsDto){
            if(itemDto.getGroupId()==1) count1++;
            else if(itemDto.getGroupId()==2) count2++;
        }
        if(count1 == 0) throw new TutorException(AT_LEAST_ONE_ITEM_IN_GROUP_ONE_NEEDED);
        if(count2 == 0) throw new TutorException(AT_LEAST_ONE_ITEM_IN_GROUP_TWO_NEEDED);

        verifyCombinations(itemsDto);

        for (Item item: this.items) {
            item.remove();
        }
        this.items.clear();

        int index = 0;
        for (ItemDto itemDto : itemsDto) {
            itemDto.setSequence(index++);
            new Item(itemDto).setQuestionDetails(this);

        }
    }

    private void verifyCombinations(List<ItemDto> itemsDto) {
        List<ItemDto> groupOne = itemsDto.stream().filter(it -> it.getGroupId().equals(1)).collect(Collectors.toList());
        List<ItemDto> groupTwo = itemsDto.stream().filter(it -> it.getGroupId().equals(2)).collect(Collectors.toList());

        for(ItemDto itemG2: groupTwo){
            if (itemG2.getCombinations().size() == 0) continue;
            for(ItemDto itemG1 : groupOne){
                if (itemG2.getCombinations().contains(itemG1.getContent())) {
                    return;
                }
            }
        }
        throw new TutorException(NO_CORRECT_ITEM_COMBINATION);
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void update(ItemCombinationQuestionDto questionDetails) {
        setItems(questionDetails.getItems());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitItems(Visitor visitor) {
        for (Item item : this.items) {
            item.accept(visitor);
        }
    }


    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new ItemCombinationCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new ItemCombinationStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new ItemCombinationStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new ItemCombinationAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new ItemCombinationQuestionDto(this);
    }


    @Override
    public void delete() {
        super.delete();
        for (Item item : this.items) {
            item.remove();
        }
        this.items.clear();
    }

    @Override
    public String toString() {
        return "ItemCombinationQuestion{" +
                "items=" + items +
                '}';
    }


    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        return null;
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        String answer = "";
       
        for(int i = 0; i < this.items.size(); i ++){
            if(this.items.get(i).getGroupId() == 1) continue;
            answer += this.items.get(i).getContent() + ": " + this.items.get(i).getCombinations();
            if(i != this.items.size() - 1) answer += " , ";
        }
        return answer;
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }
}
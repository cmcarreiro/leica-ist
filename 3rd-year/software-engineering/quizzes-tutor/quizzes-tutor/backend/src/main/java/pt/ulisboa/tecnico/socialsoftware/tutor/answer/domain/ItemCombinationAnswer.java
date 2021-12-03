package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_ITEM_MISMATCH;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationAnswer extends AnswerDetails {
    @OneToMany(mappedBy = "itemCombinationAnswer", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemAnswer> itemAnswers = new HashSet<>();

    public ItemCombinationAnswer() {
        super();
    }

    public ItemCombinationAnswer(QuestionAnswer questionAnswer){
        super(questionAnswer);
    }

    public Set<ItemAnswer> getItemAnswers() {
        return itemAnswers;
    }

    public void setItemAnswers(Set<ItemAnswer> itemAnswers) {
        this.itemAnswers = itemAnswers;
    }

    public void setItemAnswers(ItemCombinationQuestion question, ItemCombinationStatementAnswerDetailsDto itemCombinationStatementAnswerDetailsDto) {
        this.itemAnswers.clear();
        if (!itemCombinationStatementAnswerDetailsDto.emptyAnswer()) {
            for(ItemStatementAnswerDetailsDto itemStatement : itemCombinationStatementAnswerDetailsDto.getItemStatements()){

                if(itemStatement.getItemId() == null){
                    continue;
                }

                Item item = question.getItems().stream()
                .filter(item1 -> item1.getId().equals(itemStatement.getItemId()))
                .findAny()
                .orElseThrow(() -> new TutorException(QUESTION_ITEM_MISMATCH, itemStatement.getItemId()));

                ItemAnswer itemAnswer = new ItemAnswer(item, this, itemStatement.getCombinations());
                getItemAnswers().add(itemAnswer);
            }
        }
    }

    @Override
    public boolean isCorrect() {
        return this.getItemAnswers().stream().allMatch(ItemAnswer::isCorrect);
    }


    public void remove() {
        if (itemAnswers != null) {
            this.getItemAnswers().forEach(ItemAnswer::remove);
            itemAnswers.clear();
        }
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new ItemCombinationAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return this.getItemAnswers() != null && !itemAnswers.isEmpty();
    }

    @Override
    public String getAnswerRepresentation() {
        String answer = "";
        int i = 0;
        for(ItemAnswer itemAnswer: itemAnswers){
            answer += itemAnswer.getItem().getContent() + ": " + itemAnswer.getCombinations();
            if (i++ < itemAnswers.size()) answer += ", ";
        }
        return answer;
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new ItemCombinationStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}

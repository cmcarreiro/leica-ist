package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item;

import java.util.Set;
import java.util.HashSet;

public class ItemAnswerDto {
    private Integer itemId;
    private Set<String> combinations;
    private boolean correct;
    private Integer sequence;
    private String content;

    public ItemAnswerDto(Item correctItem) {
        itemId = correctItem.getId();
        combinations = new HashSet(correctItem.getCombinations());
        correct = true;
        content = correctItem.getContent();
    }

    public ItemAnswerDto(ItemAnswer itemAnswer) {
        itemId = itemAnswer.getItem().getId();
        combinations = itemAnswer.getCombinations();
        correct = itemAnswer.isCorrect();
        sequence = itemAnswer.getItem().getSequence();
        content = itemAnswer.getItem().getContent();
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Set<String> getCombinations() {
        return combinations;
    }

    public void setCombinations(Set<String> combinations) {
        this.combinations = combinations;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }
}

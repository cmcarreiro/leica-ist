package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemAnswer;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;

public class ItemStatementAnswerDetailsDto implements Serializable {
    private Integer itemId;
    private Set<String> combinations;

    public ItemStatementAnswerDetailsDto() {
    }

    public ItemStatementAnswerDetailsDto(Integer itemId, HashSet<String> combinations) {
        this.itemId = itemId;
        this.combinations = combinations;
    }

    public ItemStatementAnswerDetailsDto(ItemAnswer item) {
        this.itemId = item.getItem().getId();
        this.combinations = new HashSet<>(item.getCombinations());
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
}
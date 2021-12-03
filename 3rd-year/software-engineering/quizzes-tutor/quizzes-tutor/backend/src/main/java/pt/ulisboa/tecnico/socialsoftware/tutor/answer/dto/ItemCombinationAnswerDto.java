package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationAnswerDto extends AnswerDetailsDto {
    private List<ItemAnswerDto> itemAnswers = new ArrayList<>();

    public ItemCombinationAnswerDto() {
    }

    public ItemCombinationAnswerDto(ItemCombinationAnswer answer) {
        if (answer.getItemAnswers() != null)
            this.itemAnswers = answer.getItemAnswers().stream().map(ItemAnswerDto::new).collect(Collectors.toList());
    }

    public List<ItemAnswerDto> getItemAnswers() {
        return itemAnswers;
    }

    public void setItemAnswers(List<ItemAnswerDto> itemAnswers) {
        this.itemAnswers = itemAnswers;
    }
}

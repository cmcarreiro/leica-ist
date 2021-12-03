package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationCorrectAnswerDto extends CorrectAnswerDetailsDto {
    private List<ItemAnswerDto> correctItems;

    public ItemCombinationCorrectAnswerDto(ItemCombinationQuestion question) {
        this.correctItems = question.getItems()
                .stream()
                .map(ItemAnswerDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemAnswerDto> getCorrectItems() {
        return correctItems;
    }

    public void setCorrectItems(List<ItemAnswerDto> correctItems) {
        this.correctItems = correctItems;
    }
}
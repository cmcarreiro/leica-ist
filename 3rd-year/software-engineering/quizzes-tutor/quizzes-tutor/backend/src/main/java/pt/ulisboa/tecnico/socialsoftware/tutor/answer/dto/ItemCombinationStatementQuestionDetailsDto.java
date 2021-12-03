package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private List<ItemStatementQuestionDetailsDto> itemStatements;

    public ItemCombinationStatementQuestionDetailsDto(ItemCombinationQuestion question) {
        this.itemStatements = question.getItems().stream()
                .map(ItemStatementQuestionDetailsDto::new)
                .collect(Collectors.toList());
    }

    public List<ItemStatementQuestionDetailsDto> getItemStatements() {
        return itemStatements;
    }

    public void setItemStatements(List<ItemStatementQuestionDetailsDto> itemStatements) {
        this.itemStatements = itemStatements;
    }

    @Override
    public String toString() {
        return "ItemCombinationStatementQuestionDetailsDto{" +
                "itemStatements=" + itemStatements +
                '}';
    }
}
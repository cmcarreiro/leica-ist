package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationAnswerItem extends QuestionAnswerItem {

    @ElementCollection
    private List<ItemAnswerItem> itemAnwers;

    public ItemCombinationAnswerItem() {
    }

    public ItemCombinationAnswerItem(String username, int quizId, StatementAnswerDto answer, ItemCombinationStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.itemAnwers = detailsDto.getItemStatements()
                .stream()
                .map(ItemAnswerItem::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return questionDetails.getAnswerRepresentation(
                itemAnwers.stream()
                        .map(ItemAnswerItem::getItemId)
                        .collect(Collectors.toList()));
    }
}

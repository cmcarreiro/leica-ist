package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.AnswerDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationStatementAnswerDetailsDto extends StatementAnswerDetailsDto {

    private List<ItemStatementAnswerDetailsDto> itemStatements = new ArrayList<>();

    private ItemCombinationAnswer itemCombinationAnswer;

    public ItemCombinationStatementAnswerDetailsDto() {
    }

    public ItemCombinationStatementAnswerDetailsDto(ItemCombinationAnswer questionAnswer) {
        if (questionAnswer.getItemAnswers() != null) {
            this.itemStatements = questionAnswer.getItemAnswers()
            .stream().map(ItemStatementAnswerDetailsDto::new).collect(Collectors.toList());
        }
    }

    public List<ItemStatementAnswerDetailsDto> getItemStatements() {
        return itemStatements;
    }

    public void setItemStatements(List<ItemStatementAnswerDetailsDto> itemStatements) {
        this.itemStatements = itemStatements;
    }

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        itemCombinationAnswer = new ItemCombinationAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return itemCombinationAnswer;
    }

    @Override
    public boolean emptyAnswer() {
        return itemStatements == null || itemStatements.isEmpty();
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new ItemCombinationAnswerItem(username, quizId, statementAnswerDto, this);
    }

    @Override
    public void update(ItemCombinationQuestion question) {
        itemCombinationAnswer.setItemAnswers(question, this);
    }

    @Override
    public String toString() {
        return "ItemCombinationStatementAnswerDto{" +
                "itemStatements=" + itemStatements +
                '}';
    }
}

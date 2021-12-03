package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question.QuestionTypes.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = MultipleChoiceStatementQuestionDetailsDto.class,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MultipleChoiceStatementQuestionDetailsDto.class, name = MULTIPLE_CHOICE_QUESTION),
        @JsonSubTypes.Type(value = CodeFillInStatementQuestionDetailsDto.class, name = CODE_FILL_IN_QUESTION),
        @JsonSubTypes.Type(value = CodeOrderStatementQuestionDetailsDto.class, name = CODE_ORDER_QUESTION),
        @JsonSubTypes.Type(value = OpenAnswerStatementQuestionDetailsDto.class, name = OPEN_ANSWER_QUESTION),
        @JsonSubTypes.Type(value = ItemCombinationStatementQuestionDetailsDto.class, name = ITEM_COMBINATION_QUESTION),
})
public abstract class StatementQuestionDetailsDto {
}

package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenAnswerStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.Arrays;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ANSWER_QUESTION)
public class OpenAnswerItem extends QuestionAnswerItem {

    private String answer;

    public OpenAnswerItem() {
    }

    public OpenAnswerItem(String username, int quizId, StatementAnswerDto answer, OpenAnswerStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.answer = detailsDto.getAnswer();
    }

    @Override
    public String getAnswerRepresentation (QuestionDetails questionDetails) {
        return this.getAnswer() != null ? questionDetails.getAnswerRepresentation(null) : "-";
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

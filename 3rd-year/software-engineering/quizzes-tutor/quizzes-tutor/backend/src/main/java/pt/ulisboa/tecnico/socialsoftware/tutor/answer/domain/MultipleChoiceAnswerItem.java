package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ElementCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswerItem extends QuestionAnswerItem {

    @ElementCollection
    private List<Integer> listOfOptionIds = new ArrayList<>();;

    public MultipleChoiceAnswerItem() {
    }

    public MultipleChoiceAnswerItem(String username, int quizId, StatementAnswerDto answer, MultipleChoiceStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.listOfOptionIds = detailsDto.getListOfOptionIds();
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return this.getListOfOptionIds() != null ? questionDetails.getAnswerRepresentation(listOfOptionIds) : "-";
    }

    public List<Integer> getListOfOptionIds() {
        return listOfOptionIds;
    }

    public void setListOfOptionIds(List<Integer> listOfOptionIds) {
        this.listOfOptionIds = listOfOptionIds;
    }
}

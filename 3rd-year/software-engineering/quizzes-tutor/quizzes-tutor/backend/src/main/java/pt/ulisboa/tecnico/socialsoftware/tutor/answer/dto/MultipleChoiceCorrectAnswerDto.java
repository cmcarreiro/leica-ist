package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;

import java.util.List;

public class MultipleChoiceCorrectAnswerDto extends CorrectAnswerDetailsDto {
    private List<Integer> listOfCorrectOptionIds;

    public MultipleChoiceCorrectAnswerDto(MultipleChoiceQuestion question) {
        this.listOfCorrectOptionIds = question.getListOfCorrectOptionIds();
    }

    public List<Integer> getListOfCorrectOptionIds() {
        return listOfCorrectOptionIds;
    }

    public void setListOfCorrectOptionIds(List<Integer> listOfCorrectOptionIds) {
        this.listOfCorrectOptionIds = listOfCorrectOptionIds;
    }

    @Override
    public String toString() {
        return "MultipleChoiceCorrectAnswerDto{" +
                "listOfCorrectOptionIds=" + listOfCorrectOptionIds +
                '}';
    }
}
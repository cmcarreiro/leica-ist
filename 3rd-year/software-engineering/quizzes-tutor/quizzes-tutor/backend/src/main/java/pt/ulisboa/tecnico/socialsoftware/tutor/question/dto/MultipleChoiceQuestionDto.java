package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleChoiceQuestionDto extends QuestionDetailsDto {
    private List<OptionDto> listOfOptions = new ArrayList<>();

    public MultipleChoiceQuestionDto() {
    }

    public MultipleChoiceQuestionDto(MultipleChoiceQuestion question) {
        this.listOfOptions = question.getListOfOptions().stream().map(OptionDto::new).collect(Collectors.toList());
    }

    public List<OptionDto> getListOfOptions() {
        return listOfOptions;
    }

    public void setListOfOptions(List<OptionDto> listOfOptions) {
        this.listOfOptions = listOfOptions;
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new MultipleChoiceQuestion(question, this);
    }

    @Override
    public void update(MultipleChoiceQuestion question) {
        question.update(this);
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestionDto{" +
                "listOfOptions=" + listOfOptions +
                '}';
    }

}

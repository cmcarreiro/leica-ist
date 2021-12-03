package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultipleChoiceStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private List<StatementOptionDto> listOfStatementOptionDtos = new ArrayList<>();

    public MultipleChoiceStatementQuestionDetailsDto(MultipleChoiceQuestion question) {
        this.listOfStatementOptionDtos = question.getListOfOptions().stream()
                .map(StatementOptionDto::new)
                .collect(Collectors.toList());
    }

    public List<StatementOptionDto> getListOfStatementOptionDtos() {
        return listOfStatementOptionDtos;
    }

    public void setListOfStatementOptionDtos(List<StatementOptionDto> listOfStatementOptionDtos) {
        this.listOfStatementOptionDtos = listOfStatementOptionDtos;
    }

    @Override
    public String toString() {
        return "MultipleChoiceStatementQuestionDetailsDto{" +
                "listOfStatementOptionDtos=" + listOfStatementOptionDtos +
                '}';
    }
}
package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceAnswerDto extends AnswerDetailsDto {
    private ArrayList<OptionDto> listOfOptionDtos = new ArrayList<>();;

    public MultipleChoiceAnswerDto() {
    }

    public MultipleChoiceAnswerDto(MultipleChoiceAnswer answer) {
        if (answer.getListOfOptions() != null) {
            this.listOfOptionDtos = new ArrayList<>();
            for(Option option : answer.getListOfOptions())
                listOfOptionDtos.add(new OptionDto(option));
        }
    }

    public List<OptionDto> getListOfOptionDtos() {
        return listOfOptionDtos;
    }

    public void setListOfOptionDtos(List<OptionDto> listOfOptionDtos) {
        this.listOfOptionDtos = new ArrayList<>(listOfOptionDtos);
    }
}

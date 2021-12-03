package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenAnswer;

public class OpenAnswerDto extends AnswerDetailsDto {
    private String answer;

    public OpenAnswerDto() {
    }

    public OpenAnswerDto(OpenAnswer answer) {
        if (answer.getAnswer() != null)
            this.answer = answer.getAnswer();
    }

    public String getAnswer() { return answer; }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}

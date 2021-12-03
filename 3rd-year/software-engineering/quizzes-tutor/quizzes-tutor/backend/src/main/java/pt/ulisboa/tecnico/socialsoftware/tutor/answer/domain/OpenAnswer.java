package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ANSWER_QUESTION)
public class OpenAnswer extends AnswerDetails {

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column
    private boolean matches = false;

    public OpenAnswer() {
        super();
    }

    public OpenAnswer(QuestionAnswer questionAnswer){
        super(questionAnswer);
    }

    public OpenAnswer(QuestionAnswer questionAnswer, String answer){
        super(questionAnswer);
        this.setAnswer(answer);
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(OpenAnswerQuestion question, OpenAnswerStatementAnswerDetailsDto openAnswerStatementAnswerDetailsDto) {

        if (openAnswerStatementAnswerDetailsDto.getAnswer() != null) {
            this.setAnswer(openAnswerStatementAnswerDetailsDto.getAnswer());
            this.matches = question.getAnswer().equals(answer);

        } else {
            this.setAnswer(null);
        }
    }

    @Override
    public boolean isCorrect() {
        return this.matches;
    }


    public void remove() {
        this.answer = null;
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new OpenAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return this.getAnswer() != null;
    }

    @Override
    public String getAnswerRepresentation() {
        return answer;
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new OpenAnswerStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}

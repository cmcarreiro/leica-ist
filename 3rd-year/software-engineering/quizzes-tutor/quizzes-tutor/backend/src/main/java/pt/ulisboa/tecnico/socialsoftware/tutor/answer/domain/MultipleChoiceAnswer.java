package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_OPTION_MISMATCH;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswer extends AnswerDetails {
    @ManyToMany
    @JoinColumn(name = "option_id")
    private List<Option> listOfOptions = new ArrayList<>();

    public MultipleChoiceAnswer() {
        super();
    }

    public MultipleChoiceAnswer(QuestionAnswer questionAnswer){
        super(questionAnswer);
    }

    public MultipleChoiceAnswer(QuestionAnswer questionAnswer, List<Option> listOfOptions){
        super(questionAnswer);
        this.setListOfOptions(listOfOptions);
    }

    public List<Option> getListOfOptions() {
        return listOfOptions;
    }

    public void setListOfOptions(List<Option> listOfOptions) {
        this.listOfOptions = new ArrayList<>(listOfOptions);

        if (listOfOptions != null)
            for(Option option : listOfOptions)
                option.addQuestionAnswer(this);
    }

    public void setListOfOptions(MultipleChoiceQuestion question, MultipleChoiceStatementAnswerDetailsDto multipleChoiceStatementAnswerDetailsDto) {
        if (multipleChoiceStatementAnswerDetailsDto.getListOfOptionIds() != null) {
            if (this.getListOfOptions() != null)
                for(Option option : this.getListOfOptions())
                    option.getQuestionAnswers().remove(this);

            ArrayList<Option> listOfOptionsThatMatchIds = new ArrayList<>();
            for (Integer id : multipleChoiceStatementAnswerDetailsDto.getListOfOptionIds()) {
                Option optionThatMatchesId = null;
                for (Option option : question.getListOfOptions()) {
                    if(option.getId() == id) {
                        optionThatMatchesId = option;
                    }
                }
                listOfOptionsThatMatchIds.add(optionThatMatchesId);
            }
            this.setListOfOptions(listOfOptionsThatMatchIds);
        } else
            this.setListOfOptions(null);
    }

    @Override
    public boolean isCorrect() {
        return getListOfOptions() != null && getListOfOptions().stream().allMatch(o -> o.isCorrect());
    }


    public void remove() {
        if (listOfOptions != null) {
            for(Option option : this.getListOfOptions())
                option.getQuestionAnswers().remove(this);
            listOfOptions = null;
        }
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return this.getListOfOptions() != null;
    }

    @Override
    public String getAnswerRepresentation() {
        if(this.getListOfOptions() == null) return "-";
        String answerRepresentation = "";
        for(Option option : this.getListOfOptions())
            answerRepresentation += MultipleChoiceQuestion.convertSequenceToLetter(option.getSequence());
        return answerRepresentation;
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}

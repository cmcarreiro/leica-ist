package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Option> listOfOptions = new ArrayList<>();


    public MultipleChoiceQuestion() {
        super();
    }

    public MultipleChoiceQuestion(Question question, MultipleChoiceQuestionDto questionDto) {
        super(question);
        setListOfOptions(questionDto.getListOfOptions());
    }

    public List<Option> getListOfOptions() {
        return listOfOptions;
    }

    public void setListOfOptions(List<OptionDto> options) {
        if (options.stream().filter(OptionDto::isCorrect).count() == 0) {
            throw new TutorException(AT_LEAST_ONE_CORRECT_OPTION_NEEDED);
        }

        int index = 0;
        for (OptionDto optionDto : options) {
            if (optionDto.getId() == null) {
                optionDto.setSequence(index++);
                new Option(optionDto).setQuestionDetails(this);
            } else {
                Option option = getListOfOptions()
                        .stream()
                        .filter(op -> op.getId().equals(optionDto.getId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, optionDto.getId()));

                option.setContent(optionDto.getContent());
                option.setCorrect(optionDto.isCorrect());
                option.setRelevance(optionDto.getRelevance());
            }
        }
    }

    public void addOption(Option option) {
        listOfOptions.add(option);
    }

    public List<Integer> getListOfCorrectOptionIds() {
        return this.getListOfOptions()
                .stream()
                .filter(Option::isCorrect)
                .sorted(Comparator.comparingInt(Option::getRelevance))
                .map(Option::getId)
                .collect(Collectors.toList());
    }

    public void update(MultipleChoiceQuestionDto questionDetails) {
        setListOfOptions(questionDetails.getListOfOptions());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return this.getCorrectAnswer()
                .stream()
                .map(MultipleChoiceQuestion::convertSequenceToLetter)
                .collect(Collectors.joining("|"));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitOptions(Visitor visitor) {
        for (Option option : this.getListOfOptions()) {
            option.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new MultipleChoiceCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new MultipleChoiceStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new MultipleChoiceQuestionDto(this);
    }

    public List<Integer> getCorrectAnswer() {
        List<Integer> correctAnswerSequence = this.getListOfOptions()
                .stream()
                .filter(Option::isCorrect)
                .sorted(Comparator.comparingInt(Option::getRelevance))
                .map(Option::getSequence)
                .collect(Collectors.toList());

        if(correctAnswerSequence.isEmpty())
            throw new TutorException(NO_CORRECT_OPTION);
        else
            return correctAnswerSequence;
    }

    @Override
    public void delete() {
        super.delete();
        for (Option option : this.listOfOptions) {
            option.remove();
        }
        this.listOfOptions.clear();
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestion{" +
                "listOfOptions=" + listOfOptions +
                '}';
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        var result = this.listOfOptions
                .stream()
                .filter(x -> selectedIds.contains(x.getId()))
                .map(x -> convertSequenceToLetter(x.getSequence()))
                .collect(Collectors.joining("|"));
        return !result.isEmpty() ? result : "-";
    }
}

package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;

public class OpenAnswerStatementAnswerDetailsDto extends StatementAnswerDetailsDto {

    private String answer;

    public OpenAnswerStatementAnswerDetailsDto() {
    }

    public OpenAnswerStatementAnswerDetailsDto(OpenAnswer questionAnswer) {
        if (questionAnswer.getAnswer() != null) {
            this.answer = questionAnswer.getAnswer();
        }
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    private OpenAnswer createdOpenAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        createdOpenAnswer = new OpenAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return createdOpenAnswer;
    }

    @Override
    public boolean emptyAnswer() {
        return answer == null;
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new OpenAnswerItem(username, quizId, statementAnswerDto, this);
    }

    @Override
    public void update(OpenAnswerQuestion question) {
        createdOpenAnswer.setAnswer(question, this);
    }

    @Override
    public String toString() {
        return "OpenAnswerStatementAnswerDetailsDto{" +
                "answer=" + answer +
                '}';
    }
}

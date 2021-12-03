package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;

public class OpenAnswerStatementQuestionDetailsDto extends StatementQuestionDetailsDto {

    // no inherent object to statement openAnswerDto

    public OpenAnswerStatementQuestionDetailsDto (OpenAnswerQuestion question) {
    }

    @Override
    public String toString() {
        return "OpenAnswerStatementQuestionDetailsDto{}";
    }
}
package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User


@DataJpaTest
class UpdateOpenAnswerQuestionTest extends SpockTest {
    def question
    def answer
    def user

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)


        given: 'a question'
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)

        and: 'one answer'
        answer = new String(OPEN_ANSWER_1_CONTENT);

        def questionDetails = new OpenAnswerQuestion()
        questionDetails.setAnswer(answer)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

    }

    def "update an question"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())

        and: 'a changed answer'
        def newAnswer = new String(OPEN_ANSWER_2_CONTENT)
        questionDto.getQuestionDetailsDto().setAnswer(newAnswer)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question changes"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'and these are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getKey() == 1
        and: 'the answer changes'
        result.getQuestionDetails().getAnswer() == OPEN_ANSWER_2_CONTENT
    }


    def "update with invalid answer"(){
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())

        and: 'an invalid answer'
        def newAnswer = new String(OPEN_ANSWER_INV_CONTENT)
        questionDto.getQuestionDetailsDto().setAnswer(newAnswer)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question changes"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'and these are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getKey() == 1
        and: 'exception is thrown'
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ANSWER_MUST_BE_PROVIDED

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

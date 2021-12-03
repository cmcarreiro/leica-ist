package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenAnswerStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto

@DataJpaTest
class ExportOpenAnswerQuizTest extends SpockTest {

    def quiz
    def creationDate
    def availableDate
    def conclusionDate

    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        def questionDetails = new OpenAnswerQuestionDto()
        questionDetails.setAnswer(OPEN_ANSWER_1_CONTENT)
        questionDto.setQuestionDetailsDto(questionDetails)
        questionDto = questionService.createQuestion(externalCourse.getId(), questionDto)

        def quizDto = new QuizDto()
        quizDto.setKey(1)
        quizDto.setScramble(false)
        quizDto.setQrCodeOnly(false)
        quizDto.setOneWay(false)
        quizDto.setTitle(QUIZ_TITLE)
        creationDate = DateHandler.now()
        availableDate = DateHandler.now()
        conclusionDate = DateHandler.now().plusDays(2)
        quizDto.setCreationDate(DateHandler.toISOString(creationDate))
        quizDto.setAvailableDate(DateHandler.toISOString(availableDate))
        quizDto.setConclusionDate(DateHandler.toISOString(conclusionDate))
        quizDto.setType(Quiz.QuizType.EXAM.toString())
        quiz = quizService.createQuiz(externalCourseExecution.getId(), quizDto)

        quizService.addQuestionToQuiz(questionDto.getId(), quiz.getId())

        def user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        def statementQuiz = answerService.startQuiz(user.getId(), quiz.getId())
        def statementAnswerDto = new StatementAnswerDto()
        def openAnswerDto = new OpenAnswerStatementAnswerDetailsDto()
        openAnswerDto.setAnswer(OPEN_ANSWER_1_CONTENT)
        statementAnswerDto.setAnswerDetails(openAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(statementQuiz.getQuizAnswerId())
        statementQuiz.getAnswers().add(statementAnswerDto)

        answerService.concludeQuiz(statementQuiz)
    }

    def 'export quizz'() {
        when:
        def exported = quizService.exportQuiz(quiz.getId())

        then:
        exported != null
    }


    def 'export quiz to xml'() {
        when:
        def quizzesXml = quizService.exportQuizzesToXml()

        then:
        quizzesXml != null
        print quizzesXml
      }

    def 'export quiz to latex'() {
        when:
        def quizzesLatex = quizService.exportQuizzesToLatex(quiz.getId())

        then:
        quizzesLatex != null
        print quizzesLatex
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

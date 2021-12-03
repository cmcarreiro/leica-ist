package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenAnswerStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcludeQuizOpenAnswerIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def user
    def question
    def questionDetails
    def response
    def quiz
    def quizQuestion
    def quizAnswer

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(user)
        userRepository.save(user)

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)
        questionDetails = new OpenAnswerQuestion()
        questionDetails.setAnswer(OPEN_ANSWER_1_CONTENT)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    def 'conclude quiz with no answer'() {
        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
        and: 'a user login'
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)


        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: "the request returns OK"
        response != null
        response.status == 200
        and: 'if it responds with the correct answers'
        def correctAnswers = response.data
        correctAnswers.size() == 1
        def correctAnswer = response.data.get(0)
        correctAnswer.sequence == 0
        def correctAnswerDetails = correctAnswer.correctAnswerDetails
        correctAnswerDetails.type == "open_answer"
        correctAnswerDetails.correctAnswer == OPEN_ANSWER_1_CONTENT
    }

    def 'conclude quiz with answer'() {
        given: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def OpenAnswerDto = new OpenAnswerStatementAnswerDetailsDto()
        OpenAnswerDto.setAnswer(OPEN_ANSWER_1_CONTENT)
        statementAnswerDto.setAnswerDetails(OpenAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
        and: 'a user login'
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: "the request returns OK"
        response != null
        response.status == 200
        and: 'if it responds with the correct answers'
        def correctAnswers = response.data
        correctAnswers.size() == 1
        def correctAnswer = response.data.get(0)
        correctAnswer.sequence == 0
        def correctAnswerDetails = correctAnswer.correctAnswerDetails
        correctAnswerDetails.type == "open_answer"
        correctAnswerDetails.correctAnswer == OPEN_ANSWER_1_CONTENT

        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getAnswerDetails().getAnswerRepresentation() == OPEN_ANSWER_1_CONTENT
}


    def 'unauthorized student tries to complete quiz'() {
        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
        and: 'a user login'
        def userKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(userKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(userKO.getId())
    }


    def 'unauthorized teacher tries to complete quiz'() {
        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
        and: 'a user login'
        def userKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        userKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(userKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(userKO.getId())
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(user.getId())
    }

}


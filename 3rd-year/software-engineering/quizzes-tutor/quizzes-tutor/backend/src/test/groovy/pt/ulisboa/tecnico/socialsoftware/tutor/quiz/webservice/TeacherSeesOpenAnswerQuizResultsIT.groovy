package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.webservice

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
class TeacherSeesOpenAnswerQuizResultsIT extends SpockTest {

    @LocalServerPort
    private int port

    def user
    def teacher
    def quiz
    def response
    def quizQuestion
    def quizAnswer
    def statementQuizDto

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

        def question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)
        def questionDetails = new OpenAnswerQuestion()
        questionDetails.setAnswer(OPEN_ANSWER_1_CONTENT)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def OpenAnswerDto = new OpenAnswerStatementAnswerDetailsDto()
        OpenAnswerDto.setAnswer(OPEN_ANSWER_1_CONTENT)
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setAnswerDetails(OpenAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
    }

    def "teacher sees concluded quiz results"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)
        and: 'a teacher login'
        teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(teacher)
        userRepository.save(teacher)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: 'the request returns OK'
        response != null
        response.status == 200
        and: 'quiz answer matches correct answer'
        def quizAnswers = response.data
        quizAnswers.quizAnswers.size() == 1

        def questionAnswers = quizAnswers.quizAnswers.get(0).questionAnswers
        questionAnswers.size() == 1
        def questionAnswer = questionAnswers.get(0)

        def answerDetails = questionAnswer.answerDetails
        def questionDetails = questionAnswer.question.questionDetailsDto
        answerDetails.answer == questionDetails.answer

        cleanup:
        userRepository.deleteById(teacher.getId())
    }

    def "unauthorized student tries to see concluded quiz results"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)
        and: 'an unauthorized teacher login'
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(student.getId())
    }

    def "unauthorized teacher tries to see concluded quiz results"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)
        and: 'an unauthorized teacher login'
        teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacher)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(teacher.getId())
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(user.getId())
    }
}

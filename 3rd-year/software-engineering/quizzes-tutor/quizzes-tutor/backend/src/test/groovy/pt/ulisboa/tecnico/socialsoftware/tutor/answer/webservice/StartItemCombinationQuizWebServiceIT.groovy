package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_START_QRCODE_QUIZ
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_ALREADY_COMPLETED
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_YET_AVAILABLE
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StartItemCombinationQuizWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port
    
    def course
    def courseExecution
    def student

    def quiz
    def question

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        student = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(courseExecution)
        userRepository.save(student)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        question = new Question()
        question.setKey(1)
        question.setCourse(course)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)
    }

    @Unroll
    def "returns statement and generates item combination quiz answer: quizType=#quizType | OneWay=#oneWay | qRCodeOnly=#qRCodeOnly | availableDate=#availableDate | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setCourseExecution(courseExecution)
        quiz.setOneWay(oneWay)
        quiz.setQrCodeOnly(qRCodeOnly)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(availableDate)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quizRepository.save(quiz)
        and: 'a quiz question'
        def quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        when:
        def response = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/start',
                requestContentType: 'application/json'
        )


        then: 'the return statement contains one quiz'
        response != null
        response.status ==200

        def statementQuiz = response.data
        statementQuiz.id != null
        statementQuiz.quizAnswerId != null
        statementQuiz.title == QUIZ_TITLE
        statementQuiz.oneWay == oneWay
        statementQuiz.timed == quizType.equals(Quiz.QuizType.IN_CLASS)
        statementQuiz.questions.size() == 1
        def question = statementQuiz.questions.get(0)
        question.content == content
        statementQuiz.answers.size() == 1
        def answer = statementQuiz.answers.get(0)
        answer.questionAnswerId != null
        answer.quizQuestionId == quizQuestion.id

        where:
        quizType                | oneWay | qRCodeOnly | availableDate     | conclusionDate      | resultsDate      || content
        Quiz.QuizType.PROPOSED  | true   | false      | LOCAL_DATE_BEFORE | null                | null             || null
        Quiz.QuizType.PROPOSED  | false  | false      | LOCAL_DATE_BEFORE | null                | null             || QUESTION_1_CONTENT
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | null             || null
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER || null
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | null             || QUESTION_1_CONTENT
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER || QUESTION_1_CONTENT
        Quiz.QuizType.GENERATED | false  | false      | LOCAL_DATE_BEFORE | null                | null             || QUESTION_1_CONTENT
    }

    @Unroll
    def "cannot start quiz and there is no item combination quiz answer: quizType=#quizType | OneWay=#oneWay | qRCodeOnly=#qRCodeOnly | availableDate=#availableDate | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setCourseExecution(courseExecution)
        quiz.setOneWay(oneWay)
        quiz.setQrCodeOnly(qRCodeOnly)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(availableDate)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quizRepository.save(quiz)
        and: 'a quiz question'
        def quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        when:
        def response = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/start',
                requestContentType: 'application/json'
        )

        then: 'an exception is thrown'
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == errorMessage.label

        where:
        quizType                | oneWay | qRCodeOnly | availableDate         | conclusionDate       | resultsDate      || errorMessage
        Quiz.QuizType.PROPOSED  | false  | false      | LOCAL_DATE_TOMORROW   | null                 | null             || QUIZ_NOT_YET_AVAILABLE
        Quiz.QuizType.PROPOSED  | false  | true       | LOCAL_DATE_YESTERDAY  | null                 | null             || CANNOT_START_QRCODE_QUIZ
        Quiz.QuizType.PROPOSED  | true   | false      | LOCAL_DATE_TOMORROW   | null                 | null             || QUIZ_NOT_YET_AVAILABLE
        Quiz.QuizType.PROPOSED  | true   | true       | LOCAL_DATE_YESTERDAY  | null                 | null             || CANNOT_START_QRCODE_QUIZ
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_TOMORROW   | LOCAL_DATE_LATER     | null             || QUIZ_NOT_YET_AVAILABLE
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_YESTERDAY | null             || QUIZ_NO_LONGER_AVAILABLE
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_YESTERDAY | LOCAL_DATE_LATER || QUIZ_NO_LONGER_AVAILABLE
        Quiz.QuizType.IN_CLASS  | false  | true       | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_TOMORROW  | null             || CANNOT_START_QRCODE_QUIZ
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_TOMORROW   | LOCAL_DATE_LATER     | null             || QUIZ_NOT_YET_AVAILABLE
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_YESTERDAY | null             || QUIZ_NO_LONGER_AVAILABLE
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_YESTERDAY | LOCAL_DATE_LATER || QUIZ_NO_LONGER_AVAILABLE
        Quiz.QuizType.IN_CLASS  | true   | true       | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_TOMORROW  | null             || CANNOT_START_QRCODE_QUIZ
    }

    @Unroll
    def "returns statement but item combination quiz answer already exists: quizType=#quizType | OneWay=#oneWay | qRCodeOnly=#qRCodeOnly | availableDate=#availableDate | conclusionDate=#conclusionDate | resultsDate=#resultsDate | creationDate=#creationDate"() {
        given: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setCourseExecution(courseExecution)
        quiz.setOneWay(oneWay)
        quiz.setQrCodeOnly(qRCodeOnly)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(availableDate)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quizRepository.save(quiz)
        and: 'a quiz question'
        def quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)
        and: 'a quiz answer'
        def quizAnswer = new QuizAnswer(student, quiz)
        quizAnswer.setCompleted(false)
        quizAnswer.setCreationDate(creationDate)
        quizAnswerRepository.save(quizAnswer)

        when:
        def response = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/start',
                requestContentType: 'application/json'
        )

        then: 'the return statement contains one quiz'
        response != null
        response.status ==200

        def statementQuiz = response.data
        statementQuiz.id != null
        statementQuiz.quizAnswerId != null
        statementQuiz.title == QUIZ_TITLE
        statementQuiz.oneWay == oneWay
        statementQuiz.timed == quizType.equals(Quiz.QuizType.IN_CLASS)
        statementQuiz.questions.size() == 1
        def question = statementQuiz.questions.get(0)
        question.content == content
        statementQuiz.answers.size() == 1
        def answer = statementQuiz.answers.get(0)
        answer.questionAnswerId != null
        answer.quizQuestionId == quizQuestion.id


        where:
        quizType                | oneWay | qRCodeOnly | availableDate     | conclusionDate      | resultsDate      | creationDate         || content
        Quiz.QuizType.PROPOSED  | true   | false      | LOCAL_DATE_BEFORE | null                | null             | null                 || null
        Quiz.QuizType.PROPOSED  | false  | false      | LOCAL_DATE_BEFORE | null                | null             | null                 || QUESTION_1_CONTENT
        Quiz.QuizType.PROPOSED  | false  | true       | LOCAL_DATE_BEFORE | null                | null             | null                 || QUESTION_1_CONTENT
        Quiz.QuizType.PROPOSED  | false  | true       | LOCAL_DATE_BEFORE | null                | null             | LOCAL_DATE_YESTERDAY || QUESTION_1_CONTENT
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | null             | null                 || null
        Quiz.QuizType.IN_CLASS  | true   | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER | null                 || null
        Quiz.QuizType.IN_CLASS  | true   | true       | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | null             | null                 || null
        Quiz.QuizType.IN_CLASS  | true   | true       | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER | null                 || null
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | null             | null                 || QUESTION_1_CONTENT
        Quiz.QuizType.IN_CLASS  | false  | false      | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER | null                 || QUESTION_1_CONTENT
        Quiz.QuizType.IN_CLASS  | false  | true       | LOCAL_DATE_BEFORE | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER | LOCAL_DATE_YESTERDAY || QUESTION_1_CONTENT
        Quiz.QuizType.GENERATED | false  | false      | LOCAL_DATE_BEFORE | null                | null             | LOCAL_DATE_YESTERDAY || QUESTION_1_CONTENT
    }

    @Unroll
    def "does not return returns statement and item combination quiz answer already exists: quizType=#quizType | OneWay=#oneWay | qRCodeOnly=#qRCodeOnly | availableDate=#availableDate | conclusionDate=#conclusionDate | resultsDate=#resultsDate | creationDate=#creationDate | completed=#completed"() {
        given: 'a quiz'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setCourseExecution(courseExecution)
        quiz.setOneWay(oneWay)
        quiz.setQrCodeOnly(qRCodeOnly)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(availableDate)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quizRepository.save(quiz)
        and: 'a quiz question'
        def quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)
        and: 'a quiz answer'
        def quizAnswer = new QuizAnswer(student, quiz)
        quizAnswer.setCompleted(completed)
        quizAnswer.setCreationDate(creationDate)
        quizAnswerRepository.save(quizAnswer)

        when:
        def response = restClient.get(
                path: '/quizzes/'+ quiz.getId() +'/start',
                requestContentType: 'application/json'
        )

        then: 'an exception is thrown'
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == errorMessage.label
        

        where:
        quizType                 | oneWay | qRCodeOnly | availableDate         | conclusionDate       | resultsDate      | creationDate         | completed || errorMessage
        Quiz.QuizType.PROPOSED   | false  | false      | LOCAL_DATE_BEFORE     | null                 | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.PROPOSED   | false  | true       | LOCAL_DATE_YESTERDAY  | null                 | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.PROPOSED   | true   | false      | LOCAL_DATE_YESTERDAY  | null                 | null             | LOCAL_DATE_YESTERDAY | false     || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.PROPOSED   | true   | false      | LOCAL_DATE_YESTERDAY  | null                 | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.PROPOSED   | true   | true       | LOCAL_DATE_YESTERDAY  | null                 | null             | LOCAL_DATE_YESTERDAY | false     || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.PROPOSED   | true   | true       | LOCAL_DATE_YESTERDAY  | null                 | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | false  | false      | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_LATER     | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | false  | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | false  | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | LOCAL_DATE_LATER | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | false  | true       | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_TOMORROW  | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | false      | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_LATER     | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | false      | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_LATER     | null             | LOCAL_DATE_YESTERDAY | false     || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | null             | LOCAL_DATE_YESTERDAY | false     || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | LOCAL_DATE_LATER | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | false      | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | LOCAL_DATE_LATER | LOCAL_DATE_YESTERDAY | false     || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | true       | LOCAL_DATE_YESTERDAY  | LOCAL_DATE_TOMORROW  | null             | LOCAL_DATE_YESTERDAY | true      || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.IN_CLASS   | true   | true       | LOCAL_DATE_BEFORE     | LOCAL_DATE_TOMORROW  | null             | LOCAL_DATE_YESTERDAY | false     || QUIZ_ALREADY_COMPLETED
        Quiz.QuizType.GENERATED  | false  | false      | LOCAL_DATE_YESTERDAY  | null                 | null             | LOCAL_DATE_TODAY     | true      || QUIZ_ALREADY_COMPLETED
    }

    def cleanup() {

        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())

        persistentCourseCleanup()
    }
}

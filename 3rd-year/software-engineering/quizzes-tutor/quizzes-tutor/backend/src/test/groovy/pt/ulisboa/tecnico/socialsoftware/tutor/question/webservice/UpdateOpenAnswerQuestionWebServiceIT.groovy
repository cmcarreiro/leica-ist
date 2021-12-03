package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateOpenAnswerQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def questionDto
    def question
    def response

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setAnswer(OPEN_ANSWER_1_CONTENT)

        questionService.createQuestion(courseExecution.getId(), questionDto)
        question = questionRepository.findAll().get(0)

    }

    def "edit open answer question"() {
        given: "a teacher"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: "a openAnswerQuestionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())
        questionDto.setQuestionDetailsDto(question.getQuestionDetailsDto())
        questionDto.getQuestionDetailsDto().setAnswer(OPEN_ANSWER_2_CONTENT)

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the updated question"
        def openAnswerQuestion = response.data
        openAnswerQuestion.id != null
        openAnswerQuestion.status == questionDto.getStatus()
        openAnswerQuestion.title == questionDto.getTitle()
        openAnswerQuestion.content == questionDto.getContent()
        openAnswerQuestion.status == Question.Status.SUBMITTED.name()
        openAnswerQuestion.questionDetailsDto != null
        openAnswerQuestion.questionDetailsDto.answer == questionDto.getQuestionDetailsDto().getAnswer()

    }


    def "teacher with no access to course execution updates an open answer question"() {
        given: "a teacher"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a openAnswerQuestionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())
        questionDto.setQuestionDetailsDto(question.getQuestionDetailsDto())
        questionDto.getQuestionDetailsDto().setAnswer(OPEN_ANSWER_2_CONTENT)

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(teacherKO.getId())
    }


    def "student updates an open answer question"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a openAnswerQuestionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())
        questionDto.setQuestionDetailsDto(question.getQuestionDetailsDto())
        questionDto.getQuestionDetailsDto().setAnswer(OPEN_ANSWER_2_CONTENT)

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(student.getId())
    }


    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}



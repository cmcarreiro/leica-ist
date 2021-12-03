package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportItemCombinationQuestionsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setGroupId(1)
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setCombinations(combinations)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionService.createQuestion(courseExecution.getId(), questionDto)
    }

    def "teacher exports question"() {
        given: "prepare request response"
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "export question is called"
        def map = restClient.get(
            path: '/courses/' + courseExecution.getId() + '/questions/export',
            requestContentType: "application/zip"
        )

        then: "check that response status is 200"
        map.response.status == 200
        map.reader != null
    }


    def "teacher without access to course execution tries to export question"() {
        given: "a teacher without access to the course execution"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when: "export question is called"
        def response = restClient.get(
            path: '/courses/' + courseExecution.getId() + '/questions/export',
            requestContentType: "application/zip"
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label
        userRepository.deleteById(teacherKO.getId())
    }

    def "student tries to export question"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when: "export question is called"
        def response = restClient.get(
            path: '/courses/' + courseExecution.getId() + '/questions/export',
            requestContentType: "application/zip"
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label
        userRepository.deleteById(student.getId())
    }


    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}
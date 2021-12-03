package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportQuestionsWebServiceIT extends SpockTest {
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
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())

        def optionDto1 = new OptionDto()
        optionDto1.setContent(OPTION_1_CONTENT)
        optionDto1.setCorrect(false)
        optionDto1.setRelevance(0)

        def optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_2_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setRelevance(2)

        def optionDto3 = new OptionDto()
        optionDto3.setContent(OPTION_3_CONTENT)
        optionDto3.setCorrect(true)
        optionDto3.setRelevance(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto1)
        options.add(optionDto2)
        options.add(optionDto3)
        questionDto.getQuestionDetailsDto().setListOfOptions(options)

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

        userRepository.deleteById(teacherKO.getId())
    }

    def "student tries to update question"() {
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

        userRepository.deleteById(student.getId())
    }


    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}


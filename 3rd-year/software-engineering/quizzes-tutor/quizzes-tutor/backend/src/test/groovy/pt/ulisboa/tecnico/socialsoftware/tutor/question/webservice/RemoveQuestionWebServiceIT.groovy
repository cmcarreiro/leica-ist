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
class RemoveQuestionWebServiceIT extends SpockTest {
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

        
    }

    def "teacher removes question"() {
        given: "a question"
        def questionDtoOne = new QuestionDto()
        questionDtoOne.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDtoOne.setTitle(QUESTION_1_TITLE)
        questionDtoOne.setContent(QUESTION_1_CONTENT)
        questionDtoOne.setStatus(Question.Status.SUBMITTED.name())

        def optionDtoOne = new OptionDto()
        optionDtoOne.setContent(OPTION_1_CONTENT)
        optionDtoOne.setCorrect(true)
        optionDtoOne.setRelevance(0)

        def optionsOne = new ArrayList<OptionDto>()
        optionsOne.add(optionDtoOne)
        questionDtoOne.getQuestionDetailsDto().setListOfOptions(optionsOne)

        questionService.createQuestion(courseExecution.getId(), questionDtoOne)
        def questionOne = questionRepository.findAll().get(0)

        when: "remove question is called"
        def response = restClient.delete(
                path: '/questions/' + questionOne.getId()
        )

        then: "check that response status is 200"
        response != null
        response.status == 200

        questionRepository.findAll().size() == 0

    }

    def "teacher without access to course execution tries to delete question"() {
        given: "a question"
        def questionDtoTwo = new QuestionDto()
        questionDtoTwo.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDtoTwo.setTitle(QUESTION_1_TITLE)
        questionDtoTwo.setContent(QUESTION_1_CONTENT)
        questionDtoTwo.setStatus(Question.Status.SUBMITTED.name())

        def optionDtoTwo = new OptionDto()
        optionDtoTwo.setContent(OPTION_1_CONTENT)
        optionDtoTwo.setCorrect(true)
        optionDtoTwo.setRelevance(0)

        def optionsTwo = new ArrayList<OptionDto>()
        optionsTwo.add(optionDtoTwo)
        questionDtoTwo.getQuestionDetailsDto().setListOfOptions(optionsTwo)

        questionService.createQuestion(courseExecution.getId(), questionDtoTwo)
        def questionTwo = questionRepository.findAll().get(0)


        and: "a teacher without access to the course execution"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when: "delete question is called"
        def response = restClient.delete(
                path: '/questions/' + questionTwo.getId()
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403   

        userRepository.delete(userRepository.findById(teacherKO.getId()).get())
    }

    def "student tries to remove a question"() {
        given: "a question"
        def questionDtoThree = new QuestionDto()
        questionDtoThree.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDtoThree.setTitle(QUESTION_1_TITLE)
        questionDtoThree.setContent(QUESTION_1_CONTENT)
        questionDtoThree.setStatus(Question.Status.SUBMITTED.name())

        def optionDtoThree = new OptionDto()
        optionDtoThree.setContent(OPTION_1_CONTENT)
        optionDtoThree.setCorrect(true)
        optionDtoThree.setRelevance(0)

        def optionsThree = new ArrayList<OptionDto>()
        optionsThree.add(optionDtoThree)
        questionDtoThree.getQuestionDetailsDto().setListOfOptions(optionsThree)

        questionService.createQuestion(courseExecution.getId(), questionDtoThree)
        def questionThree = questionRepository.findAll().get(0)

        and: "a student"
        def student = new User(USER_3_NAME, USER_3_EMAIL, USER_3_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        createdUserLogin(USER_3_EMAIL, USER_1_PASSWORD)

        when: "remove question is called"
        def response = restClient.delete(
                path: '/questions/' + questionThree.getId()
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        userRepository.delete(userRepository.findById(student.getId()).get())

    }

    def cleanup() {
        persistentCourseCleanup()

        userRepository.delete(userRepository.findById(teacher.getId()).get())

        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}


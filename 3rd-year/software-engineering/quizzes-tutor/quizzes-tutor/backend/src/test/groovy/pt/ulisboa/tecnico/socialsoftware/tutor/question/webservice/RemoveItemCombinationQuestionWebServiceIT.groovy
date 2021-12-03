package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

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
class RemoveItemCombinationQuestionWebServiceIT extends SpockTest {
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
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        def itemDto1 = new ItemDto()
        itemDto1.setGroupId(1)
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())
        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setCombinations(combinations)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionService.createQuestion(courseExecution.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: "remove question is called"
        def response = restClient.delete(
                path: '/questions/' + question.getId()
        )

        then: "check that response status is 200"
        response != null
        response.status == 200

        questionRepository.findAll().size() == 0
        questionDetailsRepository.findAll().size()  == 0
        itemRepository.findAll().size() == 0

    }

    def "teacher without access to course execution tries to delete question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        def itemDto1 = new ItemDto()
        itemDto1.setGroupId(1)
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())
        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setCombinations(combinations)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionService.createQuestion(courseExecution.getId(), questionDto)
        def question = questionRepository.findAll().get(0)


        and: "a teacher without access to the course execution"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when: "delete question is called"
        def response = restClient.delete(
                path: '/questions/' + question.getId()
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label

        userRepository.delete(userRepository.findById(teacherKO.getId()).get())
    }

    def "student tries to remove a question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        def itemDto1 = new ItemDto()
        itemDto1.setGroupId(1)
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())
        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setCombinations(combinations)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionService.createQuestion(courseExecution.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        and: "a student"
        def student = new User(USER_3_NAME, USER_3_EMAIL, USER_3_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        createdUserLogin(USER_3_EMAIL, USER_1_PASSWORD)

        when: "remove question is called"
        def response = restClient.delete(
                path: '/questions/' + question.getId()
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label

        userRepository.delete(userRepository.findById(student.getId()).get())

    }

    def cleanup() {
        persistentCourseCleanup()

        userRepository.delete(userRepository.findById(teacher.getId()).get())

        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}

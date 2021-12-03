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
class CreateItemCombinationQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def response

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
    }

    def "create question submission for course execution"() {
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

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct question"
        def question = response.data
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()
        question.status == Question.Status.AVAILABLE.name()
        question.questionDetailsDto.type == "item_combination"
        question.questionDetailsDto.items.size() == 2
        question.questionDetailsDto.items[0].id != null
        question.questionDetailsDto.items[0].groupId == 1
        question.questionDetailsDto.items[0].sequence == itemDto1.getSequence()
        question.questionDetailsDto.items[0].combinations.size() == 0
        question.questionDetailsDto.items[0].content == itemDto1.getContent()

        question.questionDetailsDto.items[1].id != null
        question.questionDetailsDto.items[1].groupId == 2
        question.questionDetailsDto.items[1].sequence == itemDto2.getSequence()
        question.questionDetailsDto.items[1].combinations.size() == 1
        question.questionDetailsDto.items[1].combinations[0] == itemDto2.getCombinations().get(0)
        question.questionDetailsDto.items[1].content == itemDto2.getContent()

        questionRepository.count() == 1

    }

    def "create question submission without items in groupOne for course execution"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(0)
        def items = new ArrayList<ItemDto>()
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 400"
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == ErrorMessage.AT_LEAST_ONE_ITEM_IN_GROUP_ONE_NEEDED.label

    }

    def "create question submission without items in groupTwo for course execution"() {
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
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 400"
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == ErrorMessage.AT_LEAST_ONE_ITEM_IN_GROUP_TWO_NEEDED.label

    }

    def "create question submission with no correct combination for course execution"() {
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

        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(0)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 400"
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == ErrorMessage.NO_CORRECT_ITEM_COMBINATION.label

    }

    def "teacher with no access to course execution tries to create question"() {
        given: "a teacher with no access to the course execution"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)

        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a questionDto"
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
        itemDto2.setSequence(0)
        itemDto2.setCombinations(combinations)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label

        userRepository.deleteById(teacherKO.getId())
    }

    def "student tries to create question"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setGroupId(1)
        itemDto1.setSequence(0)
        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())
        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(0)
        itemDto2.setCombinations(combinations)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.post(
                path: '/courses/' + courseExecution.getId() + '/questions',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
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
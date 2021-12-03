package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovy.json.JsonOutput   
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
class UpdateItemCombinationQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def response
    def question

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(2)
        itemDto2.setCombinations(combinations)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionService.createQuestion(courseExecution.getId(), questionDto)
        question = questionRepository.findAll().get(0)
    }

    def "update question submission for course execution"() {
        given: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_2_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_1_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(2)
        itemDto2.setCombinations(combinations)

        def itemDto3 = new ItemDto()
        itemDto3.setContent(ITEM_3_CONTENT)
        itemDto3.setSequence(2)
        itemDto3.setGroupId(2)
        itemDto3.setCombinations(combinations)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        items.add(itemDto3)
        updatedQuestionDto.getQuestionDetailsDto().setItems(items)

        when: "update question is called"
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct question"
        def questionResponse = response.data
        questionResponse.id != null
        questionResponse.title == updatedQuestionDto.getTitle()
        questionResponse.content == updatedQuestionDto.getContent()
        questionResponse.status == Question.Status.SUBMITTED.name()
        questionResponse.questionDetailsDto.type == "item_combination"

        questionResponse.questionDetailsDto.items[0].sequence == itemDto1.getSequence()
        questionResponse.questionDetailsDto.items[0].combinations.size() == 0
        questionResponse.questionDetailsDto.items[0].content == itemDto1.getContent()
        questionResponse.questionDetailsDto.items[0].groupId == 1

        questionResponse.questionDetailsDto.items[1].sequence == itemDto2.getSequence()
        questionResponse.questionDetailsDto.items[1].combinations.size() == 1
        questionResponse.questionDetailsDto.items[1].content == itemDto2.getContent()
        questionResponse.questionDetailsDto.items[1].groupId == 2

        questionResponse.questionDetailsDto.items[2].sequence == itemDto3.getSequence()
        questionResponse.questionDetailsDto.items[2].combinations.size() == 1
        questionResponse.questionDetailsDto.items[2].content == itemDto3.getContent()
        questionResponse.questionDetailsDto.items[2].groupId == 2

        questionResponse.questionDetailsDto.items.size() == 3

    }

    def "update question submission without items in groupTwo for course execution"() {
        given: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(1)

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(1)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        updatedQuestionDto.getQuestionDetailsDto().setItems(items)

        when: "update question is called"
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 400"
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == ErrorMessage.AT_LEAST_ONE_ITEM_IN_GROUP_TWO_NEEDED.label

    }

    def "update question submission without items in groupOne for course execution"() {
        given: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(2)

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(2)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        updatedQuestionDto.getQuestionDetailsDto().setItems(items)

        when: "update question is called"
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 400"
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == ErrorMessage.AT_LEAST_ONE_ITEM_IN_GROUP_ONE_NEEDED.label

    }

    def "update question submission without correct combinations for course execution"() {
        given: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_1_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(1)

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(2)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        updatedQuestionDto.getQuestionDetailsDto().setItems(items)

        when: "update question is called"
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 400"
        def exception = thrown(HttpResponseException)
        exception.response.status == 400
        exception.response.data.message == ErrorMessage.NO_CORRECT_ITEM_COMBINATION.label

    }

    def "teacher with no access to course execution tries to update a question"() {
        given: "a teacher with no access to the course execution"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)

        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_2_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_1_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(2)
        itemDto2.setCombinations(combinations)

        def itemDto3 = new ItemDto()
        itemDto3.setContent(ITEM_3_CONTENT)
        itemDto3.setSequence(2)
        itemDto3.setGroupId(2)
        itemDto3.setCombinations(combinations)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        items.add(itemDto3)
        updatedQuestionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label

        userRepository.deleteById(teacherKO.getId())
    }

    def "student tries to update a question"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        def itemDto1 = new ItemDto()
        itemDto1.setContent(ITEM_2_CONTENT)
        itemDto1.setSequence(0)
        itemDto1.setGroupId(1)

        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setContent(ITEM_1_CONTENT)
        itemDto2.setSequence(1)
        itemDto2.setGroupId(2)
        itemDto2.setCombinations(combinations)

        def itemDto3 = new ItemDto()
        itemDto3.setContent(ITEM_3_CONTENT)
        itemDto3.setSequence(2)
        itemDto3.setGroupId(2)
        itemDto3.setCombinations(combinations)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        items.add(itemDto3)
        updatedQuestionDto.getQuestionDetailsDto().setItems(items)

        when:
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(updatedQuestionDto),
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
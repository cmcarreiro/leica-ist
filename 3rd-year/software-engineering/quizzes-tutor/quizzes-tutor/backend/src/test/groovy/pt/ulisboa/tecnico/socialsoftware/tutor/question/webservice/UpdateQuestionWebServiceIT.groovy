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
class UpdateQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def question

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
        optionDto1.setCorrect(true)
        optionDto1.setRelevance(0)

        def optionDto2 = new OptionDto()
        optionDto2.setContent(OPTION_2_CONTENT)
        optionDto2.setCorrect(false)
        optionDto2.setRelevance(0)

        def optionDto3 = new OptionDto()
        optionDto3.setContent(OPTION_3_CONTENT)
        optionDto3.setCorrect(false)
        optionDto3.setRelevance(0)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto1)
        options.add(optionDto2)
        options.add(optionDto3)
        questionDto.getQuestionDetailsDto().setListOfOptions(options)

        questionService.createQuestion(courseExecution.getId(), questionDto)
        question = questionRepository.findAll().get(0)
    }

    def "teacher updates question"() {
        given: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.SUBMITTED.name())

        updatedQuestionDto.getQuestionDetailsDto().setListOfOptions(question.getQuestionDetailsDto().getListOfOptions())

        def optionDto1 = question.getQuestionDetailsDto().getListOfOptions()[0]
        optionDto1.setContent(OPTION_2_CONTENT)
        optionDto1.setCorrect(false)

        def optionDto2 = question.getQuestionDetailsDto().getListOfOptions()[1]
        optionDto2.setContent(OPTION_3_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setRelevance(2)

        def optionDto3 = question.getQuestionDetailsDto().getListOfOptions()[2]
        optionDto3.setContent(OPTION_4_CONTENT)
        optionDto3.setCorrect(true)
        optionDto3.setRelevance(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto1)
        options.add(optionDto2)
        options.add(optionDto3)
        updatedQuestionDto.getQuestionDetailsDto().setListOfOptions(options)

        when: "update question is called"
        def response = restClient.put(
                path: '/questions/' + question.getId(),
                body: JsonOutput.toJson(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 200"
        response != null
        response.status == 200

        and: "it responds with the correct question"
        def questionDtoResponse = response.data
        
        questionDtoResponse.id == question.getId()
        questionDtoResponse.title == QUESTION_2_TITLE
        questionDtoResponse.content == QUESTION_2_CONTENT
        questionDtoResponse.status == Question.Status.SUBMITTED.name()

        and: "with the right question details"
        def questionDetailsDtoResponse = questionDtoResponse.questionDetailsDto
        questionDetailsDtoResponse.type == "multiple_choice"

        and: "with the right option details"
        def optionDtoResponse1 = questionDetailsDtoResponse.listOfOptions[0]
        optionDtoResponse1.correct == false
        optionDtoResponse1.content == OPTION_2_CONTENT
        optionDtoResponse1.relevance == 0

        def optionDtoResponse2 = questionDetailsDtoResponse.listOfOptions[1]
        optionDtoResponse2.correct == true
        optionDtoResponse2.content == OPTION_3_CONTENT
        optionDtoResponse2.relevance == 2
        
        def optionDtoResponse3 = questionDetailsDtoResponse.listOfOptions[2]
        optionDtoResponse3.correct == true
        optionDtoResponse3.content == OPTION_4_CONTENT
        optionDtoResponse3.relevance == 1
    }

    def "teacher without access to course execution tries to update question"() {
        given: "a teacher without access to the course execution"
        def teacherKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacherKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacherKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.SUBMITTED.name())

        updatedQuestionDto.getQuestionDetailsDto().setListOfOptions(question.getQuestionDetailsDto().getListOfOptions())

        def optionDto1 = question.getQuestionDetailsDto().getListOfOptions()[0]
        optionDto1.setContent(OPTION_2_CONTENT)
        optionDto1.setCorrect(false)

        def optionDto2 = question.getQuestionDetailsDto().getListOfOptions()[1]
        optionDto2.setContent(OPTION_3_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setRelevance(2)

        def optionDto3 = question.getQuestionDetailsDto().getListOfOptions()[2]
        optionDto3.setContent(OPTION_4_CONTENT)
        optionDto3.setCorrect(true)
        optionDto3.setRelevance(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto1)
        options.add(optionDto2)
        options.add(optionDto3)
        updatedQuestionDto.getQuestionDetailsDto().setListOfOptions(options)

        when: "update question is called"
        def response = restClient.put(
                path: '/questions/' + question.getId(),
                body: JsonOutput.toJson(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

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

        and: "an updated questionDto"
        def updatedQuestionDto = new QuestionDto()
        updatedQuestionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        updatedQuestionDto.setTitle(QUESTION_2_TITLE)
        updatedQuestionDto.setContent(QUESTION_2_CONTENT)
        updatedQuestionDto.setStatus(Question.Status.SUBMITTED.name())

        updatedQuestionDto.getQuestionDetailsDto().setListOfOptions(question.getQuestionDetailsDto().getListOfOptions())

        def optionDto1 = question.getQuestionDetailsDto().getListOfOptions()[0]
        optionDto1.setContent(OPTION_2_CONTENT)
        optionDto1.setCorrect(false)

        def optionDto2 = question.getQuestionDetailsDto().getListOfOptions()[1]
        optionDto2.setContent(OPTION_3_CONTENT)
        optionDto2.setCorrect(true)
        optionDto2.setRelevance(2)

        def optionDto3 = question.getQuestionDetailsDto().getListOfOptions()[2]
        optionDto3.setContent(OPTION_4_CONTENT)
        optionDto3.setCorrect(true)
        optionDto3.setRelevance(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto1)
        options.add(optionDto2)
        options.add(optionDto3)
        updatedQuestionDto.getQuestionDetailsDto().setListOfOptions(options)

        when: "update question is called"
        def response = restClient.put(
                path: '/questions/' + question.getId(),
                body: JsonOutput.toJson(updatedQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check that response status is 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        userRepository.deleteById(student.getId())
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}


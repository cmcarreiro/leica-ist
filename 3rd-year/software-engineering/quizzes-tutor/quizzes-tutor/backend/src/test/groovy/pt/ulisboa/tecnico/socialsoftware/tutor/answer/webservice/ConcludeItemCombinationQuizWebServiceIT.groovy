package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto

import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

import java.util.Set;
import java.util.HashSet;

import java.util.List;
import java.util.ArrayList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcludeItemCombinationQuizWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def student

    def quizQuestion
    def item1
    def item2
    def item3
    def combinations
    def quizAnswer
    def date
    def quiz

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

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(courseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        def question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        item1 = new Item()
        item1.setGroupId(1)
        item1.setContent(ITEM_1_CONTENT)
        item1.setSequence(0)
        item1.setQuestionDetails(questionDetails)
        itemRepository.save(item1)

        item2 = new Item()
        item2.setGroupId(1)
        item2.setContent(ITEM_2_CONTENT)
        item2.setSequence(1)
        item2.setQuestionDetails(questionDetails)
        itemRepository.save(item2)

        combinations = new ArrayList<>()
        combinations.add(item1.getContent())
        combinations.add(item2.getContent())
        
        item3 = new Item()
        item3.setGroupId(2)
        item3.setContent(ITEM_3_CONTENT)
        item3.setSequence(3)
        item3.setCombinations(combinations)
        item3.setQuestionDetails(questionDetails)
        itemRepository.save(item3)

        date = DateHandler.now()

        quizAnswer = new QuizAnswer(student, quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    def 'conclude item combination question quiz without conclusionDate, without answering'() {
               given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/quizzes/'+ quiz.getId() +'/conclude',
                body: mapper.writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: 'the value is createQuestion and persistent'
        questionAnswerRepository.findAll().size() == 1
        and: 'the return value is OK'
        response != null
        response.status == 200
        and: "if it responds with the correct answer"
        response.data.id != null
        def correctAnswer = response.data[0]
        correctAnswer.sequence == 0
        def correctItems = correctAnswer.correctAnswerDetails.correctItems
        correctItems[0].itemId != 0
        correctItems[0].combinations == item1.getCombinations()
        
        correctItems[1].itemId != 0
        correctItems[1].combinations == item2.getCombinations()
        
        correctItems[2].itemId != 0
        correctItems[2].combinations.sort() == item3.getCombinations()
    }

     def 'conclude item combination question quiz with answer, before conclusionDate'() {
        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))
        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        def statementItem1Dto = new ItemStatementAnswerDetailsDto(item1.getId(), new HashSet<>())
        def statementItem2Dto = new ItemStatementAnswerDetailsDto(item2.getId(), new HashSet<>())
        def statementItem3Dto = new ItemStatementAnswerDetailsDto(item3.getId(), new HashSet<>(combinations))
        def itemStatements = new ArrayList<ItemStatementAnswerDetailsDto>()
        itemStatements.add(statementItem1Dto)
        itemStatements.add(statementItem2Dto)
        itemStatements.add(statementItem3Dto)

        itemCombinationAnswerDto.setItemStatements(itemStatements)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/quizzes/'+ quiz.getId() +'/conclude',
                body: mapper.writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: 'the value is createQuestion and persistent'
        questionAnswerRepository.findAll().size() == 1

        and: 'the return value is OK'
        response != null
        response.status == 200

        and: "if it responds with the correct answer"
        response.data.id != null
        def correctAnswer = response.data[0]
        correctAnswer.sequence == 0
        def correctItems = correctAnswer.correctAnswerDetails.correctItems
        correctItems[0].itemId != 0
        correctItems[0].combinations == item1.getCombinations()
        
        correctItems[1].itemId != 0
        correctItems[1].combinations == item2.getCombinations()
        
        correctItems[2].itemId != 0
        correctItems[2].combinations.sort() == item3.getCombinations()
    }

    def 'unauthorized student tries to complete item combination quiz'() {
        def studentKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        studentKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(studentKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        def quizAnswerKO = new QuizAnswer(studentKO, quiz)
        quizAnswerRepository.save(quizAnswerKO)

        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))
        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswerKO.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        def statementItem1Dto = new ItemStatementAnswerDetailsDto(item1.getId(), new HashSet<>())
        def statementItem2Dto = new ItemStatementAnswerDetailsDto(item2.getId(), new HashSet<>())
        def statementItem3Dto = new ItemStatementAnswerDetailsDto(item3.getId(), new HashSet<>(combinations))
        def itemStatements = new ArrayList<ItemStatementAnswerDetailsDto>()
        itemStatements.add(statementItem1Dto)
        itemStatements.add(statementItem2Dto)
        itemStatements.add(statementItem3Dto)

        itemCombinationAnswerDto.setItemStatements(itemStatements)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswerKO.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/quizzes/'+ quiz.getId() +'/conclude',
                body: mapper.writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: 'the value is createQuestion and persistent'
        questionAnswerRepository.findAll().size() == 2

        and: 'check that response status is 403'
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label

        cleanup:
        quizAnswerRepository.deleteById(quizAnswerKO.getId())
        userRepository.deleteById(studentKO.getId())
    }

    def 'unauthorized teacher tries to complete item combination quiz'() {
        def teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacher)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        def quizAnswerKO = new QuizAnswer(teacher, quiz)
        quizAnswerRepository.save(quizAnswerKO)

        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))
        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswerKO.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        def statementItem1Dto = new ItemStatementAnswerDetailsDto(item1.getId(), new HashSet<>())
        def statementItem2Dto = new ItemStatementAnswerDetailsDto(item2.getId(), new HashSet<>())
        def statementItem3Dto = new ItemStatementAnswerDetailsDto(item3.getId(), new HashSet<>(combinations))
        def itemStatements = new ArrayList<ItemStatementAnswerDetailsDto>()
        itemStatements.add(statementItem1Dto)
        itemStatements.add(statementItem2Dto)
        itemStatements.add(statementItem3Dto)

        itemCombinationAnswerDto.setItemStatements(itemStatements)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswerKO.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/quizzes/'+ quiz.getId() +'/conclude',
                body: mapper.writeValueAsString(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: 'the value is createQuestion and persistent'
        questionAnswerRepository.findAll().size() == 2

        and: 'check that response status is 403'
        def exception = thrown(HttpResponseException)
        exception.response.status == 403
        exception.response.data.message == ErrorMessage.ACCESS_DENIED.label

        cleanup:
        quizAnswerRepository.deleteById(quizAnswerKO.getId())
        userRepository.deleteById(teacher.getId())
    }


    def cleanup() {

        quizAnswerRepository.deleteById(quizAnswer.getId())

        persistentCourseCleanup()

        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())

        courseRepository.deleteById(course.getId())
    }

}
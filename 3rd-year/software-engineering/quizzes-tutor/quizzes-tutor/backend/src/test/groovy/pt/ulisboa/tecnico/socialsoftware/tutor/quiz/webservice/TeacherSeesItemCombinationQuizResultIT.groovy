package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TeacherSeesItemCombinationQuizResultsIT extends SpockTest {

    @LocalServerPort
    private int port

    def user
    def teacher
    def quiz
    def response
    def quizQuestion
    def quizAnswer
    def statementQuizDto

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(user)
        userRepository.save(user)

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
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

        def item1 = new Item()
        item1.setGroupId(1)
        item1.setContent(ITEM_1_CONTENT)
        item1.setSequence(0)
        item1.setQuestionDetails(questionDetails)
        itemRepository.save(item1)


        def combinations = new ArrayList<String>()
        combinations.add(item1.getContent())
        
        def item2 = new Item()
        item2.setGroupId(2)
        item2.setContent(ITEM_3_CONTENT)
        item2.setSequence(3)
        item2.setCombinations(combinations)
        item2.setQuestionDetails(questionDetails)
        item2.setCombinations(combinations)
        itemRepository.save(item2)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
      
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()

        def itemAnswerDto1 = new ItemStatementAnswerDetailsDto(item1.getId(), new HashSet<>())
        def itemAnswerDto2 = new ItemStatementAnswerDetailsDto(item2.getId(), new HashSet<>(combinations))

        def itemAnswers = new ArrayList<>()
        itemAnswers.add(itemAnswerDto1)
        itemAnswers.add(itemAnswerDto2)
        itemCombinationAnswerDto.setItemStatements(itemAnswers)
        
        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
    }

    def "teacher sees concluded quiz results"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)
        and: 'a teacher login'
        teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(teacher)
        userRepository.save(teacher)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: 'the request returns OK'
        response != null
        response.status == 200
        and: 'quiz answer matches correct answer'
        def quizAnswers = response.data

        quizAnswers.quizAnswers.size() == 1

        def questionAnswers = quizAnswers.quizAnswers.get(0).questionAnswers
        questionAnswers.size() == 1
        def questionAnswer = questionAnswers.get(0)


        def answerDto = questionAnswer.answerDetails
        def questionDto = questionAnswer.question.questionDetailsDto

        answerDto.itemAnswers.size() == 2
        questionDto.items.size() == 2

        for(int i=0; i < 2; i++){
            if( answerDto.itemAnswers[i].itemId == questionDto.items[0].id){
                answerDto.itemAnswers[i].combinations == new HashSet<>(questionDto.items[0].combinations)
            }
            else if( answerDto.itemAnswers[i].itemId == questionDto.items[1].id){
                answerDto.itemAnswers[i].combinations == new HashSet<>(questionDto.items[1].combinations)
            }
            else {
                assert false
            }
        }
        

        cleanup:
        userRepository.deleteById(teacher.getId())
    }

    def "unauthorized student tries to see concluded quiz results"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)
        and: 'an unauthorized teacher login'
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(student)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(student.getId())
    }

    def "unauthorized teacher tries to see concluded quiz results"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)
        and: 'an unauthorized teacher login'
        teacher = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(teacher)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.get(
                path: '/quizzes/' + quiz.getId() + '/answers',
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(teacher.getId())
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(user.getId())
    }
}
package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.dto.CourseExecutionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import spock.lang.Unroll

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetSolvedItemCombinationQuizzesWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def student

    def question
    def item1
    def item2
    def item3
    def combinations
    def quiz
    def quizQuestion

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

        question = new Question()
        question.setKey(1)
        question.setCourse(course)
        question.setContent("Question Content")
        question.setTitle("Question Title")

        def questionDetails = new ItemCombinationQuestion();
        question.setQuestionDetails(questionDetails);
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

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
        item3.setCombinations(combinations)
        itemRepository.save(item3)

    }

    @Unroll
    def "returns solved quiz with: quizType=#quizType | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quiz.setCourseExecution(courseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(student)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)

        def answerDetails = new ItemCombinationAnswer(questionAnswer)
        
        def itemAnswer1 = new ItemAnswer(item1, answerDetails, new HashSet<>())
        def itemAnswer2 = new ItemAnswer(item2, answerDetails, new HashSet<>())
        def itemAnswer3 = new ItemAnswer(item3, answerDetails, new HashSet<>(combinations))

        def itemAnswers = new HashSet<>()
        itemAnswers.add(itemAnswer1)
        itemAnswers.add(itemAnswer2)
        itemAnswers.add(itemAnswer3)

        answerDetails.setItemAnswers(itemAnswers)

        questionAnswer.setAnswerDetails(answerDetails)

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        
        when:
        def response = restClient.get(
            path: '/executions/'+ courseExecution.getId() +'/quizzes/solved',
            requestContentType: 'application/json'
        )


        then: 'returns correct data'
        response.status == 200
        def solvedQuizDtos = response.data
        solvedQuizDtos.size() == 1

        def solvedQuizDto = solvedQuizDtos.get(0)
        def statementQuizDto = solvedQuizDto.statementQuiz
        statementQuizDto.questions.size() == 1
        solvedQuizDto.statementQuiz.answers.size() == 1
        def answer = solvedQuizDto.statementQuiz.answers.get(0)
        answer.sequence == 0

        def itemStatements = answer.answerDetails.itemStatements

        itemStatements.size() == 3

        solvedQuizDto.correctAnswers.size() == 1
        def correctAnswerDto = solvedQuizDto.correctAnswers.get(0)
        correctAnswerDto.sequence == 0

        correctAnswerDto.correctAnswerDetails.correctItems[0].itemId == item1.getId()
        correctAnswerDto.correctAnswerDetails.correctItems[0].combinations == item1.getCombinations()

        correctAnswerDto.correctAnswerDetails.correctItems[1].itemId == item2.getId()
        correctAnswerDto.correctAnswerDetails.correctItems[1].combinations == item2.getCombinations()

        correctAnswerDto.correctAnswerDetails.correctItems[2].itemId == item3.getId()
        correctAnswerDto.correctAnswerDetails.correctItems[2].combinations.sort() == item3.getCombinations()

        where:
        quizType                 | conclusionDate    | resultsDate
        Quiz.QuizType.GENERATED  | null              | null
        Quiz.QuizType.PROPOSED   | null              | null
        Quiz.QuizType.IN_CLASS   | LOCAL_DATE_BEFORE | LOCAL_DATE_YESTERDAY
        Quiz.QuizType.IN_CLASS   | LOCAL_DATE_BEFORE | null
    }

    @Unroll
    def "does not return quiz with: quizType=#quizType | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quiz.setCourseExecution(courseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(student)
        quizAnswer.setQuiz(quiz)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        
        def answerDetails = new ItemCombinationAnswer(questionAnswer)

        def itemAnswer1 = new ItemAnswer(item1, answerDetails, new HashSet<>())
        def itemAnswer2 = new ItemAnswer(item2, answerDetails, new HashSet<>())
        def itemAnswer3 = new ItemAnswer(item3, answerDetails, new HashSet<>(combinations))

        def itemAnswers = new HashSet<>()
        itemAnswers.add(itemAnswer1)
        itemAnswers.add(itemAnswer2)
        itemAnswers.add(itemAnswer3)

        answerDetails.setItemAnswers(itemAnswers)
        questionAnswer.setAnswerDetails(answerDetails)

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)


        when:
        def response = restClient.get(
            path: '/executions/'+ courseExecution.getId() +'/quizzes/solved',
            requestContentType: 'application/json'
        )

        then: 'returns correct data'
        response.status == 200
        def solvedQuizDtos = response.data
        solvedQuizDtos.size() == 0


        where:
        quizType                | conclusionDate      | resultsDate
        Quiz.QuizType.IN_CLASS  | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER
        Quiz.QuizType.IN_CLASS  | LOCAL_DATE_TOMORROW | null
    }

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}
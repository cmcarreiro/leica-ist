package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
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

@DataJpaTest
class GetSolvedItemCombinationQuizzesTest extends SpockTest {
    def user
    def courseDto
    def question
    def item1
    def item2
    def item3
    def combinations
    def quiz
    def quizQuestion

    def setup() {
        courseDto = new CourseExecutionDto(externalCourseExecution)

        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setContent("Question Content")
        question.setTitle("Question Title")
        questionRepository.save(question)

        def questionDetails = new ItemCombinationQuestion();
        question.setQuestionDetails(questionDetails);
        questionDetailsRepository.save(questionDetails)

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
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
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
        def solvedQuizDtos = answerService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then: 'returns correct data'
        solvedQuizDtos.size() == 1
        def solvedQuizDto = solvedQuizDtos.get(0)
        def statementQuizDto = solvedQuizDto.getStatementQuiz()
        statementQuizDto.getQuestions().size() == 1
        solvedQuizDto.statementQuiz.getAnswers().size() == 1
        def answer = solvedQuizDto.statementQuiz.getAnswers().get(0)
        answer.getSequence() == 0

        def itemStatements = answer.getAnswerDetails().getItemStatements()

        itemStatements.size() == 3
        for(itemStatement in itemStatements){
            if(itemStatement.getItemId() == itemAnswer1.getId()) {
                itemStatement.getCombinations().equals(itemAnswer1.getCombinations())
            }
            else if(itemStatement.getItemId() == itemAnswer2.getId()) {
                itemStatement.getCombinations().equals(itemAnswer2.getCombinations())
            }
            else if(itemStatement.getItemId() == itemAnswer3.getId()) {
                itemStatement.getCombinations().equals(itemAnswer3.getCombinations())
            }
            else {
                assert false
            }
        }

        solvedQuizDto.getCorrectAnswers().size() == 1
        def correctAnswerDto = solvedQuizDto.getCorrectAnswers().get(0)
        correctAnswerDto.getSequence() == 0

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[0].getItemId() == item1.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[0].getCombinations().equals(new HashSet<>(item1.getCombinations()))

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[1].getItemId() == item2.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[1].getCombinations().equals(new HashSet<>(item2.getCombinations()))

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[2].getItemId() == item3.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[2].getCombinations().equals(new HashSet<>(item3.getCombinations()))

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
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
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
        def solvedQuizDtos = answerService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then: 'returns no quizzes'
        solvedQuizDtos.size() == 0

        where:
        quizType                | conclusionDate      | resultsDate
        Quiz.QuizType.IN_CLASS  | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER
        Quiz.QuizType.IN_CLASS  | LOCAL_DATE_TOMORROW | null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
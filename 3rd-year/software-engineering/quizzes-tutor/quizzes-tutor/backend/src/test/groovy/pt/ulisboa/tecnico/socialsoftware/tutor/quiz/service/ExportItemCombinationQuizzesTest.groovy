package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
class ExportItemCombinationQuizzesTest extends SpockTest {
    def quiz
    def creationDate
    def availableDate
    def conclusionDate

    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())


        def itemDto1 = new ItemDto()
        itemDto1.setGroupId(1)
        itemDto1.setContent(ITEM_1_CONTENT)
        
        def combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setContent(ITEM_2_CONTENT)
        itemDto2.setCombinations(combinations)

        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)
        items.add(itemDto2)
        questionDto.getQuestionDetailsDto().setItems(items)

        questionDto = questionService.createQuestion(externalCourse.getId(), questionDto)

        def quizDto = new QuizDto()
        quizDto.setKey(1)
        quizDto.setScramble(false)
        quizDto.setQrCodeOnly(false)
        quizDto.setOneWay(false)
        quizDto.setTitle(QUIZ_TITLE)
        creationDate = DateHandler.now()
        availableDate = DateHandler.now()
        conclusionDate = DateHandler.now().plusDays(2)
        quizDto.setCreationDate(DateHandler.toISOString(creationDate))
        quizDto.setAvailableDate(DateHandler.toISOString(availableDate))
        quizDto.setConclusionDate(DateHandler.toISOString(conclusionDate))
        quizDto.setType(Quiz.QuizType.EXAM.toString())
        quiz = quizService.createQuiz(externalCourseExecution.getId(), quizDto)

        quizService.addQuestionToQuiz(questionDto.getId(), quiz.getId())


        def user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        def statementQuiz = answerService.startQuiz(user.getId(), quiz.getId())
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()

        def itemAnswerDto1 = new ItemStatementAnswerDetailsDto(itemDto1.getId(), new HashSet<>())
        def itemAnswerDto2 = new ItemStatementAnswerDetailsDto(itemDto2.getId(), new HashSet<>(combinations))
        def itemAnswers = new ArrayList<>()
        itemAnswers.add(itemAnswerDto1)
        itemAnswers.add(itemAnswerDto2)

        itemCombinationAnswerDto.setItemStatements(itemAnswers)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(statementQuiz.getQuizAnswerId())
        statementQuiz.getAnswers().add(statementAnswerDto)

        answerService.concludeQuiz(statementQuiz)
    }


    def 'export quizz'() {
        when:
        def exported = quizService.exportQuiz(quiz.getId())

        then:
        exported != null
    }



    def 'export quiz to xml'() {
        when:
        def quizzesXml = quizService.exportQuizzesToXml()

        then:
        quizzesXml != null
        print quizzesXml
    }


    def 'export quiz to latex'() {
        when:
        def quizzesLatex = quizService.exportQuizzesToLatex(quiz.getId())

        then:
        quizzesLatex != null
        print quizzesLatex
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

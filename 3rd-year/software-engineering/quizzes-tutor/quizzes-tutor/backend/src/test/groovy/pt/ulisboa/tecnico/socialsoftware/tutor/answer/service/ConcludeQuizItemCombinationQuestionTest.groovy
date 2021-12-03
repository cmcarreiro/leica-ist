package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_YET_AVAILABLE
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@DataJpaTest
class ConcludeQuizItemCombinationQuestionTest extends SpockTest {

    def user
    def quizQuestion
    def item1
    def item2
    def item3
    def combinations
    def quizAnswer
    def date
    def quiz

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
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

        combinations = new ArrayList<String>()
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

        date = DateHandler.now()

        quizAnswer = new QuizAnswer(user, quiz)
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
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        quizAnswer.getAnswerDate() != null
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getAnswerDetails() == null
        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[0].getItemId() == item1.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[0].getCombinations().equals(new HashSet<>(item1.getCombinations()))

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[1].getItemId() == item2.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[1].getCombinations().equals(new HashSet<>(item2.getCombinations()))

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[2].getItemId() == item3.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[2].getCombinations().equals(new HashSet<>(item3.getCombinations()))

    }

    def 'conclude item combination question quiz IN_CLASS with answer, before conclusionDate'() {
        given: 'an IN_CLASS quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))
        quiz.setType(Quiz.QuizType.IN_CLASS.toString())
        and: 'an empty answer'
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
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        quizAnswer.getAnswerDate() == null
        quizAnswerItemRepository.findAll().size() == 1
        def quizAnswerItem = quizAnswerItemRepository.findAll().get(0)
        quizAnswerItem.getQuizId() == quiz.getId()
        quizAnswerItem.getQuizAnswerId() == quizAnswer.getId()
        quizAnswerItem.getAnswerDate() != null
        quizAnswerItem.getAnswersList().size() == 1
        def resStatementAnswerDto = quizAnswerItem.getAnswersList().get(0)
        resStatementAnswerDto.getAnswerDetails().getItemStatements().size() == 3
        resStatementAnswerDto.getAnswerDetails().getItemStatements()[0].getItemId() == statementItem1Dto.getItemId()
        resStatementAnswerDto.getAnswerDetails().getItemStatements()[0].getCombinations().equals(statementItem1Dto.getCombinations())
        resStatementAnswerDto.getAnswerDetails().getItemStatements()[1].getItemId() == statementItem2Dto.getItemId()
        resStatementAnswerDto.getAnswerDetails().getItemStatements()[1].getCombinations().equals(statementItem2Dto.getCombinations())
        resStatementAnswerDto.getAnswerDetails().getItemStatements()[2].getItemId() == statementItem3Dto.getItemId()
        resStatementAnswerDto.getAnswerDetails().getItemStatements()[2].getCombinations().equals(statementItem3Dto.getCombinations())
        resStatementAnswerDto.getSequence() == 0
        resStatementAnswerDto.getTimeTaken() == 100
        and: 'does not return answers'
        correctAnswers == []
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
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        def itemSet = new HashSet<Item>()
        itemSet.add(item1)
        itemSet.add(item2)
        itemSet.add(item3)
        def itemCombinationAnswer = (ItemCombinationAnswer) questionAnswer.getAnswerDetails()
        def items = itemCombinationAnswer.getItemAnswers().stream().map(ItemAnswer::getItem).collect(Collectors.toList())
        items.size() == 3
        items.contains(item1)
        items.contains(item2)
        items.contains(item3)
        questionAnswer.getAnswerDetails().getItemAnswers().containsAll(item1.getItemAnswers())
        questionAnswer.getAnswerDetails().getItemAnswers().containsAll(item2.getItemAnswers())
        questionAnswer.getAnswerDetails().getItemAnswers().containsAll(item3.getItemAnswers())

        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[0].getItemId() == item1.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[0].getCombinations().equals(new HashSet<>(item1.getCombinations()))

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[1].getItemId() == item2.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[1].getCombinations().equals(new HashSet<>(item2.getCombinations()))

        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[2].getItemId() == item3.getId()
        correctAnswerDto.getCorrectAnswerDetails().getCorrectItems()[2].getCombinations().equals(new HashSet<>(item3.getCombinations()))
    }

    def 'conclude item combination question quiz without answering, before availableDate'() {
        given: 'a quiz with future availableDate'
        quiz.setAvailableDate(DateHandler.now().plusDays(2))
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        when:
        answerService.concludeQuiz(statementQuizDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == QUIZ_NOT_YET_AVAILABLE
    }

    def 'conclude item combination question quiz without answering, after conclusionDate'() {
        given: 'an IN_CLASS quiz with conclusionDate before now in days'
        quiz.setType(Quiz.QuizType.IN_CLASS.toString())
        quiz.setAvailableDate(DateHandler.now().minusDays(2))
        quiz.setConclusionDate(DateHandler.now().minusDays(1))
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        when:
        answerService.concludeQuiz(statementQuizDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == QUIZ_NO_LONGER_AVAILABLE
    }

    def 'conclude item combination question quiz without answering, 9 minutes after conclusionDate'() {
        given: 'an IN_CLASS quiz with conclusionDate before now in days'
        quiz.setType(Quiz.QuizType.IN_CLASS.toString())
        quiz.setAvailableDate(DateHandler.now().minusDays(2))
        quiz.setConclusionDate(DateHandler.now().minusMinutes(9))
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        when:
        answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        quizAnswer.getAnswerDate() == null
        quizAnswerItemRepository.findAll().size() == 1
    }

    def 'conclude item combination question completed quiz'() {
        given:  'a completed quiz'
        quizAnswer.completed = true
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
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'nothing occurs'
        quizAnswer.getAnswerDate() == null
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getAnswerDetails() == null
        and: 'the return value is OK'
        correctAnswers.size() == 0
    }
    
    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
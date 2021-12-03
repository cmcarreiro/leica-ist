package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@DataJpaTest
class TeacherSeesItemCombinationQuizResultsTest extends SpockTest {

    def user
    def quiz
    def quizQuestion
    def quizAnswer
    def statementQuizDto

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

    def "answer quiz with correct answer and see solved quiz"() {

        given: 'a quiz with answers'
        answerService.concludeQuiz(statementQuizDto)

        when:
        def quizAnswers = quizService.getQuizAnswers(quiz.getId())

        then: 'answer matches correct answer'
        quizAnswers.getQuizAnswers().size() == 1

        def questionAnswers = quizAnswers.getQuizAnswers().get(0).getQuestionAnswers()
        questionAnswers.size() == 1
        def questionAnswer = questionAnswers.get(0)


        def answerDto = questionAnswer.getAnswerDetails()
        def questionDto = questionAnswer.getQuestion().getQuestionDetailsDto()

        answerDto.getItemAnswers().size() == 2
        questionDto.getItems().size() == 2

        for(int i=0; i < 2; i++){
            if( answerDto.getItemAnswers()[i].getItemId() == questionDto.getItems()[0].getId()){
                answerDto.getItemAnswers()[i].getCombinations() == new HashSet<>(questionDto.getItems()[0].getCombinations())
            }
            else if( answerDto.getItemAnswers()[i].getItemId() == questionDto.getItems()[1].getId()){
                answerDto.getItemAnswers()[i].getCombinations() == new HashSet<>(questionDto.getItems()[1].getCombinations())
            }
            else {
                assert false
            }
        }

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
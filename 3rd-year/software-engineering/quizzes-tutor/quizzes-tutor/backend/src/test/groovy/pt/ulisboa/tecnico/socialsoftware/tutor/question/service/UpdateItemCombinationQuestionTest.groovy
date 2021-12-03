package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Item
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User


@DataJpaTest
class UpdateItemCombinationQuestionTest extends SpockTest {
    def itemCombinationQuestion
    def item1
    def item2
    def item3
    def combinations
    def user

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        given: "create an item combination question"
        itemCombinationQuestion = new Question()
        itemCombinationQuestion.setCourse(externalCourse)
        itemCombinationQuestion.setKey(1)
        itemCombinationQuestion.setTitle(QUESTION_1_TITLE)
        itemCombinationQuestion.setContent(QUESTION_1_CONTENT)
        itemCombinationQuestion.setStatus(Question.Status.AVAILABLE)
        
        and: 'an image'
        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)
        itemCombinationQuestion.setImage(image)

        def itemCombinationQuestionDetails = new ItemCombinationQuestion()
        itemCombinationQuestion.setQuestionDetails(itemCombinationQuestionDetails)
        questionDetailsRepository.save(itemCombinationQuestionDetails)
        questionRepository.save(itemCombinationQuestion)

        and: 'one item per group'
        item1 = new Item()
        item1.setGroupId(1)
        item1.setContent(ITEM_1_CONTENT)
        item1.setSequence(0)
        item1.setQuestionDetails(itemCombinationQuestionDetails)
        itemRepository.save(item1)

        item2 = new Item()
        item2.setGroupId(1)
        item2.setContent(ITEM_2_CONTENT)
        item2.setSequence(1)
        item2.setQuestionDetails(itemCombinationQuestionDetails)
        itemRepository.save(item2)

        combinations = new ArrayList<String>()
        combinations.add(item1.getContent())
        combinations.add(item2.getContent())
        
        item3 = new Item()
        item3.setGroupId(2)
        item3.setContent(ITEM_3_CONTENT)
        item3.setSequence(3)
        item3.setCombinations(combinations)
        item3.setQuestionDetails(itemCombinationQuestionDetails)
        item3.setCombinations(combinations)
        itemRepository.save(item3)
    }

    def "update an item combination question"() {
        given: "a changed question"
        def questionDto = new QuestionDto(itemCombinationQuestion)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        
        and: '2 changed items'
        def items = new ArrayList<ItemDto>()
        def itemDto1 = new ItemDto(item1)
        itemDto1.setGroupId(1)
        itemDto1.setContent(ITEM_2_CONTENT)
        def itemDto2 = new ItemDto(item2)
        itemDto2.setGroupId(1)
        items.add(itemDto1)
        items.add(itemDto2)
        
        def difCombinations = new ArrayList<String>()
        difCombinations.add(itemDto1.getContent())

        def itemDto3 = new ItemDto(item3)
        itemDto3.setGroupId(2)
        itemDto3.setCombinations(difCombinations)
        items.add(itemDto3)
        
        questionDto.getQuestionDetailsDto().setItems(items)

        and: 'a count to load items to memory due to in memory database flaw'
        questionRepository.count()

        when:
        questionService.updateQuestion(itemCombinationQuestion.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == itemCombinationQuestion.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getImage() != null

        def resItem2 =  result.getQuestionDetails().getItems().get(1)
        resItem2.getContent() == ITEM_2_CONTENT
        resItem2.getSequence() == 1
        resItem2.getGroupId() == 1
        
        and: 'an item is changed'
        result.getQuestionDetails().getItems().size() == 3

        def resItem1 = result.getQuestionDetails().getItems().get(0)
        resItem1.getContent() == ITEM_2_CONTENT
        resItem1.getGroupId() == 1

        def resItem3 = result.getQuestionDetails().getItems().get(2)
        resItem3.getGroupId() == 2
        resItem3.getContent() == ITEM_3_CONTENT
        resItem3.getCombinations().equals(difCombinations)
    }

    def "update an item combination question with missing data from group 1"() {
        given: "a changed question"
        def questionDto = new QuestionDto(itemCombinationQuestion)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        
        and: 'give zero items to group 1'

        def items = new ArrayList<ItemDto>()
        def itemDto3 = new ItemDto(item3)
        items.add(itemDto3)
        
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.updateQuestion(itemCombinationQuestion.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_ONE_ITEM_IN_GROUP_ONE_NEEDED
    }

    def "update an item combination question with missing data from group 2"() {
        given: "a changed question"
        def questionDto = new QuestionDto(itemCombinationQuestion)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        
        and: 'give zero items to group 2'

        def items = new ArrayList<ItemDto>()
        def itemDto1 = new ItemDto(item1)
        items.add(itemDto1)
        def itemDto2 = new ItemDto(item2)
        items.add(itemDto2)
        
        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.updateQuestion(itemCombinationQuestion.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_ONE_ITEM_IN_GROUP_TWO_NEEDED
    }

    def "update an item combination question without a combination"() {
        given: "a changed question"
        def questionDto = new QuestionDto(itemCombinationQuestion)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        
        and: 'give zero combinations'

        def difCombinations = new ArrayList<String>()
        def items = new ArrayList<ItemDto>()
        def itemDto1 = new ItemDto(item1)
        items.add(itemDto1)
        def itemDto2 = new ItemDto(item2)
        items.add(itemDto2)

        def itemDto3 = new ItemDto(item3)
        itemDto3.setCombinations(difCombinations)
        items.add(itemDto3)

        questionDto.getQuestionDetailsDto().setItems(items)

        when:
        questionService.updateQuestion(itemCombinationQuestion.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_ITEM_COMBINATION

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
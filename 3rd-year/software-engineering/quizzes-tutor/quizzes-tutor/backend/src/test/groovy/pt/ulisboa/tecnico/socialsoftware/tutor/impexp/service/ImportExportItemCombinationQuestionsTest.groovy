package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportItemCombinationQuestionsTest extends SpockTest {
    def questionId
    def combinations

    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)

        def itemDto1 = new ItemDto()
        itemDto1.setGroupId(1)
        itemDto1.setSequence(0)
        itemDto1.setContent(ITEM_1_CONTENT)
        
        combinations = new ArrayList<String>()
        combinations.add(itemDto1.getContent())

        def itemDto2 = new ItemDto()
        itemDto2.setGroupId(2)
        itemDto2.setSequence(0)
        itemDto2.setContent(ITEM_2_CONTENT)

        itemDto2.setCombinations(combinations)
    
        def items = new ArrayList<ItemDto>()
        items.add(itemDto1)

        items.add(itemDto2)
        
        questionDto.getQuestionDetailsDto().setItems(items)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export item combination questions to xml'() {
        when:
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        
        then:
        questionsXml != null
    }

    def 'export and import item combination questions to xml'() {
        given: 'a xml with item combination questions'
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getItems().size() == 2
        def itemOneResult = questionResult.getQuestionDetailsDto().getItems().get(0)
        def itemTwoResult = questionResult.getQuestionDetailsDto().getItems().get(1)
        itemOneResult.getSequence() + itemTwoResult.getSequence() == 1
        itemOneResult.getGroupId() == 1
        itemTwoResult.getGroupId() == 2
        itemOneResult.getContent() == ITEM_1_CONTENT
        itemTwoResult.getContent() == ITEM_2_CONTENT
        itemOneResult.getCombinations().size()==0
        itemTwoResult.getCombinations().equals(combinations)
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()
        print questionsLatex

        then:
        questionsLatex != null
    }
    
    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}


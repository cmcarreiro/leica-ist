package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportMultipleChoiceQuestionsTest extends SpockTest {
    def questionId

    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)

        def options = new ArrayList<OptionDto>()

        def optionOne = new OptionDto()
        optionOne.setSequence(0)
        optionOne.setContent(OPTION_1_CONTENT)
        optionOne.setCorrect(true)
        optionOne.setRelevance(2)
        options.add(optionOne)

        def optionTwo = new OptionDto()
        optionTwo.setSequence(1)
        optionTwo.setContent(OPTION_2_CONTENT)
        optionTwo.setCorrect(true)
        optionTwo.setRelevance(1)
        options.add(optionTwo)

        def optionThree = new OptionDto()
        optionThree.setSequence(2)
        optionThree.setContent(OPTION_3_CONTENT)
        optionThree.setCorrect(false)
        optionThree.setRelevance(0)
        options.add(optionThree)

        questionDto.getQuestionDetailsDto().setListOfOptions(options)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export to xml'() {
        when:
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml

        then:
        questionsXml != null
    }

    def 'export and import questions to xml'() {
        given: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()

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
        questionResult.getQuestionDetailsDto().getListOfOptions().size() == 3
        def optionOneResult = questionResult.getQuestionDetailsDto().getListOfOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getListOfOptions().get(1)
        def optionThreeResult = questionResult.getQuestionDetailsDto().getListOfOptions().get(2)
        optionOneResult.getSequence()  == 0
        optionTwoResult.getSequence()  == 1
        optionThreeResult.getSequence()  == 2
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_2_CONTENT
        optionThreeResult.getContent() == OPTION_3_CONTENT
        optionOneResult.isCorrect()
        optionTwoResult.isCorrect()
        !optionThreeResult.isCorrect()
        optionOneResult.getRelevance() == 2
        optionTwoResult.getRelevance() == 1
        optionThreeResult.getRelevance() == 0
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

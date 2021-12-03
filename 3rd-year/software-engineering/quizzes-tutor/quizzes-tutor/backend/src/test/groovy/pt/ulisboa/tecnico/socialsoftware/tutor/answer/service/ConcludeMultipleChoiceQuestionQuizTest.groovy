package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_YET_AVAILABLE
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE

@DataJpaTest
class ConcludeMultipleChoiceQuestionQuizTest extends SpockTest {

    def user
    def quiz
    def questionDetails
    def question
    def quizQuestion
    def quizAnswer

    def setup() {
        //user
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        //quiz
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        //question
        question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)
        questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        //quizQuestion
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        //quizAnswer
        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    def 'conclude quiz with one question with one right option and student chooses one wrong option'() {
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        given: 'four options'
        def option1 = new Option()
        option1.setContent("Option 1 Content")
        option1.setCorrect(false)
        option1.setSequence(0)
        option1.setRelevance(0)
        option1.setQuestionDetails(questionDetails)
        optionRepository.save(option1)

        def option2 = new Option()
        option2.setContent("Option 2 Content")
        option2.setCorrect(true)
        option2.setSequence(1)
        option2.setRelevance(0)
        option2.setQuestionDetails(questionDetails)
        optionRepository.save(option2)

        def option3 = new Option()
        option3.setContent("Option 3 Content")
        option3.setCorrect(false)
        option3.setSequence(2)
        option3.setRelevance(0)
        option3.setQuestionDetails(questionDetails)
        optionRepository.save(option3)

        def option4 = new Option()
        option4.setContent("Option 4 Content")
        option4.setCorrect(false)
        option4.setSequence(3)
        option4.setRelevance(0)
        option4.setQuestionDetails(questionDetails)
        optionRepository.save(option4)

        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        ArrayList<Integer> listOfOptionIds = new ArrayList<>()
        listOfOptionIds.add(option3.getId())
        multipleChoiceAnswerDto.setListOfOptionIds(listOfOptionIds)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'answer submitted is correctly persisted'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        ArrayList<Option> listOfOptions = new ArrayList<>()
        listOfOptions.add(option3)
        ((MultipleChoiceAnswer) questionAnswer.getAnswerDetails()).getListOfOptions().equals(listOfOptions)
        option3.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        
        and: 'the return value is ok'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        print("----------------------------------------------------------------------------------------------------------------")
        print(correctAnswerDto)
        print("----------------------------------------------------------------------------------------------------------------")
        correctAnswerDto.getSequence() == 0
        ArrayList<Integer> listOfCorrectOptionIds = new ArrayList<>()
        listOfCorrectOptionIds.add(option2.getId())
        correctAnswerDto.getCorrectAnswerDetails().getListOfCorrectOptionIds().equals(listOfCorrectOptionIds)
    }

    def 'conclude quiz with one question with one right option and student answers correctly'() {
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        given: 'four options'
        def option1 = new Option()
        option1.setContent("Option 1 Content")
        option1.setCorrect(false)
        option1.setSequence(0)
        option1.setRelevance(0)
        option1.setQuestionDetails(questionDetails)
        optionRepository.save(option1)

        def option2 = new Option()
        option2.setContent("Option 2 Content")
        option2.setCorrect(true)
        option2.setSequence(1)
        option2.setRelevance(0)
        option2.setQuestionDetails(questionDetails)
        optionRepository.save(option2)

        def option3 = new Option()
        option3.setContent("Option 3 Content")
        option3.setCorrect(false)
        option3.setSequence(2)
        option3.setRelevance(0)
        option3.setQuestionDetails(questionDetails)
        optionRepository.save(option3)

        def option4 = new Option()
        option4.setContent("Option 4 Content")
        option4.setCorrect(false)
        option4.setSequence(3)
        option4.setRelevance(0)
        option4.setQuestionDetails(questionDetails)
        optionRepository.save(option4)

        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        ArrayList<Integer> listOfOptionIds = new ArrayList<>()
        listOfOptionIds.add(option2.getId())
        multipleChoiceAnswerDto.setListOfOptionIds(listOfOptionIds)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'answer submitted is correctly persisted'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        ArrayList<Option> listOfOptions = new ArrayList<>()
        listOfOptions.add(option2)
        ((MultipleChoiceAnswer) questionAnswer.getAnswerDetails()).getListOfOptions().equals(listOfOptions)
        option2.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        
        and: 'the return value is ok'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        ArrayList<Integer> listOfCorrectOptionIds = new ArrayList<>()
        listOfCorrectOptionIds.add(option2.getId())
        correctAnswerDto.getCorrectAnswerDetails().getListOfCorrectOptionIds().equals(listOfCorrectOptionIds)
    }

    def 'conclude quiz with one question with three right options with no relevance and student answers correctly'() {
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        given: 'four options'
        def option1 = new Option()
        option1.setContent("Option 1 Content")
        option1.setCorrect(true)
        option1.setSequence(0)
        option1.setRelevance(0)
        option1.setQuestionDetails(questionDetails)
        optionRepository.save(option1)

        def option2 = new Option()
        option2.setContent("Option 2 Content")
        option2.setCorrect(true)
        option2.setSequence(1)
        option2.setRelevance(0)
        option2.setQuestionDetails(questionDetails)
        optionRepository.save(option2)

        def option3 = new Option()
        option3.setContent("Option 3 Content")
        option3.setCorrect(true)
        option3.setSequence(2)
        option3.setRelevance(0)
        option3.setQuestionDetails(questionDetails)
        optionRepository.save(option3)

        def option4 = new Option()
        option4.setContent("Option 4 Content")
        option4.setCorrect(false)
        option4.setSequence(3)
        option4.setRelevance(0)
        option4.setQuestionDetails(questionDetails)
        optionRepository.save(option4)

        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        ArrayList<Integer> listOfOptionIds = new ArrayList<>()
        listOfOptionIds.add(option1.getId())
        listOfOptionIds.add(option2.getId())
        listOfOptionIds.add(option3.getId())
        multipleChoiceAnswerDto.setListOfOptionIds(listOfOptionIds)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'answer submitted is correctly persisted'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        ArrayList<Option> listOfOptions = new ArrayList<>()
        listOfOptions.add(option1)
        listOfOptions.add(option2)
        listOfOptions.add(option3)
        ((MultipleChoiceAnswer) questionAnswer.getAnswerDetails()).getListOfOptions().equals(listOfOptions)
        option1.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        option2.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        option3.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        
        and: 'the return value is ok'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        ArrayList<Integer> listOfCorrectOptionIds = new ArrayList<>()
        listOfCorrectOptionIds.add(option1.getId())
        listOfCorrectOptionIds.add(option2.getId())
        listOfCorrectOptionIds.add(option3.getId())
        correctAnswerDto.getCorrectAnswerDetails().getListOfCorrectOptionIds().equals(listOfCorrectOptionIds)
    }

    def 'conclude quiz with one question with three right options with relevance and student answers correctly'() {
        quiz.setConclusionDate(DateHandler.now().plusDays(2))

        given: 'four options'
        def option1 = new Option()
        option1.setContent("Option 1 Content")
        option1.setCorrect(true)
        option1.setSequence(0)
        option1.setRelevance(3)
        option1.setQuestionDetails(questionDetails)
        optionRepository.save(option1)

        def option2 = new Option()
        option2.setContent("Option 2 Content")
        option2.setCorrect(true)
        option2.setSequence(1)
        option2.setRelevance(2)
        option2.setQuestionDetails(questionDetails)
        optionRepository.save(option2)

        def option3 = new Option()
        option3.setContent("Option 3 Content")
        option3.setCorrect(true)
        option3.setSequence(2)
        option3.setRelevance(1)
        option3.setQuestionDetails(questionDetails)
        optionRepository.save(option3)

        def option4 = new Option()
        option4.setContent("Option 4 Content")
        option4.setCorrect(false)
        option4.setSequence(3)
        option4.setRelevance(0)
        option4.setQuestionDetails(questionDetails)
        optionRepository.save(option4)

        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        ArrayList<Integer> listOfOptionIds = new ArrayList<>()
        listOfOptionIds.add(option3.getId())
        listOfOptionIds.add(option2.getId())
        listOfOptionIds.add(option1.getId())
        multipleChoiceAnswerDto.setListOfOptionIds(listOfOptionIds)
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'answer submitted is correctly persisted'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        ArrayList<Option> listOfOptions = new ArrayList<>()
        listOfOptions.add(option3)
        listOfOptions.add(option2)
        listOfOptions.add(option1)
        ((MultipleChoiceAnswer) questionAnswer.getAnswerDetails()).getListOfOptions().equals(listOfOptions)
        option1.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        option2.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        option3.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        
        and: 'the return value is ok'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        ArrayList<Integer> listOfCorrectOptionIds = new ArrayList<>()
        listOfCorrectOptionIds.add(option3.getId())
        listOfCorrectOptionIds.add(option2.getId())
        listOfCorrectOptionIds.add(option1.getId())
        correctAnswerDto.getCorrectAnswerDetails().getListOfCorrectOptionIds().equals(listOfCorrectOptionIds)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
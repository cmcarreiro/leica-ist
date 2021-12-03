package pt.ulisboa.tecnico.socialsoftware.tutor.user.domain

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto
import spock.lang.Unroll

@DataJpaTest
class GetNumberOfQuizzesTest extends SpockTest {
    def user

    def quiz

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        userRepository.save(user)

        QuizDto quizDto = new QuizDto()
        quizDto.setKey(1)
        quizDto.setTitle(QUIZ_TITLE)
        quizDto.setAvailableDate(STRING_DATE_TODAY)
        quizDto.setConclusionDate(STRING_DATE_TOMORROW)
        quizDto.setResultsDate(STRING_DATE_LATER)

        Question question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setTitle(QUESTION_1_TITLE)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        Option option = new Option()
        option.setSequence(1)
        option.setCorrect(true)
        question.getQuestionDetails().addOption(option)

        QuestionDto questionDto = new QuestionDto(question)
        questionDto.setKey(1)
        questionDto.setSequence(1)

        def questions = new ArrayList()
        questions.add(questionDto)
        quizDto.setQuestions(questions)

        quiz = new Quiz(quizDto)
        quiz.setCourseExecution(externalCourseExecution);
        quizRepository.save(quiz)

        QuizQuestion quizQuestion = new QuizQuestion(quiz, question, 1)

        QuizAnswer quizAnswer = new QuizAnswer(user, quiz)
        quizAnswer.setCompleted(true)

        def questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion, 1, 1)
        def listOfOptions = new ArrayList<>()
        listOfOptions.add(option)
        questionAnswer.setAnswerDetails(new MultipleChoiceAnswer(questionAnswer, listOfOptions))
    }

    @Unroll
    def "get number of quizzes" () {
        given:
        user.numberOfTeacherQuizzes = null
        user.numberOfStudentQuizzes = null
        user.numberOfInClassQuizzes = null
        quiz.setType(type)

        when:
        def nTeacherQuizzes = user.getNumberOfTeacherQuizzes()
        def nStudentQuizzes = user.getNumberOfStudentQuizzes()
        def nInClassQuizzes = user.getNumberOfInClassQuizzes()

        then:
        nTeacherQuizzes == nQuizzes1
        nStudentQuizzes == nQuizzes2
        nInClassQuizzes == nQuizzes3

        where:
        type        || nQuizzes1 | nQuizzes2 | nQuizzes3
        "PROPOSED"  || 1         | 0         | 0
        "GENERATED" || 0         | 1         | 0
        "IN_CLASS"  || 0         | 0         | 1
        "TEST"      || 0         | 0         | 0
        "EXAM"      || 0         | 0         | 0
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

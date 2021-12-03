package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenAnswerCorrectAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenAnswerStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler

@DataJpaTest
class StudentSeesOpenAnswerQuizResultTest extends SpockTest {

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
        def questionDetails = new OpenAnswerQuestion()
        questionDetails.setAnswer(OPEN_ANSWER_1_CONTENT)
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def OpenAnswerDto = new OpenAnswerStatementAnswerDetailsDto()
        OpenAnswerDto.setAnswer(OPEN_ANSWER_1_CONTENT)
        statementAnswerDto.setAnswerDetails(OpenAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
    }

    def "answer quiz with correct answer and see solved quiz"() {

        given: 'a concluded quiz'
        answerService.concludeQuiz(statementQuizDto)

        when:
        def solvedQuizzes = answerService.getSolvedQuizzes(user.getId(), externalCourseExecution.getId())

        then: 'quiz is solved and answer is correct'
        // this is the same method for quiz score in frontend
        solvedQuizzes.size() == 1
        def solved = solvedQuizzes.get(0)
        solved.getStatementQuiz().getAnswers().size() == 1
        solved.getCorrectAnswers().size() == 1
        def answer = (OpenAnswerStatementAnswerDetailsDto) solved.getStatementQuiz().getAnswers().get(0).getAnswerDetails()
        def correctAnswer = (OpenAnswerCorrectAnswerDto) solved.getCorrectAnswers().get(0).getCorrectAnswerDetails()

        answer.getAnswer() == correctAnswer.getCorrectAnswer()
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

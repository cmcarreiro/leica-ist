package pt.ulisboa.tecnico.socialsoftware.tutor.answer.webservice

import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcludeQuizMultipleChoiceAnswerWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def user
    def question
    def questionDetails
    def response
    def quiz
    def quizQuestion
    def quizAnswer
    def optionOneOk
    def optionTwoOk
    def optionThreeKo

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        //user
        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        user.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(user)
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

        //option
        //def options = new ArrayList<Option>()

        optionOneOk = new Option()
        optionOneOk.setContent("Option One Content")
        optionOneOk.setCorrect(true)
        optionOneOk.setSequence(0)
        optionOneOk.setRelevance(1)
        optionOneOk.setQuestionDetails(questionDetails)
        //options.add(optionOneOk)
        optionRepository.save(optionOneOk)

        optionTwoOk = new Option()
        optionTwoOk.setContent("Option Two Content")
        optionTwoOk.setCorrect(true)
        optionTwoOk.setSequence(1)
        optionTwoOk.setRelevance(2)
        optionTwoOk.setQuestionDetails(questionDetails)
        //options.add(optionTwoOk)
        optionRepository.save(optionTwoOk)

        optionThreeKo = new Option()
        optionThreeKo.setContent("Option Three Content")
        optionThreeKo.setCorrect(false)
        optionThreeKo.setSequence(2)
        optionThreeKo.setRelevance(0)
        optionThreeKo.setQuestionDetails(questionDetails)
        //options.add(optionThreeKo)
        optionRepository.save(optionThreeKo)

        //question.getQuestionDetails().setListOfOptions(options)
    }

    def 'conclude quiz with empty answer'() {

        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
        
        and: 'a good user login'
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: JsonOutput.toJson(statementQuizDto),
                requestContentType: 'application/json'
        )

        then: "the request returns OK"
        response != null
        response.status == 200

        and: 'if it responds with the correct answers'
        def correctAnswers = response.data
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.sequence == 0
        def correctAnswerDetails = correctAnswerDto.correctAnswerDetails
        correctAnswerDetails.type == "multiple_choice"
        ArrayList<Integer> listOfCorrectOptionIds = new ArrayList<>()
        listOfCorrectOptionIds.add(optionOneOk.getId())
        listOfCorrectOptionIds.add(optionTwoOk.getId())
        correctAnswerDetails.listOfCorrectOptionIds.equals(listOfCorrectOptionIds)
    }

    /*def 'conclude quiz with right answer'() {
        given: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def multipleChoiceAnswer = new multipleChoiceAnswer()

        
        def multipleChoiceAnswerDto = new MultipleChoiceStatementAnswerDetailsDto()
        

        
        def rightAnswerOptions = new ArrayList<Option>
        rightAnswerOptions.add(optionOneOK)
        rightAnswerOptions.add(optionTwoOk)
        
        //def OpenAnswerDto = new OpenAnswerStatementAnswerDetailsDto()
        //OpenAnswerDto.setAnswer(OPEN_ANSWER_1_CONTENT)
        
        statementAnswerDto.setAnswerDetails(multipleChoiceAnswerDto)

        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)


        and: 'a user login'
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

         when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: JsonOutput.toJson(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "the request returns OK"
        response != null
        response.status == 200
        and: 'if it responds with the correct answers'
        def correctAnswers = response.data
        correctAnswers.size() == 1
        def correctAnswer = response.data.get(0)
        correctAnswer.sequence == 0
        def correctAnswerDetails = correctAnswer.correctAnswerDetails
        correctAnswerDetails.type == "open_answer"
        correctAnswerDetails.correctAnswer == OPEN_ANSWER_1_CONTENT

        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getAnswerDetails().getAnswerRepresentation() == OPEN_ANSWER_1_CONTENT
    }

    def 'conclude quiz with wrong answer'() {
        
        given: 'an answer'
        def statementQuizDto = new StatementQuizDto()
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

        and: 'a user login'
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: JsonOutput.toJson(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "the request returns OK"
        response != null
        response.status == 200

        and: 'if it responds with the correct answers'
        def correctAnswers = response.data
        correctAnswers.size() == 1
        def correctAnswer = response.data.get(0)
        correctAnswer.sequence == 0
        def correctAnswerDetails = correctAnswer.correctAnswerDetails
        correctAnswerDetails.type == "open_answer"
        correctAnswerDetails.correctAnswer == OPEN_ANSWER_1_CONTENT

        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getAnswerDetails().getAnswerRepresentation() == OPEN_ANSWER_1_CONTENT
    } 


    def 'unauthorized student tries to complete quiz'() {

        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        and: 'a user login'
        def userKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(userKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: JsonOutput.toJson(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(userKO.getId())
    }


    def 'unauthorized teacher tries to complete quiz'() {

        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)
        
        and: 'a user login'
        def userKO = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL, User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        userKO.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        userRepository.save(userKO)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        when:
        response = restClient.post(
                path: '/quizzes/' + quiz.getId() + '/conclude',
                body: JsonOutput.toJson(statementAnswerDto),
                requestContentType: 'application/json'
        )

        then: "the request returns 403"
        def exception = thrown(HttpResponseException)
        exception.response.status == 403

        cleanup:
        userRepository.deleteById(userKO.getId())
    }*/

    def cleanup() {
        persistentCourseCleanup()
        userRepository.deleteById(user.getId())
    }

}

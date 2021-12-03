package pt.ulisboa.tecnico.socialsoftware.tutor.answer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.api.TopicController;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class AnswerController {
    private static final Logger logger = LoggerFactory.getLogger(TopicController.class);

    @Autowired
    private AnswerService answerService;

    @GetMapping("/executions/{executionId}/quizzes/available")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<QuizDto> getAvailableQuizzes(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        return answerService.getAvailableQuizzes(user.getId(), executionId);
    }

    @PostMapping("/executions/{executionId}/quizzes/generate")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public StatementQuizDto getNewQuiz(Principal principal, @PathVariable int executionId, @RequestBody StatementCreationDto quizDetails) {
        User user = (User) ((Authentication) principal).getPrincipal();

        return answerService.generateStudentQuiz(user.getId(), executionId, quizDetails);
    }

    @GetMapping("/executions/{executionId}/quizzes/solved")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<SolvedQuizDto> getSolvedQuizzes(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        return answerService.getSolvedQuizzes(user.getId(), executionId);
    }

    @GetMapping("/quizzes/{quizId}/byqrcode")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public StatementQuizDto getQuizByQRCode(Principal principal, @PathVariable int quizId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        return answerService.getQuizByQRCode(user.getId(), quizId);
    }

    @GetMapping("/quizzes/{quizId}/start")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public StatementQuizDto startQuiz(Principal principal, @PathVariable int quizId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        return answerService.startQuiz(user.getId(), quizId);
    }

    @GetMapping("/quizzes/{quizId}/question/{questionId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public StatementQuestionDto getQuestionForQuizAnswer(@PathVariable int quizId, @PathVariable int questionId) {
        return answerService.getQuestionForQuizAnswer(quizId, questionId);
    }

    @PostMapping("/quizzes/{quizId}/submit")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public void submitAnswer(Principal principal, @PathVariable int quizId, @Valid @RequestBody StatementAnswerDto answer) {
        User user = (User) ((Authentication) principal).getPrincipal();

        answerService.submitAnswer(user.getUsername(), quizId, answer);
    }

    @PostMapping("/quizzes/{quizId}/conclude")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#quizId, 'QUIZ.ACCESS')")
    public List<CorrectAnswerDto> concludeQuiz(@PathVariable int quizId, @RequestBody StatementQuizDto statementQuizDto) {
        return answerService.concludeQuiz(statementQuizDto);
    }

}
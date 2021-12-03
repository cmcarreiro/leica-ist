package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;

import java.util.Comparator;
import java.util.stream.Collectors;

public abstract class LatexVisitor implements Visitor {
    protected String result = "";
    protected String questionContent;

    @Override
    public void visitQuiz(Quiz quiz) {
        this.result = this.result +
                "% Title: " + quiz.getTitle() + "\n" +
                "% Available date: " + quiz.getAvailableDate() + "\n" +
                "% Conclusion date: " + quiz.getConclusionDate() + "\n" +
                "% Type: " + quiz.getType() + "\n" +
                "% Scramble: " + quiz.getScramble() + "\n" +
                "% OneWay: " + quiz.isOneWay() + "\n" +
                "% QrCodeOnly: " + quiz.isQrCodeOnly() + "\n\n";
    }

    @Override
    public void visitQuizQuestion(QuizQuestion quizQuestion) {
        this.result = this.result
                + "\\q" + quizQuestion.getQuestion().getTitle().replaceAll("\\s+", "")
                + convertToAlphabet(quizQuestion.getQuestion().getKey())
                + "\n\n";
    }

    @Override
    public void visitQuestion(Question question) {
        this.result = this.result
                + "\\newcommand{\\q"
                + question.getTitle().replaceAll("\\s+", "")
                + convertToAlphabet(question.getKey())
                + "}{\n"
                + "\\begin{ClosedQuestion}\n";

        this.questionContent = question.getContent();

        if (question.getImage() != null)
            question.getImage().accept(this);

        this.result = this.result + "\t" + this.questionContent + "\n\n";

        question.getQuestionDetails().accept(this);


    }

    @Override
    public void visitQuestionDetails(MultipleChoiceQuestion question) {
        question.visitOptions(this);

        this.result = this.result + "\\putOptions\n";

        this.result = this.result + "% Answer: " + question.getCorrectAnswerRepresentation() + "\n";

        this.result = this.result + "\\end{ClosedQuestion}\n}\n\n";
    }

    @Override
    public void visitQuestionDetails(ItemCombinationQuestion question) {

        this.result = this.result + "\\putItems\n";
        question.visitItems(this);


        this.result = this.result + "% Answer: " + question.getCorrectAnswerRepresentation() + "\n";

        this.result = this.result + "\\end{ClosedQuestion}\n}\n\n";
    }

    @Override
    public void visitQuestionDetails(OpenAnswerQuestion question) {
      this.result = this.result + "\\putCorrectAnswer\n";

      this.result = this.result + "% Answer: " + question.getCorrectAnswerRepresentation() + "\n";

      this.result = this.result + "\\end{ClosedQuestion}\n}\n\n";
    }

    @Override
    public void visitQuestionDetails(CodeFillInQuestion question) {

        this.result +=
                String.format("\n\tCode snippet language: %s", question.getLanguage()) +
                        "\n\\begin{lstlisting}\n" + question.getCode() + "\n\\end{lstlisting}\n\n";

        question.visitFillInSpots(this);

        this.result = this.result + "\\putOptions\n";

        this.result = this.result + "% Answer: " +
                question.getFillInSpots()
                        .stream()
                        .sorted(Comparator.comparing(CodeFillInSpot::getSequence))
                        .map(spot -> spot.getOptions()
                                .stream()
                                .filter(CodeFillInOption::isCorrect)
                                .map(x -> String.format("{%s}", x.getContent()))
                                .findAny()
                                .orElse("")
                        ).collect(Collectors.joining("; ")) + "\n";

        this.result = this.result + "\\end{ClosedQuestion}\n}\n\n";
    }

    @Override
    public void visitQuestionDetails(CodeOrderQuestion question) {

        this.result += String.format("\n\tCode snippet language: %s", question.getLanguage()) + "\n\n";
        this.result += "\\begin{lstlisting}\n";
        question.visitCodeOrderSlots(this);
        this.result += "\\end{lstlisting}\n";

        this.result = this.result + "% Answer: \n\\begin{lstlisting}\n" +
                question.getCodeOrderSlots()
                        .stream()
                        .filter(x -> x.getOrder() != null)
                        .sorted(Comparator.comparing(CodeOrderSlot::getOrder))
                        .map(spot -> spot.getContent()
                        ).collect(Collectors.joining("\n")) + "\n\\end{lstlisting}\n";

        this.result = this.result + "\\end{ClosedQuestion}\n}\n\n";
    }

    @Override
    public void visitImage(Image image) {
        String imageString = "\n\t\\begin{center}\n";
        imageString = imageString + "\t\t\\includegraphics[width=" + image.getWidth() + "cm]{" + image.getUrl() + "}\n";
        imageString = imageString + "\t\\end{center}\n\t";

        this.questionContent = this.questionContent.replaceAll("!\\[image\\]\\[image\\]", imageString);
    }

    @Override
    public void visitCodeOrderSlot(CodeOrderSlot slot) {
        this.result += String.format("%s\n", slot.getContent());
    }

    @Override
    public void visitFillInSpot(CodeFillInSpot spot) {
        this.result += String.format("\n\t Spot -> {{slot-%d}}\n", spot.getSequence());
        spot.visitOptions(this);
    }

    @Override
    public void visitFillInOption(CodeFillInOption option) {
        this.result += "\t\\fillInOption{" + option.getContent() + "}\n";
    }

    @Override
    public void visitOption(Option option) {
        this.result = this.result + "\t\\option" + MultipleChoiceQuestion.convertSequenceToLetter(option.getSequence()) + "{" + option.getContent() + "}\n";
    }

    @Override
    public void visitItem(Item item) {
        this.result = this.result + "\t\\item" + ItemCombinationQuestion.convertSequenceToLetter(item.getSequence()) + "{content: " + item.getContent() + ", groupId: " + item.getGroupId() + "}\n";
    }

    private String convertToAlphabet(int number) {
        String result = "";
        String ALPHABET = "ABCDEFGHIJ";
        String numberString = String.valueOf(number);
        for (int i = 0; i < numberString.length(); i++) {
            int position = Character.getNumericValue(numberString.charAt(i));
            result = result + ALPHABET.charAt(position);
        }

        return result;
    }

}

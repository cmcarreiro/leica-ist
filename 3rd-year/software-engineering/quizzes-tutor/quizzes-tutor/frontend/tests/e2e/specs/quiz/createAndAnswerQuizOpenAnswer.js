describe('Manage Open Answer Quiz Walk-through', () => {
    function validateQuestion(
      title,
      content,
      answer
    ) {

      cy.get('[data-cy="showQuestionDialog"]')
        .should('be.visible')
        .within($ls => {
          cy.get('.headline').should('contain', title);
          cy.get('span > p').should('contain', content);
          cy.get('[data-cy="answer"]').should('contain', answer);
        });
    }

    function validateQuestionFull(
      title,
      content,
      answer
    ) {
      cy.log('Validate question with show dialog.');

      cy.get('[data-cy="questionTitleGrid"]')
        .first()
        .click();

      validateQuestion(title, content, answer);

      cy.get('button')
        .contains('close')
        .click();
    }

    before(() => {
        cy.cleanQuizzes();
        cy.cleanOpenAnswerQuestionsByName('Cypress Question Example');
    });
    after(() => {
        cy.cleanQuizzes();
        cy.cleanOpenAnswerQuestionsByName('Cypress Question Example');
    });

    beforeEach(() => {});

    afterEach(() => {
        cy.logout();
    });

    it('Creates new open answer questions', function() {
        cy.demoTeacherLogin();
        cy.server();
        cy.route('GET', '/courses/*/questions').as('getQuestions');
        cy.route('GET', '/courses/*/topics').as('getTopics');
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="questionsTeacherMenuButton"]').click();

        cy.wait('@getQuestions')
            .its('status')
            .should('eq', 200);

        cy.wait('@getTopics')
            .its('status')
            .should('eq', 200);

        cy.get('button')
            .contains('New Question')
            .click();

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible');

        cy.get('span.headline').should('contain', 'New Question');

        cy.get(
            '[data-cy="questionTitleTextArea"]'
        ).type('Cypress Question Example - 01', { force: true });
        cy.get(
            '[data-cy="questionQuestionTextArea"]'
        ).type('Cypress Question Example - Content - 01', { force: true });

        cy.get('[data-cy="questionTypeInput"]')
            .type('open_answer', { force: true })
            .click({ force: true });

        cy.get('[data-cy="textInput"]'
        ).type('Cypress Question Example - Answer - 01', { force: true});

        cy.route('POST', '/courses/*/questions/').as('postQuestion');

        cy.get('button')
            .contains('Save')
            .click();

        cy.wait('@postQuestion')
            .its('status')
            .should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]')
            .first()
            .should('contain', 'Cypress Question Example - 01');

        validateQuestionFull(
            'Cypress Question Example - 01',
            'Cypress Question Example - Content - 01',
            'Cypress Question Example - Answer - 01'
        );

        cy.get('button')
            .contains('New Question')
            .click();

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible');

        cy.get('span.headline').should('contain', 'New Question');

        cy.get(
            '[data-cy="questionTitleTextArea"]'
        ).type('Cypress Question Example - 02', { force: true });
        cy.get(
            '[data-cy="questionQuestionTextArea"]'
        ).type('Cypress Question Example - Content - 02', { force: true });

        cy.get('[data-cy="questionTypeInput"]')
            .type('open_answer', { force: true })
            .click({ force: true });

        cy.get('[data-cy="textInput"]'
        ).type('Cypress Question Example - Answer - 02', { force: true});

        cy.route('POST', '/courses/*/questions/').as('postQuestion');

        cy.get('button')
            .contains('Save')
            .click();

        cy.wait('@postQuestion')
            .its('status')
            .should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]')
            .first()
            .should('contain', 'Cypress Question Example - 02');

        validateQuestionFull(
            'Cypress Question Example - 02',
            'Cypress Question Example - Content - 02',
            'Cypress Question Example - Answer - 02'
        );
    });

    it('Teacher creates Quiz', function() {
        cy.demoTeacherLogin();
        cy.server();
        cy.route('GET', '/courses/*/questions').as('getQuestions');
        cy.route('GET', '/courses/*/topics').as('getTopics');
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="questionsTeacherMenuButton"]').click();

        cy.createQuizzWith2Questions('Cypress Open Answer Quiz Example - 01', 'Cypress Question Example - 01', 'Cypress Question Example - 02');
        cy.createQuizzWith2Questions('Cypress Open Answer Quiz Example - 02', 'Cypress Question Example - 01', 'Cypress Question Example - 02');

    });

    it('Student responds to Quiz with right answers', function() {
        cy.demoStudentLogin();
        cy.server();
        cy.get('[data-cy="quizzesStudentMenuButton"]').click();
        cy.contains('Available').click();

        cy.contains('Cypress Open Answer Quiz Example - 01').click();

        var i = 1;
        for(i = 1; i < 2; i++) {
            cy.get('[data-cy="textInput"]').type('Cypress Question Example - Answer - 0' + i, { force: true });
            cy.get('[data-cy="nextQuestionButton"]').click();
        }
        cy.get('[data-cy="textInput"]').type('Cypress Question Example - Answer - 0' + i, { force: true });
        cy.get('[data-cy="endQuizButton"]').click();
        cy.get('[data-cy="confirmationButton"]').click();

        cy.get('[data-cy="quizzesStudentMenuButton"]').click();

        cy.contains('Solved').click();

        cy.contains('Cypress Open Answer Quiz Example - 01').parent().should('contain', '2/2');
    });

    it('Teacher views correct results of quiz', function() {
        cy.demoTeacherLogin();
        cy.server();
        cy.route('GET', '/courses/*/questions').as('getQuestions');
        cy.route('GET', '/courses/*/topics').as('getTopics');
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="quizzesTeacherMenuButton"]').click();

        cy.contains('Cypress Open Answer Quiz Example - 01').parent().parent().within(() => {
            cy.get('[data-cy="showResults"]').click();
        });

        cy.get('tbody tr')
            .first()
            .get('[data-cy="showResultsDetails"]').first()
            .should('have.class', 'answer correct');

        cy.get('tbody tr')
            .first()
            .get('[data-cy="showResultsDetails"]').last()
            .should('have.class', 'answer correct');
    });

    it('Student responds to Quiz with wrong answers', function() {
        cy.demoStudentLogin();
        cy.server();
        cy.get('[data-cy="quizzesStudentMenuButton"]').click();
        cy.contains('Available').click();

        cy.contains('Cypress Open Answer Quiz Example - 02').click();

        var i = 1;
        for(i = 1; i < 2; i++) {
            cy.get('[data-cy="textInput"]').type('Cypress Question Example - WrongAnswer - 0' + i, { force: true });
            cy.get('[data-cy="nextQuestionButton"]').click();
        }
        cy.get('[data-cy="textInput"]').type('Cypress Question Example - WrongAnswer - 0' + i, { force: true });
        cy.get('[data-cy="endQuizButton"]').click();
        cy.get('[data-cy="confirmationButton"]').click();

        cy.get('[data-cy="quizzesStudentMenuButton"]').click();

        cy.contains('Solved').click();

        cy.contains('Cypress Open Answer Quiz Example - 02').parent().should('contain', '0/2');
    });

    it('Teacher views wrong results of quiz', function() {
        cy.demoTeacherLogin();
        cy.server();
        cy.route('GET', '/courses/*/questions').as('getQuestions');
        cy.route('GET', '/courses/*/topics').as('getTopics');
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="quizzesTeacherMenuButton"]').click();

        cy.contains('Cypress Open Answer Quiz Example - 02').parent().parent().within(() => {
            cy.get('[data-cy="showResults"]').click();
        });

        cy.get('tbody tr')
            .first()
            .get('[data-cy="showResultsDetails"]').first()
            .should('have.class', 'answer incorrect');

        cy.get('tbody tr')
            .first()
            .get('[data-cy="showResultsDetails"]').last()
            .should('have.class', 'answer incorrect');
    });


});

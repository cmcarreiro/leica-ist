describe('Manage Open Answer Questions Walk-through', () => {
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
    cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
    cy.cleanCodeFillInQuestionsByName('Cypress Question Example');
    cy.cleanOpenAnswerQuestionsByName('Cypress Question Example');
  });
  after(() => {
    cy.cleanOpenAnswerQuestionsByName('Cypress Question Example');
  });

  beforeEach(() => {
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
  });

  afterEach(() => {
    cy.logout();
  });

  it('Creates a new open answer question', function() {
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
  });


  it('Creates a new open answer question with invalid answer example', function() {
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

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button')
      .contains('Save')
      .click();

    cy.wait('@postQuestion')
      .its('status')
      .should('eq', 400);

    cy.contains('Error')
      .should('contain', 'Error: Open answer question must specify a correct answer example.');

  });

  it('Can update title (with right-click)', function() {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .rightclick();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within($list => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionTitleTextArea"]')
          .clear({ force: true })
          .type('Cypress Question Example - 01 - Edited', { force: true });

        cy.get('button')
          .contains('Save')
          .click();
      });

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01 - Edited');

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited',
      'Cypress Question Example - Content - 01',
      'Cypress Question Example - Answer - 01'
    );
  });

  it('Can update content (with button)', function() {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody tr')
      .first()
      .within($list => {
        cy.get('button')
          .contains('edit')
          .click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within($list => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionQuestionTextArea"]')
          .clear({ force: true })
          .type('Cypress Question Example - Content - 01 - Edited', { force: true });

        cy.get('[data-cy="textInput"]')
          .clear({ force: true})
          .type('Cypress Question Example - Answer - 01 - Edited',  { force: true});

        cy.get('button')
          .contains('Save')
          .click();
      });

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 200);

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited',
      'Cypress Question Example - Content - 01 - Edited',
      'Cypress Question Example - Answer - 01 - Edited'
    );
  });


  it('Can\'t update content (with button) with empty answer', function() {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody tr')
      .first()
      .within($list => {
        cy.get('button')
          .contains('edit')
          .click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within($list => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionQuestionTextArea"]')
          .clear({ force: true })
          .type('Cypress Question Example - Content - 01 - Edited', { force: true });

        cy.get('[data-cy="textInput"]')
          .clear({ force: true});

        cy.get('button')
          .contains('Save')
          .click();
      });

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 400);

    cy.contains('Error')
      .should('contain', 'Error: Open answer question must specify a correct answer example.');
  });

  it('Can duplicate question', function() {
    cy.get('tbody tr')
      .first()
      .within($list => {
        cy.get('button')
          .contains('cached')
          .click();
      });

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get('[data-cy="questionTitleTextArea"]')
      .should('have.value', 'Cypress Question Example - 01 - Edited')
      .type('{end} - DUP', { force: true });

    cy.get('[data-cy="questionQuestionTextArea"]')
      .should('have.value', 'Cypress Question Example - Content - 01 - Edited'
      );

    cy.get('[data-cy="textInput"')
      .should('have.value', 'Cypress Question Example - Answer - 01 - Edited'
      );

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button')
      .contains('Save')
      .click();

    cy.wait('@postQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01 - Edited - DUP');

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited - DUP',
      'Cypress Question Example - Content - 01 - Edited',
      'Cypress Question Example - Answer - 01 - Edited'
    );
  });

  it('Can delete open answer question', function() {
    cy.route('DELETE', '/questions/*').as('deleteQuestion');

    cy.get('tbody>tr')
      .contains('Cypress Question Example - 01 - Edited - DUP')
      .parent()
      .parent()
      .within($list => {
        cy.get('button')
          .contains('delete')
          .click();
      });
  });

  it('Can view open answer question (with click)', function() {
    validateQuestionFull(
        'Cypress Question Example - 01 - Edited',
        'Cypress Question Example - Content - 01 - Edited',
        'Cypress Question Example - Answer - 01 - Edited'
    );
  });

  it('Can view open answer question (with button)', function() {
    cy.get('tbody>tr')
      .contains('Cypress Question Example - 01 - Edited')
      .parent()
      .parent()
      .within($list => {
        cy.get('button')
          .contains('visibility')
          .click();
      });

    validateQuestion(
      'Cypress Question Example - 01 - Edited',
      'Cypress Question Example - Content - 01 - Edited',
      'Cypress Question Example - Answer - 01 - Edited'
    );

    cy.get('button')
      .contains('close')
      .click();
  });

});

describe('Create Multiple Choice Question Walkthrough', () => {
  before(() => {
    cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
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

  it('Can create a multiple choice question with four options and three correct answers where order matters', function () {
    cy.get('button')
      .contains('New Question')
      .click();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get(
      '[data-cy="questionTitleTextArea"]'
    ).type('Cypress Question Example', { force: true });
    cy.get(
      '[data-cy="questionQuestionTextArea"]'
    ).type('Cypress Question Example - Content', { force: true });

    cy.get('[data-cy="questionOptionsInput"').should('have.length', 4);

    cy.get(`[data-cy="Option1"]`).type('Option 1');
    cy.get(`[data-cy="Option2"]`).type('Option 2');
    cy.get(`[data-cy="Option3"]`).type('Option 3');
    cy.get(`[data-cy="Option4"]`).type('Option 4');

    cy.get(`[data-cy="Switch1"]`).check({ force: true });
    cy.get(`[data-cy="Switch3"]`).check({ force: true });
    cy.get(`[data-cy="Switch2"]`).check({ force: true });

    cy.get('[data-cy="orderMattersSwitch"').check({ force: true });

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button')
      .contains('Save')
      .click();

    cy.wait('@postQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content');
        cy.get('li').each(($el, index, $list) => {
          cy.get($el).should('contain', 'Option ' + (index + 1));
          if ((index + 1) === 1)
            cy.get($el).should('contain', '[1]');
          else if ((index + 1) === 2)
            cy.get($el).should('contain', '[3]');
          else if ((index + 1) === 3)
            cy.get($el).should('contain', '[2]');
        });
      });

    cy.get('button')
      .contains('close')
      .click();
  });
})
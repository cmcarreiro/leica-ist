describe('Create Quiz with Multiple Choice Question Walkthrough', () => {
  beforeEach(() => {
    cy.demoTeacherLogin();
    cy.server();
    cy.route('GET', '/executions/*/quizzes/available').as('getAvailableQuizzes');
    cy.get('[data-cy="managementMenuButton"]').click();
    cy.get('[data-cy="quizzesTeacherMenuButton"]').click();

    cy.wait(1000);
  });

  afterEach(() => {
    cy.logout();
  });

  it('Can create a quiz with a multiple choice question with four options and three correct answers where order matters', function () {
    cy.get('button')
      .contains('New Quiz')
      .click();

    cy.get(
      '[data-cy="quizTitleTextArea"]'
    ).type('Cypress Quiz Example', { force: true });

    cy.get(
      '[data-cy="quizAvailableDataArea"]'
    ).click();

    cy.get('button')
      .contains('Now')
      .click();

    cy.get('tbody>tr')
      .contains('Cypress Question Example')
      .parent()
      .parent()
      .within($list => {
        cy.get(
          '[data-cy="addToQuizButton"]'
        ).click();
      });

    cy.get(
      '[data-cy="saveQuizButton"]'
    ).click();

    //   cy.get('[data-cy="questionOptionsInput"').should('have.length', 4);

    //   cy.get(`[data-cy="Option1"]`).type('Option 1');
    //   cy.get(`[data-cy="Option2"]`).type('Option 2');
    //   cy.get(`[data-cy="Option3"]`).type('Option 3');
    //   cy.get(`[data-cy="Option4"]`).type('Option 4');

    //   cy.get(`[data-cy="Switch1"]`).check({ force: true });
    //   cy.get(`[data-cy="Switch3"]`).check({ force: true });
    //   cy.get(`[data-cy="Switch2"]`).check({ force: true });

    //   cy.get('[data-cy="orderMattersSwitch"').check({ force: true });

    //   cy.route('POST', '/courses/*/questions/').as('postQuestion');

    //   cy.get('button')
    //     .contains('Save')
    //     .click();

    //   cy.wait('@postQuestion')
    //     .its('status')
    //     .should('eq', 200);

    //   cy.get('[data-cy="questionTitleGrid"]')
    //     .first()
    //     .should('contain', 'Cypress Question Example')
    //     .click();

    //   cy.get('[data-cy="showQuestionDialog"]')
    //     .should('be.visible')
    //     .within($ls => {
    //       cy.get('.headline').should('contain', 'Cypress Question Example');
    //       cy.get('span > p').should('contain', 'Cypress Question Example - Content');
    //       cy.get('li').each(($el, index, $list) => {
    //         cy.get($el).should('contain', 'Option ' + (index + 1));
    //         if ((index + 1) === 1)
    //           cy.get($el).should('contain', '[1]');
    //         else if ((index + 1) === 2)
    //           cy.get($el).should('contain', '[3]');
    //         else if ((index + 1) === 3)
    //           cy.get($el).should('contain', '[2]');
    //       });
    //     });

    //   cy.get('button')
    //     .contains('close')
    //     .click();
  });
})
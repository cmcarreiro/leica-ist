describe('Answer Quiz and See Results Walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.get('[data-cy="quizzesStudentMenuButton"]').click();
    cy.get('[data-cy="availableStudentMenuButton"]').click();

    cy.wait(1000);
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('Can answer quiz with a multiple choice question with four options and three correct answers where order matters and see results', () => {
    cy.get('ul>li')
      .contains('Cypress Quiz Example')
      .click();

    cy.wait(1000);

    cy.get('ul>li')
      .contains('Option 1')
      .click();

    cy.get('ul>li')
      .contains('Option 3')
      .click();

    cy.get('ul>li')
      .contains('Option 2')
      .click();

    cy.get('[data-cy="endQuizButton"]').click();
    cy.get('[data-cy="confirmationButton"]').click();
  });
});

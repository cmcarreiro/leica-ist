describe('Manage Multiple Choice Questions Walk-through', () => {
  before(() => {
    cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
    cy.cleanCodeFillInQuestionsByName('Cypress Question Example');
  });

  after(() => {
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

  it('Can create a multiple choice question with four options and only one correct answer', function () {
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

    cy.get('[data-cy="questionOptionsInput"')
      .should('have.length', 4)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          if ((index + 1) === 3) {
            cy.get(`[data-cy="Switch${index + 1}"]`).check({ force: true });
          }
          cy.get(`[data-cy="Option${index + 1}"]`).type('Option ' + (index + 1));
        });
      });

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button')
      .contains('Save')
      .click();

    cy.wait('@postQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 01')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 01');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 01');
        cy.get('li').each(($el, index, $list) => {
          cy.get($el).should('contain', 'Option ' + (index + 1));
          if ((index + 1) === 3) {
            cy.get($el).should('contain', '[★]');
          } else {
            cy.get($el).should('not.contain', '[★]');
          }
        });
      });

    cy.get('button')
      .contains('close')
      .click();
  });

  it('Can create a multiple choice question with four options and three correct answers where order doesnt matter', function () {
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

    cy.get('[data-cy="questionOptionsInput"')
      .should('have.length', 4)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          if ((index + 1) === 1 || (index + 1) === 2 || (index + 1) === 3) {
            cy.get(`[data-cy="Switch${index + 1}"]`).check({ force: true });
          }
          cy.get(`[data-cy="Option${index + 1}"]`).type('Option ' + (index + 1));
        });
      });

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button')
      .contains('Save')
      .click();

    cy.wait('@postQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 02')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 02');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 02');
        cy.get('li').each(($el, index, $list) => {
          cy.get($el).should('contain', 'Option ' + (index + 1));
          if ((index + 1) === 1 || (index + 1) === 2 || (index + 1) === 3) {
            cy.get($el).should('contain', '[★]');
          } else {
            cy.get($el).should('not.contain', '[★]');
          }
        });
      });

    cy.get('button')
      .contains('close')
      .click();
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
    ).type('Cypress Question Example - 03', { force: true });
    cy.get(
      '[data-cy="questionQuestionTextArea"]'
    ).type('Cypress Question Example - Content - 03', { force: true });

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
      .should('contain', 'Cypress Question Example - 03')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 03');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 03');
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

  it('Can view a multiple choice question (with button)', function () {
    cy.get('tbody>tr')
      .contains('Cypress Question Example - 03')
      .parent()
      .parent()
      .within($list => {
        cy.get('button')
          .contains('visibility')
          .click();
      });

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 03');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 03');
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

  it('Can view a multiple choice question (with click)', function () {
    cy.get('[data-cy="questionTitleGrid"]')
      .contains('Cypress Question Example - 03')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 03');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 03');
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

  it('Can update multiple choice question title (with right-click)', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('[data-cy="questionTitleGrid"]')
      .contains('Cypress Question Example - 02')
      .rightclick();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible')
      .within($list => {
        cy.get('span.headline').should('contain', 'Edit Question');

        cy.get('[data-cy="questionTitleTextArea"]')
          .clear({ force: true })
          .type('Cypress Question Example - 02 - Edited', { force: true });

        cy.get('button')
          .contains('Save')
          .click();
      });

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .contains('Cypress Question Example - 02 - Edited')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 02 - Edited');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 02');
        cy.get('li').each(($el, index, $list) => {
          cy.get($el).should('contain', 'Option ' + (index + 1));
          if ((index + 1) === 1 || (index + 1) === 2 || (index + 1) === 3) {
            cy.get($el).should('contain', '[★]');
          } else {
            cy.get($el).should('not.contain', '[★]');
          }
        });
      });

    cy.get('button')
      .contains('close')
      .click();
  });

  it('Can update multiple choice question content (with button)', function () {
    cy.route('PUT', '/questions/*').as('updateQuestion');

    cy.get('tbody>tr')
      .contains('Cypress Question Example - 02 - Edited')
      .parent()
      .parent()
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
          .type('Cypress Question Example - 02 - Edited', { force: true });

        cy.get(`[data-cy="orderMattersSwitch"]`).check({ force: true });

        cy.get(`[data-cy="Option1"]`).type(' - Edited');
        cy.get(`[data-cy="Option2"]`).type(' - Edited');
        cy.get(`[data-cy="Option3"]`).type(' - Edited');
        cy.get(`[data-cy="Option4"]`).type(' - Edited');

        cy.get(`[data-cy="Switch3"]`).uncheck({ force: true });
        cy.get(`[data-cy="Switch4"]`).check({ force: true });

        cy.get(`[data-cy="orderMattersSwitch"]`).check({ force: true });

        cy.get('button')
          .contains('Save')
          .click();
      });

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .contains('Cypress Question Example - 02 - Edited')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 02 - Edited');
        cy.get('span > p').should('contain', 'Cypress Question Example - 02 - Edited');
        cy.get('li').each(($el, index, $list) => {
          cy.get($el).should('contain', 'Option ' + (index + 1));
          if ((index + 1) === 1)
            cy.get($el).should('contain', '[1]');
          else if ((index + 1) === 2)
            cy.get($el).should('contain', '[2]');
          else if ((index + 1) === 4)
            cy.get($el).should('contain', '[3]');
        });
      });

    cy.get('button')
      .contains('close')
      .click();
  });

  it('Can duplicate multiple choice question', function () {
    cy.get('tbody>tr')
      .contains('Cypress Question Example - 03')
      .parent()
      .parent()
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
      .should('have.value', 'Cypress Question Example - 03')
      .type('{end} - DUP', { force: true });
    cy.get('[data-cy="questionQuestionTextArea"]')
      .should('have.value', 'Cypress Question Example - Content - 03');

    cy.get('[data-cy="questionOptionsInput"')
      .should('have.length', 4)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          cy.get('textarea').should('have.value', 'Option ' + (index + 1));
        });
      });

    cy.route('POST', '/courses/*/questions/').as('postQuestion');

    cy.get('button')
      .contains('Save')
      .click();

    cy.wait('@postQuestion')
      .its('status')
      .should('eq', 200);

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .should('contain', 'Cypress Question Example - 03 - DUP');

    cy.get('[data-cy="questionTitleGrid"]')
      .contains('Cypress Question Example - 03 - DUP')
      .click();

    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', 'Cypress Question Example - 03 - DUP');
        cy.get('span > p').should('contain', 'Cypress Question Example - Content - 03');
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

  it('Can delete multiple choice question', function () {
    cy.route('DELETE', '/questions/*').as('deleteQuestion');

    cy.get('tbody>tr')
      .contains('Cypress Question Example - 03')
      .parent()
      .parent()
      .within($list => {
        cy.get('button')
          .contains('delete')
          .click();
      });
  });
})

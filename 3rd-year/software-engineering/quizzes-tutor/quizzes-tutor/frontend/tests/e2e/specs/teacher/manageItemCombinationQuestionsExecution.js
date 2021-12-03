describe('Manage Item Combination Questions Walk-through', () => {
  function validateQuestion(
    title,
    content,
    itemPrefix = 'Item ',
    correctCombinations = '[Item 2]',
    indexWithCombinations
  ) {
    cy.get('[data-cy="showQuestionDialog"]')
      .should('be.visible')
      .within($ls => {
        cy.get('.headline').should('contain', title);
        cy.get('span > p').should('contain', content);
        cy.get('li').each(($el, index, $list) => {
          cy.get($el).should('contain', itemPrefix + index);
          
          if (indexWithCombinations.includes(index)) {
            cy.get($el).should('contain', correctCombinations);
          } else {
            cy.get($el).should('not.contain', correctCombinations);
          }
         
        });
      });
  }

  function validateQuestionFull(
    title,
    content,
    itemPrefix = 'Item ',
    correctCombinations = '[Item 2]',
    indexWithCombinations
  ) {
    cy.log('Validate question with show dialog. ');

    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .click();

    validateQuestion(title, content, itemPrefix, correctCombinations, indexWithCombinations);

    cy.get('button')
      .contains('close')
      .click();
  }

  before(() => {
    cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
    cy.cleanCodeFillInQuestionsByName('Cypress Question Example');
    cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
  });
  after(() => {
    cy.cleanItemCombinationQuestionsByName('Cypress Question Example');
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

  it('Creates a new item combination question', function() {
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
    .type('item_combination', { force: true })
    .click({ force: true });

    cy.wait(1000);


    cy.get('[data-cy="questionItemsInput"')
      .should('have.length', 4)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          cy.get(`[data-cy="Item${index + 1}"]`).type('Item ' + index);
          if(index + 1 < 3){ cy.get(`[data-cy="ItemGroup${index + 1}"]`).click({ force: true }).type('2{enter}', { force: true }); }
          else {cy.get(`[data-cy="ItemGroup${index + 1}"]`).click({ force: true }).type('1{enter}', { force: true }); }
        });
      });

    cy.get('[data-cy="questionItemCombinationsInput"')
      .should('have.length', 2)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          if (index + 1 < 3){ 
            cy.get(`[data-cy="ItemCombination${index + 1}"]`).click({ force: true }).type('{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); }
        });
      });
    
    cy.get('[data-cy="questionItemCombinationsInput"');
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
      'Item ',
      '[Item 2]',
      [0, 1]
    );
  });

  it('Can view question (with button)', function() {
    cy.get('tbody tr')
      .first()
      .within($list => {
        cy.get('button')
          .contains('visibility')
          .click();
      });

      validateQuestion(
        'Cypress Question Example - 01',
        'Cypress Question Example - Content - 01',
        'Item ',
        '[Item 2]',
        [0, 1]
      );

    cy.get('button')
      .contains('close')
      .click();
  });

  it('Can view question (with click)', function() {
    cy.get('[data-cy="questionTitleGrid"]')
      .first()
      .click();

    validateQuestion(
      'Cypress Question Example - 01',
      'Cypress Question Example - Content - 01',
      'Item ',
      '[Item 2]',
      [0, 1]
    );

    cy.get('button')
      .contains('close')
      .click();
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
        'Item ',
        '[Item 2]',
        [0, 1]
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
          .type('Cypress New Content For Question!', { force: true });

        cy.get('button')
          .contains('Save')
          .click();
      });

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 200);

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited',
      'Cypress New Content For Question!',
      'Item ',
      '[Item 2]',
      [0, 1]
    );
  });

  it('Can update item combinations (with button)', function() {
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
      

        cy.get('[data-cy="addItemCombination"]').click({ force: true });
        cy.get(`[data-cy="Item5"]`).type('Item 4');
        cy.get(`[data-cy="ItemGroup5"]`).click({ force: true }).type('1{enter}', { force: true });
        
        
        
      });

      cy.get('[data-cy="questionItemCombinationsInput"')
        .should('have.length', 2)
        .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          if (index + 1 < 3){cy.get(`[data-cy="ItemCombination${index + 1}"]`).click({ force: true }).type('{downarrow}{enter}{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true});}  
        });
        });

        cy.get('button')
        .contains('Save')
        .click();

    cy.wait('@updateQuestion')
      .its('status')
      .should('eq', 200);

    validateQuestionFull(
      'Cypress Question Example - 01 - Edited',
      'Cypress New Content For Question!',
      'Item ',
      '[Item 3]',
      [0, 1]
    );
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
    cy.get('[data-cy="questionQuestionTextArea"]').should(
      'have.value',
      'Cypress New Content For Question!'
    );

    cy.get('[data-cy="questionItemsInput"')
      .should('have.length', 5)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          cy.get('textarea').should('have.value', 'Item ' + index);
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
      .should('contain', 'Cypress Question Example - 01 - Edited - DUP');

      validateQuestionFull(
        'Cypress Question Example - 01 - Edited',
        'Cypress New Content For Question!',
        'Item ',
        '[Item 3]',
        [0, 1]
      );
  });

  it('Can delete created question', function() {
    cy.route('DELETE', '/questions/*').as('deleteQuestion');
    cy.get('tbody tr')
      .first()
      .within($list => {
        cy.get('button')
          .contains('delete')
          .click();
      });

    cy.wait('@deleteQuestion')
      .its('status')
      .should('eq', 200);
  });

  it('Creates a new item combination question with only 2 items', function() {
    cy.get('button')
      .contains('New Question')
      .click();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get(
      '[data-cy="questionTitleTextArea"]'
    ).type('Cypress Question Example - 01 (2 Items)', { force: true });
    cy.get('[data-cy="questionQuestionTextArea"]').type(
      'Cypress Question Example - Content - 01 (2 Items)',
      {
        force: true
      }
    );

    cy.get('[data-cy="questionTypeInput"]')
    .type('item_combination', { force: true })
    .click({ force: true });

    cy.get('[data-cy="questionItemsInput"').should('have.length', 4);

    cy.get(`[data-cy="Item1"]`).type('Item2 0');
    cy.get(`[data-cy="ItemGroup1"]`).click({ force: true }).type('1{enter}', { force: true });
    cy.get(`[data-cy="Item2"]`).type('Item2 1');
    cy.get(`[data-cy="ItemGroup2"]`).click({ force: true }).type('2{enter}', { force: true });
    
    
    cy.get('[data-cy="questionItemCombinationsInput"')
    .should('have.length', 1)
    .each(($el, index, $list) => {
      cy.get($el).within($ls => {
        cy.get(`[data-cy="ItemCombination${index + 1}"]`).click({ force: true }).type('{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); }
        );
      });
      
    cy.get(`[data-cy="Delete4"]`).click({ force: true });
    cy.get(`[data-cy="Delete3"]`).click({ force: true });

 
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
      'Cypress Question Example - 01 (2 Items)',
      'Cypress Question Example - Content - 01 (2 Items)',
      'Item2 ',
      '[Item2 0]',
      [1]
    );
  });

  it('Creates a new item combination question with 6 items', function() {
    cy.get('button')
      .contains('New Question')
      .click();

    cy.get('[data-cy="createOrEditQuestionDialog"]')
      .parent()
      .should('be.visible');

    cy.get('span.headline').should('contain', 'New Question');

    cy.get(
      '[data-cy="questionTitleTextArea"]'
    ).type('Cypress Question Example - 01 (6 Items)', { force: true });
    cy.get('[data-cy="questionQuestionTextArea"]').type(
      'Cypress Question Example - Content - 01 (6 Items)',
      {
        force: true
      }
    );

    cy.get('[data-cy="questionTypeInput"]')
    .type('item_combination', { force: true })
    .click({ force: true });

    cy.wait(1000);

    cy.get('[data-cy="addItemCombination"]').click({ force: true }); // 5
    cy.get('[data-cy="addItemCombination"]').click({ force: true }); // 6

    cy.get('[data-cy="questionItemsInput"')
      .should('have.length', 6)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          cy.get(`[data-cy="Item${index + 1}"]`).type('Item ' + index);
          if(index + 1 < 3){ cy.get(`[data-cy="ItemGroup${index + 1}"]`).click({ force: true }).type('2{enter}', { force: true }); }
          else {cy.get(`[data-cy="ItemGroup${index + 1}"]`).click({ force: true }).type('1{enter}', { force: true }); }
        });
      });

    cy.get('[data-cy="questionItemCombinationsInput"')
      .should('have.length', 2)
      .each(($el, index, $list) => {
        cy.get($el).within($ls => {
          if (index + 1 < 3){ cy.get(`[data-cy="ItemCombination${index + 1}"]`).click({ force: true }).type('{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); }
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
      .should('contain', 'Cypress Question Example - 01');

    validateQuestionFull(
      'Cypress Question Example - 01 (6 Items)',
      'Cypress Question Example - Content - 01 (6 Items)',
      'Item ',
      '[Item 2]',
      [0, 1]
    );
  });
});
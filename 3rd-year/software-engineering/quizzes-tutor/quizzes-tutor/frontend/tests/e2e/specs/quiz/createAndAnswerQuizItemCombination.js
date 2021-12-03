describe('Manage Item Combination Questions Walk-through', () => {
    
  
    before(() => {
      cy.cleanQuizzesItemCombination();
      cy.cleanItemCombinationQuestionsByName('Cypress Question Example - 01');
      cy.cleanItemCombinationQuestionsByName('Cypress Question Example - 02');
    });
    after(() => {
      cy.cleanQuizzesItemCombination();
      cy.cleanItemCombinationQuestionsByName('Cypress Question Example - 01');
      cy.cleanItemCombinationQuestionsByName('Cypress Question Example - 02');
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
      
    });

    it('Teacher creates and student answer an item combination question', function(){
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

        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="questionsTeacherMenuButton"]').click();

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

        cy.createQuizzWith2Questions('Cypress Quiz Example - 01', 'Cypress Question Example - 01', 'Cypress Question Example - 02');
   

        cy.logout();

        cy.demoStudentLogin();
        
        cy.get('[data-cy="quizzesStudentMenuButton"]').click();
        cy.contains('Available').click();

        cy.contains('Cypress Quiz Example - 01').click();

        cy.get(`[data-cy="ItemCombination2"]`).click({ force: true }).type('{downarrow}{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); 
        cy.get(`[data-cy="ItemCombination1"]`).click({ force: true }).type('{downarrow}{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); 
        
        cy.get('[data-cy="nextQuestionButton"]').click();

        cy.get(`[data-cy="ItemCombination2"]`).click({ force: true }).type('{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); 
        cy.get(`[data-cy="ItemCombination1"]`).click({ force: true }).type('{downarrow}{enter}', { force: true }).click({force:true}).type('{esc}', { force:true}); 
        

        cy.get('[data-cy="endQuizButton"]').click();
        cy.get('[data-cy="confirmationButton"]').click();

        cy.wait(1000);

        cy.logout();

        cy.demoTeacherLogin();
        
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="quizzesTeacherMenuButton"]').click();

        cy.contains('Cypress Quiz Example - 01')
        .parent()
        .parent()
        .parent()
        .should('have.length', 1)
        .get('[data-cy="showResults"]').click()
        
        cy.get('tbody tr')
            .first()
            .get('[data-cy="showResultsDetails"]').first()
            .should('have.class', 'answer incorrect');

        cy.get('tbody tr')
            .first()
            .get('[data-cy="showResultsDetails"]').last()
            .should('have.class', 'answer correct');
    });

  
});
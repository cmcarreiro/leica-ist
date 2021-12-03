import QuestionDetails from '@/models/management/questions/QuestionDetails';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';
import MultipleChoiceAnswerDetails from '@/models/management/questions/MultipleChoiceAnswerDetails';
import CodeFillInQuestionDetails from '@/models/management/questions/CodeFillInQuestionDetails';
import CodeFillInAnswerDetails from '@/models/management/questions/CodeFillInAnswerDetails';
import ItemCombinationQuestionDetails from '@/models/management/questions/ItemCombinationQuestionDetails';
import ItemCombinationAnswerDetails from '@/models/management/questions/ItemCombinationAnswerDetails';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';
import OpenAnswerDetails from '@/models/management/questions/OpenAnswerDetails';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';
import MultipleChoiceStatementQuestionDetails from '@/models/statement/questions/MultipleChoiceStatementQuestionDetails';
import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import MultipleChoiceStatementCorrectAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementCorrectAnswerDetails';
import MultipleChoiceStatementAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementAnswerDetails';
import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import CodeFillInStatementQuestionDetails from '@/models/statement/questions/CodeFillInStatementQuestionDetails';
import CodeFillInStatementAnswerDetails from '@/models/statement/questions/CodeFillInStatementAnswerDetails';
import CodeFillInStatementCorrectAnswerDetails from '@/models/statement/questions/CodeFillInStatementCorrectAnswerDetails';
import CodeOrderQuestionDetails from '@/models/management/questions/CodeOrderQuestionDetails';
import CodeOrderAnswerDetails from '@/models/management/questions/CodeOrderAnswerDetails';
import CodeOrderStatementQuestionDetails from '@/models/statement/questions/CodeOrderStatementQuestionDetails';
import CodeOrderStatementAnswerDetails from '@/models/statement/questions/CodeOrderStatementAnswerDetails';
import CodeOrderStatementCorrectAnswerDetails from '@/models/statement/questions/CodeOrderStatementCorrectAnswerDetails';
import ItemCombinationStatementQuestionDetails from '@/models/statement/questions/ItemCombinationStatementQuestionDetails';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';
import ItemCombinationStatementAnswerDetails from '@/models/statement/questions/ItemCombinationStatementAnswerDetails';
import OpenAnswerStatementAnswerDetails from '@/models/statement/questions/OpenAnswerStatementAnswerDetails';
import OpenAnswerStatementQuestionDetails from '@/models/statement/questions/OpenAnswerStatementQuestionDetails';
import OpenAnswerStatementCorrectAnswerDetails from '@/models/statement/questions/OpenAnswerStatementCorrectAnswerDetails';
export enum QuestionTypes {
  MultipleChoice = 'multiple_choice',
  CodeFillIn = 'code_fill_in',
  CodeOrder = 'code_order',
  ItemCombination = 'item_combination',
  OpenAnswer = 'open_answer'
}

export function convertToLetter(number: number | null) {
  if (number == null) {
    return '-';
  } else {
    return String.fromCharCode(65 + number);
  }
}

export abstract class QuestionFactory {
  static getFactory(type: string): QuestionFactory {
    switch (type) {
      case QuestionTypes.MultipleChoice:
        return new MultipleChoiceQuestionFactory();
      case QuestionTypes.CodeFillIn:
        return new CodeFillInQuestionFactory();
      case QuestionTypes.CodeOrder:
        return new CodeOrderQuestionFactory();
      case QuestionTypes.ItemCombination:
        return new ItemCombinationQuestionFactory();
      case QuestionTypes.OpenAnswer:
        return new OpenAnswerQuestionFactory();
      default:
        throw new Error('Unknown question type.');
    }
  }

  abstract createEmptyQuestionDetails(): QuestionDetails;
  abstract createQuestionDetails(question: any): QuestionDetails;
  abstract createAnswerDetails(question: any): AnswerDetails;
  abstract createStatementQuestionDetails(
    question: any
  ): StatementQuestionDetails;
  abstract createStatementAnswerDetails(details: any): StatementAnswerDetails;
  abstract createStatementCorrectAnswerDetails(
    details: any
  ): StatementCorrectAnswerDetails;
}

class MultipleChoiceQuestionFactory extends QuestionFactory {
  createEmptyQuestionDetails(): QuestionDetails {
    return new MultipleChoiceQuestionDetails();
  }
  createQuestionDetails(details: any): QuestionDetails {
    return new MultipleChoiceQuestionDetails(details);
  }
  createAnswerDetails(details: any): AnswerDetails {
    return new MultipleChoiceAnswerDetails(details);
  }
  createStatementQuestionDetails(details: any): StatementQuestionDetails {
    return new MultipleChoiceStatementQuestionDetails(details);
  }
  createStatementAnswerDetails(details: any): StatementAnswerDetails {
    return new MultipleChoiceStatementAnswerDetails(details);
  }
  createStatementCorrectAnswerDetails(
    details: any
  ): StatementCorrectAnswerDetails {
    return new MultipleChoiceStatementCorrectAnswerDetails(details);
  }
}

class ItemCombinationQuestionFactory extends QuestionFactory {
  createEmptyQuestionDetails(): QuestionDetails {
    return new ItemCombinationQuestionDetails();
  }
  createQuestionDetails(details: any): QuestionDetails {
    return new ItemCombinationQuestionDetails(details);
  }
  createAnswerDetails(details: any): AnswerDetails {
    return new ItemCombinationAnswerDetails(details);
  }
  createStatementQuestionDetails(details: any): StatementQuestionDetails {
    return new ItemCombinationStatementQuestionDetails(details);
  }
  createStatementAnswerDetails(details: any): StatementAnswerDetails {
    return new ItemCombinationStatementAnswerDetails(details);
  }
  createStatementCorrectAnswerDetails(
    details: any
  ): StatementCorrectAnswerDetails {
    return new ItemCombinationStatementCorrectAnswerDetails(details);
  }
}

class CodeFillInQuestionFactory extends QuestionFactory {
  createEmptyQuestionDetails(): QuestionDetails {
    return new CodeFillInQuestionDetails();
  }
  createQuestionDetails(details: any): QuestionDetails {
    return new CodeFillInQuestionDetails(details);
  }
  createAnswerDetails(details: any): AnswerDetails {
    return new CodeFillInAnswerDetails(details);
  }
  createStatementQuestionDetails(details: any): StatementQuestionDetails {
    return new CodeFillInStatementQuestionDetails(details);
  }
  createStatementAnswerDetails(details: any): StatementAnswerDetails {
    return new CodeFillInStatementAnswerDetails(details);
  }
  createStatementCorrectAnswerDetails(
    details: any
  ): StatementCorrectAnswerDetails {
    return new CodeFillInStatementCorrectAnswerDetails(details);
  }
}

class CodeOrderQuestionFactory extends QuestionFactory {
  createEmptyQuestionDetails(): QuestionDetails {
    return new CodeOrderQuestionDetails();
  }
  createQuestionDetails(details: any): QuestionDetails {
    return new CodeOrderQuestionDetails(details);
  }
  createAnswerDetails(details: any): AnswerDetails {
    return new CodeOrderAnswerDetails(details);
  }
  createStatementQuestionDetails(details: any): StatementQuestionDetails {
    return new CodeOrderStatementQuestionDetails(details);
  }
  createStatementAnswerDetails(details: any): StatementAnswerDetails {
    return new CodeOrderStatementAnswerDetails(details);
  }
  createStatementCorrectAnswerDetails(
    details: any
  ): StatementCorrectAnswerDetails {
    return new CodeOrderStatementCorrectAnswerDetails(details);
  }
}

class OpenAnswerQuestionFactory extends QuestionFactory {
  createEmptyQuestionDetails(): QuestionDetails {
    return new OpenAnswerQuestionDetails();
  }

  createQuestionDetails(details: any): QuestionDetails {
    return new OpenAnswerQuestionDetails(details);
  }

  createAnswerDetails(details: any): AnswerDetails {
    return new OpenAnswerDetails(details);
  }

  createStatementQuestionDetails(details: any): StatementQuestionDetails {
    return new OpenAnswerStatementQuestionDetails(details);
  }

  createStatementAnswerDetails(details: any): StatementAnswerDetails {
    return new OpenAnswerStatementAnswerDetails(details);
  }

  createStatementCorrectAnswerDetails(
    details: any
  ): StatementCorrectAnswerDetails {
    return new OpenAnswerStatementCorrectAnswerDetails(details);
  }
}

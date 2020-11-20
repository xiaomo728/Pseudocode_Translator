
package translateProgram;

import translateUnits.Function;
import translateUnits.Line;
import translateUnits.Array;
import translateUnits.Variable;
import translators.*;

import java.util.*;
import java.util.regex.Pattern;

public class TranslateSystem {
	// Static class to construct translate system.
	// Important class for whole program.

    public static boolean intMode;

    // store objects.
    public static ArrayList<Variable> variables;
    public static ArrayList<Array> arrays;
    public static ArrayList<Function> functions;

    public static ArrayList<String> keywords = new ArrayList<String>();
    public static LinkedHashMap<String, String> maths = new LinkedHashMap<String, String>();
    public static LinkedHashMap<String, String> operators = new LinkedHashMap<String, String>();
    public static LinkedHashMap<String, String> singleOperators = new LinkedHashMap<String, String>();
    public static LinkedHashMap<String, String> complexOperators = new LinkedHashMap<String, String>();

    public static final String[] endStyleWords = {"IF", "REPEAT", "WHILE", "FOR", "CASE", "WHENEVER", "END"};

    public static boolean sortFlag;
    public static boolean subArrayFlag;
    public static boolean inFunction;

    public static int ifCounter, caseCounter, repeatCounter, whileCounter, forCounter, wheneverCounter;
    public static Stack<String> caseType;
    public static Stack<Integer> forVariablesIndex;
    public static Stack<Integer> repeatLine;
    public static boolean lastLineIsCaseOf;

    public static int uniqueId;

    // function to initialize translate system.
    // initArgs: 0 original initialize;
    //           1 normal initialize;
    //           2 except function set initialize.
    public static void init(int initArgs) {
        if (initArgs == 0) {
            keywords = IOHandler.readData("resources/keywordsData/Keywords.txt");
            maths = IOHandler.readDataMap("resources/keywordsData/Math.txt");
            operators = IOHandler.readDataMap("resources/keywordsData/Operators.txt");
            singleOperators = IOHandler.readDataMap("resources/keywordsData/SingleWordOperators.txt");
            complexOperators = IOHandler.readDataMap("resources/keywordsData/ComplexOperators.txt");

            System.out.println(">>> Initialize the translate system successful... (2/4)");
        }

        variables = new ArrayList<Variable>();
        arrays = new ArrayList<Array>();
        if (initArgs == 0 || initArgs == 1) functions = new ArrayList<Function>();

        sortFlag = false;
        subArrayFlag = false;
        if (initArgs == 0 || initArgs == 1) {
            inFunction = false;
        } else {
            inFunction = true;
        }

        ifCounter = 0;
        caseCounter = 0;
        repeatCounter = 0;
        whileCounter = 0;
        forCounter = 0;
        wheneverCounter = 0;

        caseType = new Stack<String>();
        forVariablesIndex = new Stack<Integer>();
        repeatLine = new Stack<Integer>();
        lastLineIsCaseOf = false;

        uniqueId = 0;
    }

    // function to split the pseudo-code to word by word.
    public static String wordSplitter(String words) {
        String allLines[] = words.split("(\\r\\n|\\r|\\n|\\n\\r)"); // line breakers.
        String splitWords = "";
        for (String line: allLines) {
            if (line.toUpperCase().trim().startsWith("COMMENT:") || line.toUpperCase().trim().startsWith("COMMENT")
                    || line.toUpperCase().trim().startsWith("//") || line.toUpperCase().trim().startsWith("DO")) {
                // skip these special lines (comment & do).
            } else {
                splitWords += line + "\r\n";
            }
        }
        System.out.println("Words need to be split:\n" + splitWords);
        splitWords = splitWords.replaceAll("(\\r\\n|\\r|\\n|\\n\\r|\\t)", " ").replaceAll(" +", " ");
        return splitWords;
    }

    // function to check END-style words by stack.
    public static boolean stackChecker(String words) {
        System.out.println("[Translate System - Start to check completeness use stack]");
        String[] wordsArray = words.split( " ");
        // Use stack to check the conditions' symmetry.
        Stack<String> keywords = new Stack<String>();
        try {
            for (int i = 0; i < wordsArray.length; i++) {
                // Case to avoid "else if" be push into stack.
                if (i != 0 && wordsArray[i].equalsIgnoreCase("IF") && wordsArray[i - 1].equalsIgnoreCase("ELSE")) {
                    wordsArray[i] = "-IF";
                }
                for (String keys: endStyleWords) {
                    if (keys.equalsIgnoreCase(wordsArray[i])) {
                        boolean willpush = false;
                        try {
                            String lastWord = keywords.peek();
                            willpush = !lastWord.equalsIgnoreCase("END");
                        } catch (EmptyStackException e) {
                            // Throw empty stack exception.
                            willpush = true;
                        }
                        if (willpush) {
                            keywords.push(wordsArray[i]);
                            System.out.println("Find keywords: " + wordsArray[i]);
                            System.out.println(">>> push \"" + wordsArray[i] + "\"");
                        } else {
                            String end = keywords.pop();
                            String related = keywords.pop();
                            System.out.println("Find keywords: " + wordsArray[i]);
                            System.out.println("However, 'END' is on the top of stack, pop the related keywords " + related);
                        }
                        System.out.println(">>> stack: " + keywords);
                        break;
                    }
                }
            }
        } catch (EmptyStackException e) {
            System.out.println("Empty stack pop, wrong with keyword scanning.");
            return false;
        }

        return keywords.empty();
    }

    // IMPORTANT function to check and translate each line. 
    public static ArrayList<String> linesChecker(String words, String translateType) {
        ArrayList<String> translatedWords = new ArrayList<String>();
        String lines[] = words.split("(\\r\\n|\\r|\\n|\\n\\r)"); // line breakers.
        int numLine = 0;
        boolean lineTranslated = false;

        for (String eachLine: lines) {
            numLine ++; // count.
            lineTranslated = false;
            // Check each line.
            Line line = new Line(eachLine, translateType);
            System.out.println("\n***[Line " + numLine + "]***");
            System.out.println(">>> Current line: " + line.getContent());

            String lineType = line.checkType();
            System.out.println(">>> Type of line: " + lineType);

            Translator t;

            if (line.getPureContent().replace(" ","").equals("")) {
                System.out.println("Blank Line, skip.");
                translatedWords.add((""));
                continue;
            }

            // Special service for CASE.
            if (lastLineIsCaseOf) {
                if (line.getPureContent().charAt(line.getPureContent().length() - 1) == ':' && caseCounter > 0
                        || line.getPureContent().replace(" ", "").equalsIgnoreCase("OTHERS:")) {
                    // constant_var: (in case statement) / OTHERS:
                    t = new CaseTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    lastLineIsCaseOf = false;
                    continue;

                } else {
                    System.out.println("||| Syntax Error in Line " + numLine + ". >>> Case statement follows an unexpected expression");
                    translatedWords.add(("[ERROR]," + numLine + ",Case statement follows an unexpected expression"));
                    return translatedWords;
                }
            }

            // Start to translate line by line.
            switch (lineType) {
            	// by keywords...
                case "PRINT":
                    t = new PrintTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "DEFINE":
                	// sub structure: sentences after end-style words.
                    if (inSubStructure()) {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> Do not define a variable or array in sub-structure");
                        translatedWords.add(("[ERROR]," + numLine + ",Do not define a variable or array in sub-structure"));
                        return translatedWords;
                    } else {
                        t = new DefineTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;
                    }
                    break;

                case "CREATE":
                    if (inSubStructure()) {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> Do not create a variable or array in sub-structure");
                        translatedWords.add(("[ERROR]," + numLine + ",Do not define a variable or array in sub-structure"));
                        return translatedWords;
                    } else {
                        t = new CreateTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;
                    }
                    break;

                case "OUTPUT":
                    t = new OutputTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "ASSIGN":
                    t = new AssignTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "IF":
                    t = new IfTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "ELSE":
                	// in IF sub structure.
                    if (ifCounter > 0) {
                        t = new IfTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;

                    } else {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> End else condition without if statement");
                        translatedWords.add(("[ERROR]," + numLine + ",Error whenever format"));
                        return translatedWords;
                    }
                    break;

                case "CASE":
                    t = new CaseTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "END":
                    t = new EndTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "FOR":
                    t = new ForTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "WHILE":
                    t = new WhileTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "REPEAT":
                    t = new RepeatTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "UNTIL":
                	// in REPEAT sub structure.
                    if (repeatCounter > 0) {
                        t = new RepeatTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;

                    } else {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> End until condition without repeat statement");
                        translatedWords.add(("[ERROR]," + numLine + ",Error whenever format"));
                        return translatedWords;
                    }
                    break;

                case "WHENEVER":
                	// infinite loop.
                    if (line.getPureContent().equalsIgnoreCase("WHENEVER")) {
                        wheneverCounter ++;
                        if (translateType.equals("Java")) {
                            translatedWords.add((line.indentation() + "while (true) {"));
                        } else {
                            translatedWords.add((line.indentation() + "while (1) {"));
                        }
                        lineTranslated = true;

                    } else {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> Error whenever format");
                        translatedWords.add(("[ERROR]," + numLine + ",Error whenever format"));
                        return translatedWords;
                    }
                    break;

                case "ADD":
                case "LIST":
                    if (inFunction) {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> Cannot use add or list statement in a function");
                        translatedWords.add(("[ERROR]," + numLine + ",Cannot use add or list statement in a function"));
                        return translatedWords;
                    } else {
                        t = new AddListTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;
                    }
                    break;

                case "SWAP":
                    t = new SwapTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "RETURN":
                    if (inFunction) {
                        t = new ReturnTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;
                    } else {
                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> Cannot use return statement out of an non-main function");
                        translatedWords.add(("[ERROR]," + numLine + ",Cannot use return statement out of an non-main function"));
                        return translatedWords;
                    }
                    break;

                case "EXECUTE":
                    t = new ExecuteTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "APPEND":
                    t = new AppendTranslator(numLine, line, translatedWords);
                    t.translateTo(translateType);
                    if (t.hasError()) return t.getPreLines();
                    lineTranslated = true;
                    break;

                case "DO":
                    if (line.getPureContent().equalsIgnoreCase("DO NOTHING")) {
                        translatedWords.add((line.indentation() + "/* program do nothing. */"));
                    } else {
                        String doSentence = line.component(2, -1).trim();
                        translatedWords.add((line.indentation() + "/* " + doSentence + ". */"));
                    }
                    break;

                case "COMMENT:":
                case "COMMENT":
                case "//":
                    String comment = line.component(2, -1);
                    translatedWords.add(line.indentation() + "// " + comment);
                    break;

                default:
                    System.out.println(">>> This is a special line that cannot be recognized by first word.");
                    // Special line that cannot be recognized by first word.
                    if (line.component(2).equals("=")) {
                        System.out.println("    - Variable Assignment.");
                        // variable = value
                        if (Pattern.matches("^[A-Za-z0-9_,\\[\\]]+$", line.checkType())) {
                            t = new AssignTranslator(numLine, line, translatedWords);
                            t.translateTo(translateType);
                            if (t.hasError()) return t.getPreLines();
                            lineTranslated = true;
                        }

                    } else if ((line.component(2).equalsIgnoreCase("INCREMENT") || line.component(2).equalsIgnoreCase("DECREMENT")
                                || line.component(2).equalsIgnoreCase("MULTI_INCREMENT") || line.component(2).equalsIgnoreCase("DIV_DECREMENT"))
                                 && line.component(3).equalsIgnoreCase("BY")) {
                        System.out.println("    - Increment and Decrement.");
                        // increment by, decrement by, multi_increment by, div_decrement by
                        if (Pattern.matches("^[A-Za-z0-9_,\\[\\]]+$", line.checkType()) && line.component(5).equals(Line.ERROR_MSG)) {
                            t = new ByTranslator(numLine, line, translatedWords);
                            t.translateTo(translateType);
                            if (t.hasError()) return t.getPreLines();
                            lineTranslated = true;
                        }

                    } else if (line.getPureContent().toUpperCase().startsWith("SORT ARRAY")) {
                        System.out.println("    -Sort Array.");
                        sortFlag = true;
                        String arr = line.component(3, -1).trim();
                        if (arr.contains(".[") && arr.endsWith("]")) {
                            // subarray arr.[i..j]
                            String subArr = expressionConverter(arr, translateType);
                            if (!subArr.equals(Line.ERROR_MSG)) {
                                subArrayFlag = true;
                                if (translateType.equals("Java")) {
                                    translatedWords.add((line.indentation() + "Arrays.sort(" + subArr + ");"));
                                } else {
                                    translatedWords.add((line.indentation() + "arraySort(" + subArr + ");"));
                                }
                                lineTranslated = true;
                            } else {
                                System.out.println("||| Syntax Error in Line " + numLine + ". >>> Sub-array syntax expression error in sort statement");
                                translatedWords.add(("[ERROR]," + numLine + ",Sub-array syntax expression error in sort statement"));
                                return translatedWords;
                            }

                        } else {
                            for (Array a: arrays) {
                                if (a.getName().equalsIgnoreCase(arr)) {
                                    if (a.getDataType().equalsIgnoreCase("Number")) {
                                        if (translateType.equals("Java")) {
                                            translatedWords.add((line.indentation() + "Arrays.sort(" + a.getName() + ");"));
                                        } else {
                                            translatedWords.add((line.indentation() + "arraySort(" + a.getName() + ");"));
                                        }
                                        lineTranslated = true;
                                    } else {
                                        System.out.println("||| Syntax Error in Line " + numLine + ". >>> Cannot sort a non-number array");
                                        translatedWords.add(("[ERROR]," + numLine + ",Cannot sort a non-number array"));
                                        return translatedWords;
                                    }
                                    break;
                                }
                            }
                        }

                    } else if (line.getPureContent().equalsIgnoreCase("EXIT PROGRAM")) {
                        System.out.println("    - Exit Program.");
                        // EXIT PROGRAM
                        if (translateType.equals("Java")) {
                            translatedWords.add((line.indentation() + "System.exit(0);"));
                        } else {
                            translatedWords.add((line.indentation() + "exit(0);"));
                        }
                        lineTranslated = true;

                    } else if (line.getPureContent().equalsIgnoreCase("STOP LOOP")) {
                        System.out.println("    - Stop Current Loop.");
                        // STOP LOOP
                        if (inLoop()) {
                            translatedWords.add((line.indentation() + "break;"));
                            lineTranslated = true;
                        } else {
                            System.out.println("||| Syntax Error in Line " + numLine + ". >>> Stop loop without a loop");
                            translatedWords.add(("[ERROR]," + numLine + ",Stop loop without a loop"));
                            return translatedWords;
                        }

                    } else if (line.getPureContent().equalsIgnoreCase("GO NEXT LOOP")) {
                        System.out.println("    - Continue to Next Loop.");
                        // GO NEXT LOOP
                        if (inLoop()) {
                            translatedWords.add((line.indentation() + "continue;"));
                            lineTranslated = true;
                        } else {
                            System.out.println("||| Syntax Error in Line " + numLine + ". >>> Go next loop without a loop");
                            translatedWords.add(("[ERROR]," + numLine + ",Go next loop without a loop"));
                            return translatedWords;
                        }

                    } else if (line.getPureContent().charAt(line.getPureContent().length() - 1) == ':' && caseCounter > 0
                            || line.getPureContent().replace(" ", "").equalsIgnoreCase("OTHERS:")) {
                        System.out.println("    - Default Choice of Cases.");
                        // constant_var: (in case statement) / OTHERS:
                        t = new CaseTranslator(numLine, line, translatedWords);
                        t.translateTo(translateType);
                        if (t.hasError()) return t.getPreLines();
                        lineTranslated = true;

                    } else {
                        System.out.println("    - Program don't know the input... TAT");
                    }

            }
        }
        // system do not know the meaning of this line.
        if (!lineTranslated) {
            System.out.println("||| Syntax Error in Line " + numLine + ". >>> Unknown keywords or format of pseudo-code");
            translatedWords.add(("[ERROR]," + numLine + ",Unknown keywords or format of pseudo-code"));
        }

        return translatedWords;
    }

    // function to add header for different translated language.
    public static ArrayList<String> headAdder(ArrayList<String> translatedCode, String translateType) {
        ArrayList<String> fullTranslatedCode = new ArrayList<String>();
        if (translateType.equals("Java")) {
            if (sortFlag) {
                fullTranslatedCode.add(("import java.util.Arrays;\n"));
            }
            fullTranslatedCode.add(("public class Main {"));
            fullTranslatedCode.add(("    public static void main(String[] args) {"));
            for (String codes: translatedCode) {
                fullTranslatedCode.add(codes);
            }
            fullTranslatedCode.add(("    }"));

        } else {
            fullTranslatedCode.add(("#include <stdio.h>"));
            fullTranslatedCode.add(("#include <string.h>"));
            fullTranslatedCode.add(("#include <math.h>"));
            fullTranslatedCode.add(("\nint main() {"));
            for (String codes: translatedCode) {
                fullTranslatedCode.add(codes);
            }
            fullTranslatedCode.add(("\n    return 0;"));
            fullTranslatedCode.add(("}"));
        }

        return fullTranslatedCode;
    }

    // function to convert expression from pseudo-code to code.
    public static String expressionConverter(String value, String translateType) {
        String translatedValue = "";

        if (translateType.equals("Java")) {
        	// To Java.
            System.out.println("[Val to Java]   Start convert: " + value);

            String tempString = stringBuilder(value);

            // Translate all operators.
            for (Map.Entry<String, String> o: complexOperators.entrySet()) {
                if (tempString.toUpperCase().contains(o.getKey())) {
                    if (o.getKey().equals("+") || o.getKey().equals("-") || o.getKey().equals("*") || o.getKey().equals("/") || o.getKey().equals("%")
                            || o.getKey().equals("!=") || o.getKey().equals("==") || o.getKey().equals("<=") || o.getKey().equals(">") || o.getKey().equals(">=")
                            || o.getKey().equals("<") || o.getKey().equals("!")) {
                        // do nothing.
                    } else {
                        System.out.println(">>> Find operator: " + o.getKey());
                        tempString = tempString.replaceAll("(?i)" + o.getKey(), o.getValue());
                        System.out.println(">>> Temp String: " + tempString);
                    }
                }
            }

            String sptOperators[] = tempString.split(" ");
            tempString = "";
            for (Map.Entry<String, String> o: singleOperators.entrySet()) {
                for (int i = 0; i < sptOperators.length; i++) {
                    if (sptOperators[i].toUpperCase().equals(o.getKey())) {
                        sptOperators[i] = o.getValue();
                    }
                }
            }
            for (String s: sptOperators) {
                tempString += s + " ";
            }

            // Translate the equals "[IS] SAME WITH" on string.
            String sptOperators2[] = tempString.split(" ");
            tempString = "";
            for (int i = 0; i < sptOperators2.length; i++) {
                if (sptOperators2[i].toLowerCase().equals(".equals(")) {
                    try {
                        sptOperators2[i + 1] = sptOperators2[i + 1] + " )";
                    } catch (Exception e) {
                        // x.equals() nothing.
                        return Line.ERROR_MSG;
                    }
                } else if (sptOperators2[i].toLowerCase().equals(".equals-(")) {
                    try {
                        sptOperators2[i] = ".equals(";
                        sptOperators2[i - 1] = "! " + sptOperators2[i - 1];
                        sptOperators2[i + 1] = sptOperators2[i + 1] + " )";
                    } catch (Exception e) {
                        // !x.equals() nothing.
                        return Line.ERROR_MSG;
                    }
                }
            }
            for (String s: sptOperators2) {
                tempString += s + " ";
            }

            // Translate IS DIVISIBLE BY.
            if (tempString.toUpperCase().contains("IS DIVISIBLE BY")) {
                System.out.println(">>>Translate \"is divisible by\".");
                tempString = tempString.replaceAll("(?i)IS DIVISIBLE BY", "\\$");
                String sptTempString[] = tempString.split(" ");
                if (sptTempString[0].equals("$")) return Line.ERROR_MSG;
                if (sptTempString[sptTempString.length - 1].equals("$")) return Line.ERROR_MSG;
                String newTempString = "";
                for (int i = 0; i < sptTempString.length; i++) {
                    if (sptTempString[i].equals("$")) {
                        newTempString += "% " + sptTempString[i + 1] + " == 0 ";
                        continue;
                    }
                    if (i > 0) {
                        if(sptTempString[i - 1].equals("$")) {
                            continue;
                        }
                    }
                    newTempString += sptTempString[i] + " ";
                }
                tempString = newTempString;
            }
            System.out.println(">>> Check operators, and translate operators, finished.");

            // Translate all maths.
            String sptTempString[] = tempString.split(" ");
            for (Map.Entry<String, String> m: maths.entrySet()) {
                switch (m.getKey()) {
                    case "RANDOM_NUMBER": case "PI": case "EULE":
                    case "MAX_8BIT": case "MIN_8BIT":
                    case "MAX_16BIT": case "MIN_16BIT":
                    case "MAX_32BIT": case "MIN_32BIT":
                        for (int i = 0; i < sptTempString.length; i++) {
                            if (sptTempString[i].equalsIgnoreCase(m.getKey())) {
                                sptTempString[i] = m.getValue();
                            }
                        }
                        break;
                    default:
                        for (int i = 0; i < sptTempString.length; i++) {
                            if (sptTempString[i].toUpperCase().startsWith(m.getKey() + "(")) {
                                sptTempString[i] = sptTempString[i].replaceAll("(?i)" + m.getKey(), m.getValue());
                            }
                        }
                }
            }
            System.out.println(">>> Check maths, finished.");

            // Translate TRUE FALSE NULL.
            for (int i = 0; i < sptTempString.length; i++) {
                switch(sptTempString[i].toUpperCase()) {
                    case "TRUE":
                        sptTempString[i] = "true"; break;
                    case "FALSE":
                        sptTempString[i] = "false"; break;
                    case "NULL":
                        sptTempString[i] = "null"; break;
                }
            }

            String tempString2 = "";
            for (String s: sptTempString) {
                tempString2 += s + " ";
            }
            System.out.println(">>> Check Booleans, finished.");

            // Format all variables and arrays.
            // Translate all String and Array units.

            tempString2 = bracketsBuilder(tempString2);
            System.out.println("tempString2:" + tempString2);

            String sptTempString2[] = tempString2.split(" ");
            for (int i = 0; i < sptTempString2.length; i++) {
                for (Variable v: variables) {
                    if (sptTempString2[i].equalsIgnoreCase(v.getName())) {
                        sptTempString2[i] = v.getName();
                    }
                    if (sptTempString2[i].startsWith(v.getName()) && !sptTempString2[i].equals(v.getName()) && v.getTrueDataType().equalsIgnoreCase("String")) {

                        System.out.println(">>> Find a suspected String operation.");
                        if (sptTempString2[i].toUpperCase().equals(v.getName().toUpperCase() + ".LENGTH")) {
                        	// str.LENGTH, the length of str.
                            sptTempString2[i] = v.getName() + ".length()";

                        } else if (sptTempString2[i].toUpperCase().startsWith(v.getName().toUpperCase() + ".INDEX[")) {
                        	// str.INDEX[x], the index number of char x of str.
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(v.getName().toUpperCase() + ".INDEX[", "");
                            String tempIndexNum2 = "";
                            int indexNum = -1;
                            if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                try {
                                    indexNum = Integer.parseInt(tempIndexNum2);
                                    if (indexNum >= 0) {
                                        sptTempString2[i] = v.getName() + ".indexOf(\"" + indexNum + "\")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index INDEX[].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");;
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "Java");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = v.getName() + ".indexOf(\"" + result + "\")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index INDEX[].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index INDEX[].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }

                        } else if (sptTempString2[i].toUpperCase().equals(v.getName().toUpperCase() + ".UPPERCASE")) {
                        	// str.UPPERCASE.
                            sptTempString2[i] = v.getName() + ".toUpperCase()";

                        } else if (sptTempString2[i].toUpperCase().equals(v.getName().toUpperCase() + ".LOWERCASE")) {
                        	// str.LOWERCASE.
                            sptTempString2[i] = v.getName() + ".toLowerCase()";

                        } else if (sptTempString2[i].toUpperCase().startsWith(v.getName().toUpperCase() + "[")) {
                        	// str[x], the char at index x of str.
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(v.getName().toUpperCase() + "[", "");
                            String tempIndexNum2 = "";
                            int indexNum = -1;
                            if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                try {
                                    indexNum = Integer.parseInt(tempIndexNum2);
                                    if (indexNum >= 0) {
                                        sptTempString2[i] = "String.valueOf(" + v.getName() + ".charAt(" + indexNum + "))";
                                    } else {
                                        System.out.println(">>> User do not give a correct index [].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");;
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "Java");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = "String.valueOf(" + v.getName() + ".charAt(" + result + "))";
                                        } else {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index [].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }

                        } else if (sptTempString2[i].toUpperCase().startsWith(v.getName().toUpperCase() + ".[")) {
                        	// str.[x..y], sub string of str.
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(v.getName().toUpperCase() + ".[", "");
                            String tempIndexNum2 = "";
                            int index1 = -1, index2 = -1;

                            if (tempIndexNum1.endsWith("..]")) {
                                tempIndexNum2 = tempIndexNum1.replace("..]", "");
                                try {
                                    index1 = Integer.parseInt(tempIndexNum2);
                                    if (index1 >= 0) {
                                        sptTempString2[i] = v.getName() + ".substring(" + index1 + ")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");;
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "Java");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = v.getName() + ".substring(" + result + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                if (tempIndexNum2.contains("..")) {
                                    String tempIndexNum3[] = tempIndexNum2.split("\\.\\.");
                                    try {
                                        index1 = Integer.parseInt(tempIndexNum3[0]);
                                        index2 = Integer.parseInt(tempIndexNum3[1]);
                                        if (index1 >= 0 && index2 >= 0 && index2 >= index1) {
                                            sptTempString2[i] = v.getName() + ".substring(" + index1 + ", " + index2 + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index.");
                                            return Line.ERROR_MSG;
                                        }
                                    } catch (Exception e) {
                                        if (tempIndexNum3[0].contains("_")) tempIndexNum3[0] = tempIndexNum3[0].replace("_", " ");;
                                        if (tempIndexNum3[1].contains("_")) tempIndexNum3[1] = tempIndexNum3[1].replace("_", " ");;
                                        if (valueJudge(tempIndexNum3[0], "Number") == 1 && valueJudge(tempIndexNum3[1], "Number") == 1) {
                                            String result1 = expressionConverter(tempIndexNum3[0], "Java");
                                            String result2 = expressionConverter(tempIndexNum3[1], "Java");
                                            if (!result1.equals(Line.ERROR_MSG) && !result2.equals(Line.ERROR_MSG)) {
                                                sptTempString2[i] = v.getName() + ".substring(" + result1 + ", " + result2 + ")";
                                            } else {
                                                System.out.println(">>> User do not give a correct index .[].");
                                                return Line.ERROR_MSG;
                                            }
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }

                        }

                    }
                }

                for (Array a: arrays) {
                    if (sptTempString2[i].equalsIgnoreCase(a.getName())) {
                        sptTempString2[i] = a.getName();
                    }
                    if (sptTempString2[i].startsWith(a.getName()) && !sptTempString2[i].equalsIgnoreCase(a.getName())) {
                        System.out.println(">>> Find a suspected Array operation.");
                        if (sptTempString2[i].toUpperCase().equals(a.getName().toUpperCase() + ".LENGTH")) {
                        	// arr.LENGTH, the length of arr.
                            sptTempString2[i] = a.getName() + ".length";

                        } else if (sptTempString2[i].toUpperCase().startsWith(a.getName().toUpperCase() + "[")) {
                        	// arr[x], the element at index x of arr.
                        	// arr[x][y], 2D array.

                            if (!sptTempString2[i].contains("][")) {
                                // 1D array.
                                System.out.println(">>> 1D Array convert.");
                                if (a.is2DArray()) return Line.ERROR_MSG;
                                String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(a.getName().toUpperCase() + "[", "");
                                String tempIndexNum2 = "";
                                int indexNum = -1;
                                if (tempIndexNum1.endsWith("]")) {
                                    tempIndexNum2 = tempIndexNum1.replace("]", "");
                                    try {
                                        indexNum = Integer.parseInt(tempIndexNum2);
                                        if (indexNum < 0) {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        } else {
                                            sptTempString2[i] = a.getName() + "[" + indexNum + "]";
                                        }
                                    } catch (NumberFormatException e) {
                                        if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");;
                                        // for each need improved?
                                        if (valueJudge(tempIndexNum2, "Number") == 1) {
                                            String result = expressionConverter(tempIndexNum2, "Java");
                                            if (result.equals(Line.ERROR_MSG)) {
                                                System.out.println(">>> User do not give a correct index [].");
                                                return Line.ERROR_MSG;
                                            } else {
                                                sptTempString2[i] = a.getName() + "[" + result + "]";
                                            }
                                        } else {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        }
                                    }

                                } else {
                                    return Line.ERROR_MSG;
                                }

                            } else {
                                // 2D array.
                                System.out.println(">>> 2D Array convert.");
                                if (!a.is2DArray()) return Line.ERROR_MSG;
                                String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(a.getName().toUpperCase() + "[", "");
                                String tempIndexNum2 = "", tempIndexNum3 = "", tempIndexNum4 = "";
                                int indexNum = -1;
                                if (tempIndexNum1.endsWith("]")) {
                                    tempIndexNum2 = tempIndexNum1.substring(0, tempIndexNum1.length() - 1);
                                    if (tempIndexNum2.contains("][")) {
                                        try {
                                            tempIndexNum3 = tempIndexNum2.split("]\\[")[0].trim();
                                            tempIndexNum4 = tempIndexNum2.split("]\\[")[1].trim();
                                        } catch (Exception e) {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        }
                                        try {
                                            int indexNum1 = Integer.parseInt(tempIndexNum3);
                                            int indexNum2 = Integer.parseInt(tempIndexNum4);
                                            if (indexNum1 < 0 || indexNum2 < 0) {
                                                System.out.println(">>> User do not give a correct index [].");
                                                return Line.ERROR_MSG;
                                            } else {
                                                sptTempString2[i] = a.getName() + "[" + indexNum1 + "][" + indexNum2 +"]";
                                            }
                                        } catch (NumberFormatException e) {
                                            if (tempIndexNum3.contains("_")) tempIndexNum3 = tempIndexNum3.replace("_", " ");;
                                            if (tempIndexNum4.contains("_")) tempIndexNum4 = tempIndexNum4.replace("_", " ");;
                                            if (valueJudge(tempIndexNum3, "Number") == 1 && valueJudge(tempIndexNum4, "Number") == 1) {
                                                String result1 = expressionConverter(tempIndexNum3, "Java");
                                                String result2 = expressionConverter(tempIndexNum4, "Java");
                                                if (result1.equals(Line.ERROR_MSG) || result2.equals(Line.ERROR_MSG)) {
                                                    System.out.println(">>> User do not give a correct index [].");
                                                    return Line.ERROR_MSG;
                                                } else {
                                                    sptTempString2[i] = a.getName() + "[" + result1 + "][" + result2 + "]";
                                                }
                                            } else {
                                                System.out.println(">>> User do not give a correct index [].");
                                                return Line.ERROR_MSG;
                                            }
                                        }

                                    } else {
                                        System.out.println(">>> User do not give a correct index [].");
                                        return Line.ERROR_MSG;
                                    }
                                }
                            }
                        } else if (sptTempString2[i].toUpperCase().startsWith(a.getName().toUpperCase() + ".[")) {
                            // arr.[x..y], sub-array of arr.
                            subArrayFlag = true;
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(a.getName().toUpperCase() + ".[", "");
                            String tempIndexNum2 = "";
                            int index1 = -1, index2 = -1;

                            if (tempIndexNum1.endsWith("..]")) {
                                tempIndexNum2 = tempIndexNum1.replace("..]", "");
                                try {
                                    index1 = Integer.parseInt(tempIndexNum2);
                                    if (index1 >= 0) {
                                        sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + index1 + ")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");;
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "Java");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + result + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                if (tempIndexNum2.contains("..")) {
                                    String tempIndexNum3[] = tempIndexNum2.split("\\.\\.");
                                    try {
                                        index1 = Integer.parseInt(tempIndexNum3[0]);
                                        index2 = Integer.parseInt(tempIndexNum3[1]);
                                        if (index1 >= 0 && index2 >= 0 && index2 >= index1) {
                                            sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + index1 + ", " + index2 + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index.");
                                            return Line.ERROR_MSG;
                                        }
                                    } catch (Exception e) {
                                        if (tempIndexNum3[0].contains("_")) tempIndexNum3[0] = tempIndexNum3[0].replace("_", " ");
                                        if (tempIndexNum3[1].contains("_")) tempIndexNum3[1] = tempIndexNum3[1].replace("_", " ");
                                        if (valueJudge(tempIndexNum3[0], "Number") == 1 && valueJudge(tempIndexNum3[1], "Number") == 1) {
                                            String result1 = expressionConverter(tempIndexNum3[0], "Java");
                                            String result2 = expressionConverter(tempIndexNum3[1], "Java");
                                            if (!result1.equals(Line.ERROR_MSG) && !result2.equals(Line.ERROR_MSG)) {
                                                sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + result1 + ", " + result2 + ")";
                                            } else {
                                                System.out.println(">>> User do not give a correct index .[].");
                                                return Line.ERROR_MSG;
                                            }
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }
                        }
                    }
                }
            }

            String tempString3 = "";
            for (String s: sptTempString2) {
                tempString3 += s + " ";
            }
            System.out.println(">>> Check String operators, finished.");

            translatedValue = stringLiberator(bracketsLiberator(tempString3));
            // Finish translation.


        } else {
        	// To C.
            System.out.println("[Val to C]   Start convert: " + value);

            String tempString = stringBuilder(value);
            // Translate all operators.
            for (Map.Entry<String, String> o: complexOperators.entrySet()) {
                if (tempString.toUpperCase().contains(o.getKey())) {
                    if (o.getKey().equals("+") || o.getKey().equals("-") || o.getKey().equals("*") || o.getKey().equals("/") || o.getKey().equals("%")
                            || o.getKey().equals("!=") || o.getKey().equals("==") || o.getKey().equals("<=") || o.getKey().equals(">") || o.getKey().equals(">=")
                            || o.getKey().equals("<") || o.getKey().equals("!")) {
                        // do nothing.

                    } else {
                        System.out.println(">>> Find operator: " + o.getKey());
                        if (o.getValue().contains("true") || o.getValue().contains("false")) {
                            tempString = tempString.replaceAll("(?i)" + o.getKey(), o.getValue().replace("true", "1").replace("false", "0"));
                        }
                        tempString = tempString.replaceAll("(?i)" + o.getKey(), o.getValue());
                        System.out.println(">>> Temp String: " + tempString);
                    }
                }
            }

            String sptOperators[] = tempString.split(" ");
            tempString = "";
            for (Map.Entry<String, String> o: singleOperators.entrySet()) {
                for (int i = 0; i < sptOperators.length; i++) {
                    if (sptOperators[i].toUpperCase().equals(o.getKey())) {
                        sptOperators[i] = o.getValue();
                    }
                }
            }
            for (String s: sptOperators) {
                tempString += s + " ";
            }

            // Translate the equals "[IS] SAME WITH" on string.
            String sptOperators2[] = tempString.split(" ");
            tempString = "";
            for (int i = 0; i < sptOperators2.length; i++) {
                if (sptOperators2[i].toLowerCase().equals(".equals(")) {
                    try {
                        sptOperators2[i - 1] = "strcmp( " + sptOperators2[i - 1];
                        sptOperators2[i] = ", ";
                        sptOperators2[i + 1] = sptOperators2[i + 1] + " ) == 0 ";
                    } catch (Exception e) {
                        // x.equals() nothing.
                        return Line.ERROR_MSG;
                    }
                } else if (sptOperators2[i].toLowerCase().equals(".equals-(")) {
                    try {
                        sptOperators2[i - 1] = "strcmp( " + sptOperators2[i - 1];
                        sptOperators2[i] = ", ";
                        sptOperators2[i + 1] = sptOperators2[i + 1] + " ) != 0 ";
                    } catch (Exception e) {
                        // !x.equals() nothing.
                        return Line.ERROR_MSG;
                    }
                }
            }
            for (String s: sptOperators2) {
                tempString += s + " ";
            }

            // Translate IS DIVISIBLE BY.
            if (tempString.toUpperCase().contains("IS DIVISIBLE BY")) {
                System.out.println(">>>Translate \"is divisible by\".");
                tempString = tempString.replaceAll("(?i)IS DIVISIBLE BY", "\\$");
                String sptTempString[] = tempString.split(" ");
                if (sptTempString[0].equals("$")) return Line.ERROR_MSG;
                if (sptTempString[sptTempString.length - 1].equals("$")) return Line.ERROR_MSG;
                String newTempString = "";
                for (int i = 0; i < sptTempString.length; i++) {
                    if (sptTempString[i].equals("$")) {
                        newTempString += "% " + sptTempString[i + 1] + " == 0 ";
                        continue;
                    }
                    if (i > 0) {
                        if(sptTempString[i - 1].equals("$")) {
                            continue;
                        }
                    }
                    newTempString += sptTempString[i] + " ";
                }
                tempString = newTempString;
            }
            System.out.println(">>> Check operators, and translate operators, finished.");

            // Translate all maths.
            String sptTempString[] = tempString.split(" ");
            for (Map.Entry<String, String> m: maths.entrySet()) {
                switch (m.getKey()) {
                    case "RANDOM_NUMBER": case "PI": case "EULE":
                    case "MAX_8BIT": case "MIN_8BIT":
                    case "MAX_16BIT": case "MIN_16BIT":
                    case "MAX_32BIT": case "MIN_32BIT":
                        for (int i = 0; i < sptTempString.length; i++) {
                            if (sptTempString[i].equalsIgnoreCase(m.getKey())) {
                                sptTempString[i] = m.getValue().replace("Math.", "");
                            }
                        }
                        break;
                    default:
                        for (int i = 0; i < sptTempString.length; i++) {
                            if (sptTempString[i].toUpperCase().startsWith(m.getKey() + "(")) {
                                sptTempString[i] = sptTempString[i].toUpperCase().replace(m.getKey(), m.getValue().replace("Math.", ""));
                            }
                        }
                }
            }
            System.out.println(">>> Check maths, finished.");

            // Translate TRUE FALSE NULL.
            for (int i = 0; i < sptTempString.length; i++) {
                switch(sptTempString[i].toUpperCase()) {
                    case "TRUE":
                        sptTempString[i] = "1"; break;
                    case "FALSE":
                        sptTempString[i] = "0"; break;
                    case "NULL":
                        sptTempString[i] = "NULL"; break;
                }
            }

            String tempString2 = "";
            for (String s: sptTempString) {
                tempString2 += s + " ";
            }
            System.out.println(">>> Check Booleans, finished.");

            // Format all variables and arrays.
            // Translate all String and Array units.
            // Similar to the procedures when translating to Java.

            tempString2 = bracketsBuilder(tempString2);

            String sptTempString2[] = tempString2.split(" ");
            for (int i = 0; i < sptTempString2.length; i++) {
                for (Variable v: variables) {
                    if (sptTempString2[i].equalsIgnoreCase(v.getName())) {
                        sptTempString2[i] = v.getName();
                    }
                    if (sptTempString2[i].startsWith(v.getName()) && !sptTempString2[i].equals(v.getName()) && v.getTrueDataType().equalsIgnoreCase("String")) {

                        System.out.println(">>> Find a suspected String operation.");
                        if (sptTempString2[i].toUpperCase().equals(v.getName().toUpperCase() + ".LENGTH")) {
                        	// str.LENGTH, the length of str.
                            sptTempString2[i] = "sizeof(" + v.getName() + ")";

                        } else if (sptTempString2[i].toUpperCase().startsWith(v.getName().toUpperCase() + ".INDEX[")) {
                        	// str.INDEX[x], the index number of char x of str.
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(v.getName().toUpperCase() + ".INDEX[", "");
                            String tempIndexNum2 = "";
                            int indexNum = -1;
                            if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                try {
                                    indexNum = Integer.parseInt(tempIndexNum2);
                                    if (indexNum >= 0) {
                                        sptTempString2[i] = "indexOf(\"" + indexNum + "\", " + v.getName() + ")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index INDEX[].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "C");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = "indexOf(\"" + result + "\", " + v.getName() + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index INDEX[].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index INDEX[].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }

                        } else if (sptTempString2[i].toUpperCase().equals(v.getName().toUpperCase() + ".UPPERCASE")) {
                        	// str.UPPERCASE.
                            sptTempString2[i] = "upper(" + v.getName() + ")";

                        } else if (sptTempString2[i].toUpperCase().equals(v.getName().toUpperCase() + ".LOWERCASE")) {
                        	// str.LOWERCASE.
                            sptTempString2[i] = "lower(" + v.getName() + ")";

                        } else if (sptTempString2[i].toUpperCase().startsWith(v.getName().toUpperCase() + "[")) {
                        	// str[x], the char at index x of str.
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(v.getName().toUpperCase() + "[", "");
                            String tempIndexNum2 = "";
                            int indexNum = -1;
                            if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                try {
                                    indexNum = Integer.parseInt(tempIndexNum2);
                                    if (indexNum >= 0) {
                                        sptTempString2[i] = "charOf(" + v.getName() + ", " + indexNum + ")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index [].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "C");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = "charOf(" + v.getName() + ", " + result + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index [].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }

                        } else if (sptTempString2[i].toUpperCase().startsWith(v.getName().toUpperCase() + ".[")) {
                        	// str.[x..y], sub string of str.
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(v.getName().toUpperCase() + ".[", "");
                            String tempIndexNum2 = "";
                            int index1 = -1, index2 = -1;

                            if (tempIndexNum1.endsWith("..]")) {
                                tempIndexNum2 = tempIndexNum1.replace("..]", "");
                                try {
                                    index1 = Integer.parseInt(tempIndexNum2);
                                    if (index1 >= 0) {
                                        sptTempString2[i] = "substringOf(" + v.getName() + ", " + index1 + ")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "C");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = "substringOf(" + v.getName() + ", " + result + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                if (tempIndexNum2.contains("..")) {
                                    String tempIndexNum3[] = tempIndexNum2.split("\\.\\.");
                                    try {
                                        index1 = Integer.parseInt(tempIndexNum3[0]);
                                        index2 = Integer.parseInt(tempIndexNum3[1]);
                                        if (index1 >= 0 && index2 >= 0 && index2 >= index1) {
                                            sptTempString2[i] = "substringOf(" + v.getName() + ", " + index1 + ", " + index2 + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index.");
                                            return Line.ERROR_MSG;
                                        }
                                    } catch (Exception e) {
                                        if (tempIndexNum3[0].contains("_")) tempIndexNum3[0] = tempIndexNum3[0].replace("_", " ");
                                        if (tempIndexNum3[1].contains("_")) tempIndexNum3[1] = tempIndexNum3[1].replace("_", " ");
                                        if (valueJudge(tempIndexNum3[0], "Number") == 1 && valueJudge(tempIndexNum3[1], "Number") == 1) {
                                            String result1 = expressionConverter(tempIndexNum3[0], "C");
                                            String result2 = expressionConverter(tempIndexNum3[1], "C");
                                            if (!result1.equals(Line.ERROR_MSG) && !result2.equals(Line.ERROR_MSG)) {
                                                sptTempString2[i] = "substringOf(" + v.getName() + ", " + result1 + ", " + result2 + ")";
                                            } else {
                                                System.out.println(">>> User do not give a correct index .[].");
                                                return Line.ERROR_MSG;
                                            }
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }

                        }

                    }
                }

                for (Array a: arrays) {
                    if (sptTempString2[i].equalsIgnoreCase(a.getName())) {
                        sptTempString2[i] = a.getName();
                    }
                    if (sptTempString2[i].startsWith(a.getName()) && !sptTempString2[i].equalsIgnoreCase(a.getName())) {
                        System.out.println(">>> Find a suspected Array operation.");
                        if (sptTempString2[i].toUpperCase().equals(a.getName().toUpperCase() + ".LENGTH")) {
                        	// arr.LENGTH, the length of arr.
                            sptTempString2[i] = "sizeof(" + a.getName() + ")";

                        } else if (sptTempString2[i].toUpperCase().startsWith(a.getName().toUpperCase() + "[")) {
                        	// arr[x], the element at index x of arr.
                        	// arr[x][y], 2D array.

                            if (!sptTempString2[i].contains("][")) {
                                // 1D array.
                                System.out.println(">>> 1D Array convert.");
                                if (a.is2DArray()) return Line.ERROR_MSG;
                                String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(a.getName().toUpperCase() + "[", "");
                                String tempIndexNum2 = "";
                                int indexNum = -1;
                                if (tempIndexNum1.endsWith("]")) {
                                    tempIndexNum2 = tempIndexNum1.replace("]", "");
                                    try {
                                        indexNum = Integer.parseInt(tempIndexNum2);
                                        if (indexNum < 0) {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        } else {
                                            sptTempString2[i] = a.getName() + "[" + indexNum + "]";
                                        }
                                    } catch (NumberFormatException e) {
                                        if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");
                                        if (valueJudge(tempIndexNum2, "Number") == 1) {
                                            String result = expressionConverter(tempIndexNum2, "C");
                                            if (result.equals(Line.ERROR_MSG)) {
                                                System.out.println(">>> User do not give a correct index [].");
                                                return Line.ERROR_MSG;
                                            } else {
                                                sptTempString2[i] = a.getName() + "[" + result + "]";
                                            }
                                        } else {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        }
                                    }

                                } else {
                                    return Line.ERROR_MSG;
                                }

                            } else {
                                // 2D array.
                                System.out.println(">>> 2D Array convert.");
                                if (!a.is2DArray()) return Line.ERROR_MSG;
                                String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(a.getName().toUpperCase() + "[", "");
                                String tempIndexNum2 = "", tempIndexNum3 = "", tempIndexNum4 = "";
                                int indexNum = -1;
                                if (tempIndexNum1.endsWith("]")) {
                                    tempIndexNum2 = tempIndexNum1.substring(0, tempIndexNum1.length() - 1);
                                    if (tempIndexNum2.contains("][")) {
                                        try {
                                            tempIndexNum3 = tempIndexNum2.split("]\\[")[0].trim();
                                            tempIndexNum4 = tempIndexNum2.split("]\\[")[1].trim();
                                        } catch (Exception e) {
                                            System.out.println(">>> User do not give a correct index [].");
                                            return Line.ERROR_MSG;
                                        }
                                        try {
                                            int indexNum1 = Integer.parseInt(tempIndexNum3);
                                            int indexNum2 = Integer.parseInt(tempIndexNum4);
                                            if (indexNum1 < 0 || indexNum2 < 0) {
                                                System.out.println(">>> User do not give a correct index [].");
                                                return Line.ERROR_MSG;
                                            } else {
                                                sptTempString2[i] = a.getName() + "[" + indexNum1 + "][" + indexNum2 +"]";
                                            }
                                        } catch (NumberFormatException e) {
                                            if (tempIndexNum3.contains("_")) tempIndexNum3 = tempIndexNum3.replace("_", " ");
                                            if (tempIndexNum4.contains("_")) tempIndexNum4 = tempIndexNum4.replace("_", " ");
                                            if (valueJudge(tempIndexNum3, "Number") == 1 && valueJudge(tempIndexNum4, "Number") == 1) {
                                                String result1 = expressionConverter(tempIndexNum3, "C");
                                                String result2 = expressionConverter(tempIndexNum4, "C");
                                                if (result1.equals(Line.ERROR_MSG) || result2.equals(Line.ERROR_MSG)) {
                                                    System.out.println(">>> User do not give a correct index [].");
                                                    return Line.ERROR_MSG;
                                                } else {
                                                    sptTempString2[i] = a.getName() + "[" + result1 + "][" + result2 + "]";
                                                }
                                            } else {
                                                System.out.println(">>> User do not give a correct index [].");
                                                return Line.ERROR_MSG;
                                            }
                                        }

                                    } else {
                                        System.out.println(">>> User do not give a correct index [].");
                                        return Line.ERROR_MSG;
                                    }
                                }
                            }
                        } else if (sptTempString2[i].toUpperCase().startsWith(a.getName().toUpperCase() + ".[")) {
                            // arr.[x..y], sub-array of arr.
                            subArrayFlag = true;
                            String tempIndexNum1 = sptTempString2[i].toUpperCase().replace(a.getName().toUpperCase() + ".[", " ");
                            String tempIndexNum2 = "";
                            int index1 = -1, index2 = -1;

                            if (tempIndexNum1.endsWith("..]")) {
                                tempIndexNum2 = tempIndexNum1.replace("..]", "");
                                try {
                                    index1 = Integer.parseInt(tempIndexNum2);
                                    if (index1 >= 0) {
                                        sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + index1 + ")";
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                } catch (NumberFormatException e) {
                                    if (tempIndexNum2.contains("_")) tempIndexNum2 = tempIndexNum2.replace("_", " ");
                                    if (valueJudge(tempIndexNum2, "Number") == 1) {
                                        String result = expressionConverter(tempIndexNum2, "C");
                                        if (!result.equals(Line.ERROR_MSG)) {
                                            sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + result + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    } else {
                                        System.out.println(">>> User do not give a correct index .[].");
                                        return Line.ERROR_MSG;
                                    }
                                }

                            } else if (tempIndexNum1.endsWith("]")) {
                                tempIndexNum2 = tempIndexNum1.replace("]", "");
                                if (tempIndexNum2.contains("..")) {
                                    String tempIndexNum3[] = tempIndexNum2.split("\\.\\.");
                                    try {
                                        index1 = Integer.parseInt(tempIndexNum3[0]);
                                        index2 = Integer.parseInt(tempIndexNum3[1]);
                                        if (index1 >= 0 && index2 >= 0 && index2 >= index1) {
                                            sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + index1 + ", " + index2 + ")";
                                        } else {
                                            System.out.println(">>> User do not give a correct index.");
                                            return Line.ERROR_MSG;
                                        }
                                    } catch (Exception e) {
                                        if (tempIndexNum3[0].contains("_")) tempIndexNum3[0] = tempIndexNum3[0].replace("_", " ");
                                        if (tempIndexNum3[1].contains("_")) tempIndexNum3[1] = tempIndexNum3[1].replace("_", " ");
                                        if (valueJudge(tempIndexNum3[0], "Number") == 1 && valueJudge(tempIndexNum3[1], "Number") == 1) {
                                            String result1 = expressionConverter(tempIndexNum3[0], "C");
                                            String result2 = expressionConverter(tempIndexNum3[1], "C");
                                            if (!result1.equals(Line.ERROR_MSG) && !result2.equals(Line.ERROR_MSG)) {
                                                sptTempString2[i] = "subArrayOf(" + a.getName() + ", " + result1 + ", " + result2 + ")";
                                            } else {
                                                System.out.println(">>> User do not give a correct index .[].");
                                                return Line.ERROR_MSG;
                                            }
                                        } else {
                                            System.out.println(">>> User do not give a correct index .[].");
                                            return Line.ERROR_MSG;
                                        }
                                    }
                                }

                            } else {
                                return Line.ERROR_MSG;
                            }
                        }
                    }
                }
            }

            String tempString3 = "";
            for (String s: sptTempString2) {
                tempString3 += s + " ";
            }
            System.out.println(">>> Check String operators, finished.");

            translatedValue = stringLiberator(bracketsLiberator(tempString3));
            // Finished translation.
        }

        return translatedValue;
    }
    
    // function to judge the data type of value.
    public static int valueJudge(String value, String dataType)  {
        int priority = 1;
        // priority 0 wrong data type.
        //          1 normal data type.
        //          2 "double".

        String handledValue = stringBuilder(value);
        if (handledValue.equals(Line.ERROR_MSG)) return 0;

        String[] val = handledValue.split(" ");
        if (dataType.equalsIgnoreCase("Number")) {
            // if a variable declare is a [Number]:
            // for operators, it only contains: PLUS(+), MINUS(-), TIMES(*), DIVS(/), MOD(%), delete them.
            // for variables, it only contains variable with Number type and String str.LENGTH, String str.INDEX
            // for arrays, it only contains arr[i] with Number array, arr.LENGTH.
            // for maths, it all contains.
            // check the remaining, it will only contains numbers.
            for (String o : TranslateSystem.operators.keySet()) {
                switch (o.toUpperCase()) {
                    case "PLUS":
                    case "+":
                    case "MINUS":
                    case "-":
                    case "TIMES":
                    case "*":
                    case "DIVS":
                    case "/":
                    case "MOD":
                    case "%":
                        for (int i = 0; i < val.length; i++) {
                            if (val[i].equalsIgnoreCase(o)) {
                                val[i] = "$";
                            }
                        }
                        break;
                    default:
                        if (handledValue.contains(" " + o + " ")) {
                            System.out.println(">>> Wrong operators exist.");
                            System.out.println(">>> o:" + o);
                            return 0;
                        }
                }
            }
            System.out.println(">>> After checking operators, " + java.util.Arrays.toString(val));

            for (Variable var : TranslateSystem.variables) {
                for (int i = 0; i < val.length; i++) {
                    if (val[i].equalsIgnoreCase(var.getName())) {
                        if (!var.getDataType().equalsIgnoreCase("Number")) {
                            System.out.println(">>> Contains non-number variable.");
                            return 0;
                        } else {
                            val[i] = "$";
                            if (var.getTrueDataType().equalsIgnoreCase("double")) {
                                priority = 2;
                            }
                        }
                    }
                    if (val[i].toUpperCase().equals(var.getName().toUpperCase() + ".LENGTH") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".INDEX")) {
                        if (var.getDataType().equalsIgnoreCase("String")) {
                            val[i] = "$";
                        } else {
                            System.out.println(">>> Wrong use on str.LENGTH, str.INDEX");
                            return 0;
                        }
                    }
                    if (val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".[") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + "[")
                            || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".UPPERCASE") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".LOWERCASE")) {
                        System.out.println(">>> Wrong use on str.[], str[], str.UPPERCASE, str.LOWERCASE");
                        return 0;
                    }
                }
            }
            System.out.println(">>> After checking variables, " + java.util.Arrays.toString(val));

            for (Array arr : TranslateSystem.arrays) {
                for (int i = 0; i < val.length; i++) {
                    if (val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + "[")) {
                        if (!arr.getDataType().equalsIgnoreCase("Number")) {
                            System.out.println(">>> Non-number array exist.");
                            return 0;
                        } else {
                            val[i] = "$";
                            if (arr.getTrueDataType().equalsIgnoreCase("double")) {
                                priority = 2;
                            }
                        }
                    }
                    if (val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".LENGTH")) {
                        val[i] = "$";
                    }
                    if (val[i].equalsIgnoreCase(arr.getName()) || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".[")) {
                        System.out.println(">>> Wrong array use.");
                        return 0;
                    }
                }
            }
            System.out.println(">>> After checking arrays, " + java.util.Arrays.toString(val));

            for (String m : TranslateSystem.maths.keySet()) {
                for (int i = 0; i < val.length; i++) {
                    if (val[i].toUpperCase().startsWith(m)) {
                        val[i] = "$";
                        priority = 2;
                    }
                }
            }
            System.out.println(">>> After checking maths, " + java.util.Arrays.toString(val));

            for (String s : val) {
                System.out.println("s:" + s);
                if (!s.equals("$")) {
                    try {
                        Integer.parseInt(s);
                        System.out.println(">>> A int exist.");
                    } catch (NumberFormatException intException) {
                        try {
                            Double.parseDouble(s);
                            System.out.println(">>> A double exist.");
                            priority = 2;
                        } catch (NumberFormatException doubleException) {
                            // NaN.
                            if (s.equals("(") || s.equals(")")) {
                                //do nothing.
                            } else if (s.startsWith("(") || s.endsWith(")")) {
                                s = s.replace("(", "").replace(")", "");

                                return valueJudge(s, "Number");
                            } else {
                                System.out.println(">>> Non-number exist.");
                                return 0;
                            }
                        }
                    }
                }
            }

        } else if (dataType.equalsIgnoreCase("String")) {
            // if a variable declare is a [String]:
            // for operators, it should contain nothing.
            // for variables, it will contain String variables, String str[i], str.[i..j], str.UPPERCASE, str.LOWERCASE
            // for arrays, it will contain arr[i] with String array.
            // for maths, it should contain nothing.
            // check the remaining, it should only has "xxx" thing.
            for (String o : TranslateSystem.operators.keySet()) {
                if (handledValue.contains(o)) {
                    return 0;
                }
            }

            for (Variable var : TranslateSystem.variables) {
                for (int i = 0; i < val.length; i++) {
                    if (val[i].equalsIgnoreCase(var.getName())) {
                        if (!var.getDataType().equalsIgnoreCase("String")) {
                            System.out.println(">>> Non-string variable exist.");
                            return 0;
                        } else {
                            val[i] = "$";
                        }
                    }
                    if (val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".[") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + "[")
                            || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".UPPERCASE") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".LOWERCASE")) {
                        if (var.getDataType().equalsIgnoreCase("String")) {
                            val[i] = "$";
                        } else {
                            System.out.println(">>> Wrong use str.xxx");
                            return 0;
                        }
                    }
                    if (val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".LENGTH") || val[i].toUpperCase().startsWith(var.getName().toUpperCase() + ".INDEX[")) {
                        System.out.println(">>> Wrong use on str.LENGTH, str.INDEX");
                        return 0;
                    }
                }
            }
            System.out.println(">>> After checking variables, " + java.util.Arrays.toString(val));

            for (Array arr : TranslateSystem.arrays) {
                for (int i = 0; i < val.length; i++) {
                    if (val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + "[")) {
                        if (!arr.getDataType().equalsIgnoreCase("String")) {
                            System.out.println(">>> Non-String array exist.");
                            return 0;
                        } else {
                            val[i] = "$";
                        }
                    }
                    if (val[i].equalsIgnoreCase(arr.getName()) || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".[")
                            || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".LENGTH")) {
                        System.out.println(">>> Wrong array use.");
                        return 0;
                    }
                }
            }
            System.out.println(">>> After checking arrays, " + java.util.Arrays.toString(val));

            for (String m : TranslateSystem.maths.keySet()) {
                for (String s : val) {
                    if (s.toUpperCase().startsWith(m)) {
                        System.out.println(">>> Wrong use Math.");
                        return 0;
                    }
                }
            }

            for (String s : val) {
                if (!s.equals("$")) {
                    if (!(s.startsWith("\"") && s.endsWith("\""))) {
                        return 0;
                    }
                }
            }

        } else if (dataType.equalsIgnoreCase("Boolean")) {
            // if a variable declare is a [Boolean]:
            // for operators, it may contain all operators, jump.
            // for variables, it will contain all variables, jump.
            // for arrays, it will contain all possible array values, but not arr, arr.[i..j].
            // for maths, it may contain all operators, jump.
            // boolean is difficult to check.
            boolean hasJudgement = false;
            if (handledValue.toUpperCase().contains("AND") || handledValue.toUpperCase().contains("OR")
                    || handledValue.toUpperCase().contains("NOT") || handledValue.toUpperCase().contains("IS DIVISIBLE BY")) hasJudgement = true;
            for (String o : TranslateSystem.operators.keySet()) {
                switch (o.toUpperCase()) {
                    case "!=":
                    case "==":
                    case ">":
                    case ">=":
                    case "<":
                    case "<=":
                        if (handledValue.contains(o)) hasJudgement = true;
                        break;
                    default:
                }
            }

            for (String o : TranslateSystem.complexOperators.keySet()) {
                if (handledValue.toUpperCase().contains(o)) hasJudgement = true;
            }

            if (!hasJudgement) {
                System.out.println(">>> No judgment expression.");
                return 0;
            }

            for (Array arr : TranslateSystem.arrays) {
                for (int i = 0; i < val.length; i++) {
                    if (val[i].equalsIgnoreCase(arr.getName()) || val[i].toUpperCase().startsWith(arr.getName().toUpperCase() + ".[")) {
                        System.out.println(">>> Wrong array use.");
                        return 0;
                    }
                }
            }

        } else {
            System.out.println(">>> User declares a wrong data type.");
            return 0;
        }

        return priority;
    }

    // function tool to make string e.g. "i love u" -> "i_love_u".
    public static String stringBuilder(String string) {
    	StringBuilder str = new StringBuilder(string.trim());
        int quoteFlag = 1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\"') {
                quoteFlag = 1 - quoteFlag;
            }
            if (quoteFlag == 0 && c == ' ') {
                str.setCharAt(i, '_');;
            }
        }
        
        return str.toString();
    }

    // function tool to make string e.g. "i_love_u" -> "i love u".
    public static String stringLiberator(String string) {
    	StringBuilder str = new StringBuilder(string.trim());
        int quoteFlag = 1;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\"') {
                quoteFlag = 1 - quoteFlag;
            }
            if (quoteFlag == 0 && c == '_') {
                str.setCharAt(i, ' ');
            }
        }
        return str.toString();
    }

    // function tool to make array e.g. arr[i + 1] -> arr[i_+_1]¡£
    public static String bracketsBuilder(String string) {
    	StringBuilder str = new StringBuilder(string.trim());
        int bracketFlag = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') {
                bracketFlag ++;
            }
            if (c == ']') {
                bracketFlag --;
            }
            if (bracketFlag > 0 && c == ' ') {
                str.setCharAt(i, '_');;
            }
        }

        return str.toString();
    }

    // function tool to make array e.g. arr[i_+_1] -> arr[i + 1].
    public static String bracketsLiberator(String string) {
    	StringBuilder str = new StringBuilder(string.trim());
        int bracketFlag = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '[') {
                bracketFlag ++;
            }
            if (c == ']') {
                bracketFlag --;
            }
            if (bracketFlag > 0 && c == '_') {
                str.setCharAt(i, ' ');;
            }
        }

        return str.toString();
    }

    // function to translate the signature of functions.
    public static String functionSignTranslator(Function f, String translateType) {
        String sign = "";
        String retValue = f.getReturnValue();
        switch (retValue.toUpperCase()) {
            case "NUMBER":
                if (intMode) {
                    retValue = "int";
                } else {
                    retValue = "double";
                }
                break;
            case "STRING":
                if (translateType.equals("Java")) {
                    retValue = "String";
                } else {
                    retValue = "char *";
                }
                break;
            case "BOOLEAN":
                if (translateType.equals("Java")) {
                    retValue = "boolean";
                } else {
                    retValue = "int";
                }
                break;
            case "NUMBER ARRAY":
                if (translateType.equals("Java")) {
                    if (intMode) {
                        retValue = "int[]";
                    } else {
                        retValue = "double[]";
                    }
                } else {
                    if (intMode) {
                        retValue = "int *";
                    } else {
                        retValue = "double *";
                    }
                }
                break;
            case "STRING ARRAY":
                if (translateType.equals("Java")) {
                    retValue = "String[]";
                } else {
                    retValue = "char **";
                }
                break;
            case "BOOLEAN ARRAY":
                if (translateType.equals("Java")) {
                    retValue = "boolean[]";
                } else {
                    retValue = "int *";
                }
                break;
            case "": retValue = "void"; break;
        }

        // private static xxx funcName() { -- Java.
        if (translateType.equals("Java")) {
            sign = "\n    private static " + retValue + " " + f.getName() + "(";
            if (f.hasParameter()) {
                int paramNum = f.getParameters().size();
                int index = 0;
                for (Map.Entry<String,String> p: f.getParameters().entrySet()) {
                    index ++;
                    sign += p.getValue() + " " + p.getKey();
                    String datatype = "";
                    if (p.getValue().endsWith("[]")) {
                        String trueDatatype = p.getValue().replace("[]", "").trim();
                        if (p.getValue().startsWith("double") || p.getValue().startsWith("int")) {
                            datatype = "NUMBER";
                        } else if (p.getValue().startsWith("String")) {
                            datatype = "STRING";
                        } else if (p.getValue().startsWith("boolean")) {
                            datatype = "BOOLEAN";
                        }
                        arrays.add(new Array(p.getKey(), datatype, "1,1", new LinkedHashMap<String, String>(), trueDatatype));
                    } else {
                        if (p.getValue().startsWith("double") || p.getValue().startsWith("int")) {
                            datatype = "NUMBER";
                        } else if (p.getValue().startsWith("String")) {
                            datatype = "STRING";
                        } else if (p.getValue().startsWith("boolean")) {
                            datatype = "BOOLEAN";
                        }
                        Variable v = new Variable(p.getKey(), datatype, "", false);
                        variables.add(new Variable(p.getKey(), datatype, v.getValue(), false, v.getTrueDataType()));
                    }
                    if (index != paramNum) {
                        sign += ", ";
                    } else {
                        sign += ") {";
                    }
                }
            } else {
                sign += ") {";
            }

        } else {
        	// xxx funcName() { -- C.
            sign = "\n" + retValue + " " + f.getName() + "(";
            if (f.hasParameter()) {
                int paramNum = f.getParameters().size();
                int index = 0;
                for (Map.Entry<String,String> p: f.getParameters().entrySet()) {
                    index ++;
                    if (p.getValue().endsWith("[]")) {
                        if (p.getValue().startsWith("String")) {
                            sign += "char ** " + p.getKey();
                        } else if (p.getValue().startsWith("boolean") || p.getValue().startsWith("int")) {
                            sign += "int ** " + p.getKey();
                        } else if (p.getValue().startsWith("double")) {
                            sign += "double ** " + p.getKey();
                        }
                    } else {
                        if (p.getValue().startsWith("String")) {
                            sign += "char * " + p.getKey();
                        } else if (p.getValue().startsWith("boolean")) {
                            sign += "int " + p.getKey();
                        } else {
                            sign += p.getValue() + " " + p.getKey();
                        }
                    }
                    String datatype = "";
                    if (p.getValue().endsWith("[]")) {
                        String trueDatatype = p.getValue().replace("[]", "").trim();
                        if (p.getValue().startsWith("double") || p.getValue().startsWith("int")) {
                            datatype = "NUMBER";
                        } else if (p.getValue().startsWith("String")) {
                            datatype = "STRING";
                        } else if (p.getValue().startsWith("boolean")) {
                            datatype = "BOOLEAN";
                        }
                        arrays.add(new Array(p.getKey(), datatype, "1,1", new LinkedHashMap<String, String>(), trueDatatype));
                    } else {
                        if (p.getValue().startsWith("double") || p.getValue().startsWith("int")) {
                            datatype = "NUMBER";
                        } else if (p.getValue().startsWith("String")) {
                            datatype = "STRING";
                        } else if (p.getValue().startsWith("boolean")) {
                            datatype = "BOOLEAN";
                        }
                        Variable v = new Variable(p.getKey(), datatype, "", false);
                        variables.add(new Variable(p.getKey(), datatype, v.getValue(), false, v.getTrueDataType()));
                    }
                    if (index != paramNum) {
                        sign += ", ";
                    } else {
                        sign += ") {";
                    }
                }
            } else {
                sign += ") {";
            }
        }

        return sign;
    }

    // function to judge whether is a condition expression.
    public static boolean isConditionExpression(String expression) {
    	// if an expression has assignment, it is not a condition expression.
        boolean hasAssign1 = Pattern.matches("[^=]=[^=]", expression);
        boolean hasAssign2 = expression.contains(" = ");
        if (hasAssign1 || hasAssign2) {
            System.out.println(">>> It is an assignment expression.");
            return false;
        }
        return true;
    }

    // function to judge whether the sentence is in a loop.
    public static boolean inLoop() {
        if (repeatCounter + whileCounter + forCounter + wheneverCounter > 0) {
            return true;
        } else {
            return false;
        }
    }

    // function to judge whether the sentence is in a sub-structure.
    public static boolean inSubStructure() {
        if (ifCounter + caseCounter + repeatCounter + whileCounter + forCounter + wheneverCounter > 0) {
            return true;
        } else {
            return false;
        }
    }

}

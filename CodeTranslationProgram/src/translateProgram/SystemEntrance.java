
package translateProgram;

import translateUnits.Function;
import java.util.ArrayList;
import java.util.Date;

public class SystemEntrance {
	// Main class to control program logic.
	// This class only communicate with user interface classes and each system.

	// function to initialize 3 systems when initializing program.
    public static void initialize() {
        TranslateSystem.init(0);
        RecordingSystem.init();
        CodeExecutionSystem.init();
    }

    // function to control the procedures of translate system.
    public static ArrayList<String> startTranslateSystem(String inputCode, ArrayList<Function> inputFunction, String translateType, boolean intMode) {
    	
    	// receive pseudo-code input.
        System.out.println("********** Received Input, analyzing. **********");
        
        System.out.println("[Translate System - Pseudo-code]\n" + inputCode);

        // Initialize translateSystem.
        // initArgs = 0 is the first start.
        // initArgs = 1 is the normal initialize.
        TranslateSystem.init(1);
        
        // Functions given, set intMode status.
        TranslateSystem.functions = inputFunction;
        TranslateSystem.intMode = intMode;

        // Step 1: split pseudo-code input, word by word.
        String splitPseudo = TranslateSystem.wordSplitter(inputCode);
        System.out.println("[Translate System - Split Pseudo-code]\n" + splitPseudo);

        // Step 2: use a stack to check the completeness of END style words.
        boolean validPseudoInStack = TranslateSystem.stackChecker(splitPseudo);
        System.out.println("[Translate System - Stack Check Result]\n" + validPseudoInStack);

        // Step 3: check and translate pseudo-code line by line.
        ArrayList<String> translatedCode = new ArrayList<String>();
        if (validPseudoInStack) {
            translatedCode = TranslateSystem.linesChecker(inputCode, translateType);
            System.out.println("\n[Translate System - Lines Check]   Total Translated Lines Size: " + translatedCode.size());

            String lastLine = "";
            if (translatedCode.size() != 0) {
                lastLine = translatedCode.get(translatedCode.size() - 1);
            } else {
                lastLine = "[ERROR],0,No pseudo-code has been translated with illegal recognized";
                translatedCode.add(lastLine);
            }

            // "[ERROR]" warning with error input.
            if (lastLine.startsWith("[ERROR]")) {
                System.out.println("[Translate System - Lines Check]   Syntax Error in Pseudo-code. Translation Stopped.");

            } else {
                // Translate successful, add headers.
                translatedCode = TranslateSystem.headAdder(translatedCode, translateType);
                
                // Step 4: translate defined functions in pseudo-code (if has).
                if (inputFunction.size() > 0) {
                    translatedCode = functionTranslationByTranslateSystem(inputFunction, translatedCode, translateType);
                    if (translatedCode.get(translatedCode.size() - 1).startsWith("[ERROR|function")) {
                        System.out.println("[Translate System - Function]    Syntax Error in function of Pseudo-code. Translation Stopped.");
                    } else {
                        System.out.println("**********Translation result for " + translateType + ". **********");
                        for (String t: translatedCode) {
                            System.out.println(t);
                        }
                        // comment on sub-array flag.
                        if (TranslateSystem.subArrayFlag) {
                            translatedCode.add("\n// subArrayOf(arr,x,y) in this program means a function:" +
                                               "\n// return the sub array of arr from index = x to y." +
                                               "\n// here omit this function;" +
                                               "\n// if you want to run this code you need implement subArrayOf() by yourself.");
                        }
                        if (translateType.equals("Java")) translatedCode.add("}"); // Class end in Java.
                    }
                } else {
                	// comment on sub-array flag.
                    if (TranslateSystem.subArrayFlag) {
                        translatedCode.add("\n// subArrayOf(arr,x,y) in this program means a function:" +
                                           "\n// return the sub array of arr from index = x to y." +
                                           "\n// here omit this function;" +
                                           "\n// if you want to run this code you need to implement subArrayOf() by yourself.");
                    }
                    if (translateType.equals("Java")) translatedCode.add("}"); // Class end in Java.
                }
            }

        } else {
        	// stack checker failed.
            translatedCode.add(new String("[ERROR],0,Syntax Error on end stack in pseudo-code"));
            System.out.println("\n[Translate System - Stack Check]   Syntax Error in Pseudo-code. Translation Stopped.");
        }

        System.out.println("**********Once translate work finished. **********\n");

        return translatedCode;
    }

    // function to control the translation of function part.
    public static ArrayList<String> functionTranslationByTranslateSystem(ArrayList<Function> functions, ArrayList<String> translatedCode, String translateType) {
        ArrayList<String> tempTranslatedCode = translatedCode;
        
        for (Function f: functions) {
            System.out.println("********** Received Function - " + f.getName() + "(), analyzing. **********");
            System.out.println("[Translate System - Function Pseudo-code]\n" + f.getContents());
            // initArgs = 2 for do not refresh functions data.
            TranslateSystem.init(2);
            
            // Step 1: translate the signature of the function.
            tempTranslatedCode.add(TranslateSystem.functionSignTranslator(f, translateType));
            
            // Step 2: split the function input, word by word.
            String splitPseudo = TranslateSystem.wordSplitter(f.getContents());
            System.out.println("[Translate System - Split Function Pseudo-code]\n" + splitPseudo);

            // Step 3: use a stack to check the completeness of END style words.
            boolean validPseudoInStack = TranslateSystem.stackChecker(splitPseudo);
            System.out.println("[Translate System - Function Stack Check Result]\n" + validPseudoInStack);

            // Step 4: check and translate pseudo-code line by line.
            ArrayList<String> functionTranslatedCode = new ArrayList<String>();
            if (validPseudoInStack) {
                functionTranslatedCode = TranslateSystem.linesChecker(f.getContents(), translateType);
                System.out.println("\n[Translate System - Function Lines Check]   Total Translated Lines Size: " + functionTranslatedCode.size());

                String lastLine = "";
                if (functionTranslatedCode.size() != 0) {
                    lastLine = functionTranslatedCode.get(functionTranslatedCode.size() - 1);
                } else {
                    lastLine = "[ERROR],0,No pseudo-code has been translated with illegal recognized";
                    functionTranslatedCode.add(lastLine);
                }

                // "[ERROR]" warning with error input.
                if (lastLine.startsWith("[ERROR]")) {
                    lastLine = lastLine.replaceFirst("\\[ERROR]", "[ERROR|function|" + f.getName());
                    functionTranslatedCode.remove(functionTranslatedCode.size() - 1);
                    functionTranslatedCode.add(lastLine);
                    System.out.println("[Translate System - Function Lines Check]   Syntax Error in Function " + f.getName() + " Pseudo-code. Translation Stopped.");

                    for (String functionLine: functionTranslatedCode) {
                        tempTranslatedCode.add(functionLine);
                    }
                    return tempTranslatedCode;

                } else {
                    // Translate successful, add this part of code into current result.
                    for (String functionLine: functionTranslatedCode) {
                        tempTranslatedCode.add(functionLine);
                    }
                    
                    // add function end.
                    if (translateType.equals("Java")) {
                        tempTranslatedCode.add("    }");
                    } else {
                        tempTranslatedCode.add("}");
                    }
                }

            } else {
            	// stack checker failed.
                tempTranslatedCode.add(new String("[ERROR|function|" + f.getName() + "],0,Syntax Error on end stack in pseudo-code"));
                System.out.println("\n[Translate System - Function Stack Check]   Syntax Error in Pseudo-code. Translation Stopped.");
                return tempTranslatedCode;
            }
        }

        return tempTranslatedCode;
    }

    // function to call record history function in recording system.
    public static void recordToRecordingSystem(Date date, String pseudoCode, ArrayList<Function> functions) {
        RecordingSystem.recordHistory(date, pseudoCode, functions);
    }

    // function to call get recent 10 records function in recording system.
    public static ArrayList<String> recentRecordByRecordingSystem() {
        return RecordingSystem.getRecentRecords();
    }

    // function to call clear history function in recording system.
    public static void clearHistory() {
        RecordingSystem.clearAllHistory();
    }

    // function to call running code function in code execution system.
    public static boolean runCodesByCodeExecutionSystem(String code) {
        return CodeExecutionSystem.run(code);
    }

}



package translateProgram;

public class CodeExecutionSystem {
	// Static class to construct code execution system. 

	// function to initialize code execution system.
    public static void init() {
        System.out.println(">>> Initialize the code execution system successful... (4/4)");
    }

    // function to run code.
    public static boolean run(String code) {
        boolean run = false;

        IOHandler.initJavaPrograms();
        IOHandler.writeJavaProgram(code);
        // get the .java file.
        System.out.println("[Code Execution System - run code] Start to run Java code...");
        
        try {
        	// run runJava.bat file.
        	/* 
        	 * @echo off
        	 * set CLASSPATH=\%JAVA_HOME%\bin\;./bin;
        	 * cd %~dp0/resources/runs
        	 * javac Main.java
        	 * java Main
        	 * pause
        	 * exit
        	 * 
        	 * */
        	
            Process process = Runtime.getRuntime().exec("cmd /c start runJava.bat");
            process.waitFor();
            run = true;
            System.out.println("********** Running Java with Command Window. **********");
            
        } catch (Exception e) {
        	// bat file has error.
            e.printStackTrace();
            System.out.println(">>> Error when running and error on open cmd window. Please find the developer to get help.");
        }


        return run;
    }
}

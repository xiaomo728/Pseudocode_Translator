
package translators;

import translateUnits.Line;
import programInterfaces.TranslatorInterface;

import java.util.ArrayList;

public class Translator implements TranslatorInterface {

    private int lineNum; // number of line.
    private Line line;
    private ArrayList<String> preLines; // previous lines.

    protected boolean syntaxError = false;
    protected String translateResult;

    public Translator (int lineNum, Line line, ArrayList<String> preLines) {
        this.lineNum = lineNum;
        this.line = line;
        this.preLines = preLines;
    }

    public void translateTo(String language) {
        // Skeleton for sub-class to override.
    }

    protected void translate() {
        System.out.println(">>>Translate Result: " + this.translateResult);
        this.preLines.add(new String(this.line.indentation() + this.translateResult));
    }

    protected void reportError(String exception) {
        System.out.println("||| Syntax Error in Line " + this.lineNum + ". >>> " + exception);
        this.preLines.add(new String("[ERROR]," + this.lineNum + "," + exception));
        syntaxError = true;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public Line getLine() {
        return this.line;
    }

    public ArrayList<String> getPreLines() {
        return this.preLines;
    }

    public boolean hasError() {
        return this.syntaxError;
    }

    public String getTranslateResult() {
        return this.translateResult;
    }

}

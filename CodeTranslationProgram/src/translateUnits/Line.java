
package translateUnits;

public class Line {
	// Object Line.

    private String content;
    private int length;
    private int headSpace;
    private String type;
    private String language;
    public static final String ERROR_MSG = "eErOrMeSsaGeForMayBeERroRiNpUT$VEry@mazing" + Math.random() * Short.MAX_VALUE;

    // constructors.
    public Line (String content) {
        this.content = content;
        this.length = content.length();
        this.headSpace = this.numOfHeadSpace();
        this.type = this.checkType();
        this.language = "";
    }

    public Line (String content, String language) {
        this.content = content;
        this.length = content.length();
        this.headSpace = this.numOfHeadSpace();
        this.type = this.checkType();
        this.language = language;
    }

    public int numOfHeadSpace() {
    	// number of indentation.
        int numOfHeadSpace = 0;
        for (int i = 0; i < this.toString().length(); i++) {
            if (this.toString().charAt(i) == ' ') {
                numOfHeadSpace++;
            } else if (this.toString().charAt(i) == '\t') {
                numOfHeadSpace += 4;
            } else {
                break;
            }
        }
        return numOfHeadSpace;
    }

    public String checkType() {
    	// check line type by first word of line.
        int index = 0;
        for (int i = 0; i < this.getPureContent().length(); i++) {
            char c = this.getPureContent().charAt(i);
            //System.out.println("Char at " + i + " is " + c);
            if (c == ' ') {
                index = i;
                break;
            }
        }
        if (index == 0) {
            index = this.getPureContent().length();
        }
        String firstWord = this.getPureContent().substring(0, index).toUpperCase();
        return firstWord;
    }

    public String component(int index) {
    	// return the word that at the index of the line.
        System.out.println("Index = " + index);
        if (index < -1) {
            return ERROR_MSG;
        }
        String[] splitLine = this.getPureContent().split(" ");
        try {
            if (index == -1) {
                return splitLine[splitLine.length - 1];
            }
            System.out.println("Component catch at Index " + index + " = " + splitLine[index - 1]);
            return splitLine[index - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Component NOT catch.");
            return ERROR_MSG;
        }
    }

    public String component(int begin, int end) {
    	// return the word that from index = begin to index = end of the line.
        if (begin < -1 || end < -1) {
            return ERROR_MSG;
        }

        if (end == -1) {
            // do nothing.
        } else if (begin > end) {
            return ERROR_MSG;
        } else if (begin == end) {
            String cont = component(begin);
            return cont;
        }
        String[] splitLine = this.getPureContent().split(" ");
        System.out.println(splitLine[splitLine.length - 1]);
        if (end == -1) {
            end = splitLine.length;
        }
        try {
            String components = "";
            for (int i = begin; i <= end; i++) {
                components += splitLine[i - 1] + " ";
            }
            System.out.println("Component catch at Index from "+ begin + " to " + end + " = " + components);
            return components;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Component NOT catch.");
            return ERROR_MSG;
        }
    }

    public int componentOf(String keywords) {
    	// return the index of the word in the line.
        // System.out.println("Keyword: " + keywords);
        String[] splitLine = this.getPureContent().split(" ");
        boolean isSearched = false;
        int index = 0;
        for (String s: splitLine) {
            index ++;
            // System.out.println("Search: " + s);
            if (keywords.equalsIgnoreCase(s)) {
                // System.out.println("Searched '" + "' at Index = " + index);
                isSearched = true;
                break;
            }
        }
        if (!isSearched) {
            return -100;
        }
        return index;
    }

    public boolean startsWith(String words) {
    	// first word of the line.
        return this.getPureContent().toUpperCase().startsWith(words);
    }

    public boolean contain(String words) {
    	// whether the line contain a word.
        String split[] = this.getPureContent().toUpperCase().split(" ");
        for (String s: split) {
            if (s.equalsIgnoreCase(words)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return this.content;
    }

    public String getContent() {
        return this.content;
    }

    public String getPureContent() {
        return this.content.trim().replaceAll(" +"," ");
    }

    public int getLength() {
        return this.length;
    }

    public int getHeadSpace() {
        return this.headSpace;
    }

    public String indentation() {
    	// indentation of the line.
        String indentation = "";
        int inden = 0;
        if (this.language.equals("Java")) {
            inden = 8;
        } else {
            inden = 4;
        }

        for (int i = 0; i < this.getHeadSpace() + inden; i++) {
            indentation += " ";
        }
        return indentation;
    }

    public String getType() {
        return this.type;
    }

    public String getLanguage() {
        return this.language;
    }
}

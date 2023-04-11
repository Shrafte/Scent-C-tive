import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    static int LONGPARAMTHRESHOLD = 6;

    static int LONGFUNCTIONTHRESHOLD = 20;
    static boolean[] settings = new boolean[20];
    static ArrayList<Smell> SMELLS = new ArrayList<>();
    static SourceBSTree tree;
    public static void main(String[] args) throws IOException {
        // error handling
        if(args.length < 1) {
            System.out.println("use 'Java Main <single-source-code-file>'");
            System.exit(-1);
        }

        String fullStr = "srcml " + args[0] + " -o " + args[0] + ".xml";
        settingsHandler(args);
        String[] strArray;
        strArray = fullStr.split("\\s+");
        File fileToDelete = new File (args[0] + ".xml");
        Process process;
        process = Runtime.getRuntime().exec(strArray);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        tree = new SourceBSTree(args[0]);

        smellHandler(args);
        printSmells();
        fileToDelete.delete();
    }
    public static void settingsHandler(String[] args){
        Arrays.fill(settings, true);
        for(int i = 0;i<args.length-1;i++){
            switch(args[i]){
                case "-g":              //Goto statements
                    settings[2] = false;
                    break;
                case "-e":              //Empty statements
                    settings[3] = false;
                    break;
                case "-m":              //Magic numbers
                    settings[4] = false;
                    break;
                case "-i":              //Block-less if statements
                    settings[5] = false;
                    break;
                case "-l":              //Block-less loops
                    settings[6] = false;
                    break;
                case "-p":              //Long parameter lists          CURRENT BUG: doesn't disable if last argument
                    if(args[i + 1].charAt(0) != '-'){
                        try{
                            LONGPARAMTHRESHOLD = Integer.parseInt(args[i+1]);
                        } catch(Exception e){
                            System.out.println("Couldn't parse long parameter list threshold");
                            break;
                        }
                        break;
                    } else {
                        settings[7] = false;
                    } break;
                case "-f":              //Long functions                CURRENT BUG: doesn't disable if last argument
                    if(args[i + 1].charAt(0) != '-'){
                        try{
                            LONGFUNCTIONTHRESHOLD = Integer.parseInt(args[i+1]);
                        } catch(Exception e){
                            System.out.println("Couldn't parse long function threshold");
                            break;
                        }
                        break;
                    } else {
                        settings[8] = false;
                    }
                    break;
                case "-d":              //Dead functions
                    settings[9] = false;
                    break;
                case "-x":              //Conditional complexity
                    settings[11] = false;
                    break;
                case "-s":              //Security issues
                    settings[12] = false;
                    break;
                case "-c":              //Continue statements
                    settings[14] = false;
                    break;
                case "-b":              //Break statements
                    settings[15] = false;
                    break;
                case "-v":              //Bad variable names
                    settings[16] = false;
                    break;
                case "-n":              //Bad function names
                    settings[17] = false;
                    break;
                default:
                    break;
            }
        }
    }
    public static void smellHandler(String[] args) throws IOException {
        if(settings[0]){                //Dictionary of Symbols
        }
        if(!settings[1]){                //smell report
            return;
        }
        if(settings[2]){                //Goto statements
            gotoHandler(args[0]);
        }
        if(settings[3]){                //Empty Statements
            //emptyStmtHandler(args[0]);
        }
        if(settings[4]){                //Magic numbers
            magicNumHandler(args[0]);
        }
        if(settings[5]){                //Block-less if statements
            noBlockIfHandler(args[0]);
        }
        if(settings[6]){                //Block-less loops
            noBlockLoopHandler(args[0]);
        }
        if(settings[7]){                //Long parameter list
            longParamHandler(args[0]);
        }
        if(settings[8]){                //Long functions
            longFuncHandler(args[0]);
        }
        if(settings[9]){                //Dead functions
            deadCodeHandler(args[0]);
        }
        if(settings[10]){                //Embedded increment/decrement
            embeddedIncrementHandler(args[0]);
        }
        if(settings[11]){                //Conditional complexity
        }
        if(settings[12]){               //Security issues
            securityRisks(args[0]);
        }
        if(settings[13]){               //Deep block nesting
        }
        if(settings[14]){               //Continue statements
            continueHandler(args[0]);
        }
        if(settings[15]){               //Break statements
            breakHandler(args[0]);
        }
        if(settings[16]){               //Bad variable names
            variableNamingHandler(args[0]);
        }
        if(settings[17]){               //Bad function names
            functionNamingHandler(args[0]);
        }
        if(settings[18]){               //Multiple variable declarations on one line
            multiVarDec(args[0]);
        }
        if(settings[19]){               //Global Variables
            globalVariableHandler(args[0]);
        }
    }

    /**
     * Creates a new smell object and adds it to the SMELLS array in a way that preserves
     * natural ordering based off of line numbers
     * @param smellType The type of smell. Use SmellEnum for consistency
     * @param code The offending lines of code concat into one string
     * @param line The line number of the first line of the offending code
     */
    private static void addSmell(String smellType, String code, int line) {
        Smell smell = new Smell(line, smellType, code);
        boolean isPlaced = false;
        if(SMELLS.size() == 0) {
            SMELLS.add(smell);
            return;
        }
        for(int i = 0; i < SMELLS.size() && !isPlaced; i++) {
            if(SMELLS.get(i).getLineNum() >= line) {
                SMELLS.add(i, smell);
                isPlaced = true;
            }
        }
        if(!isPlaced) {SMELLS.add(smell);}
    }
    private static void printSmells() {
        for(int i = 0; i < SMELLS.size(); i++) {
            System.out.println(SMELLS.get(i).getLineNum() + " | " + SMELLS.get(i).getSmellType() + ": " + SMELLS.get(i).getCode());
        }
    }
    private static void functionNamingHandler(String fileName) {
        int i = 0;
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> functionList = new ArrayList<String>();
        int lineNum;

        File bufferFile = new File(bufferFileName);
        File functionNamingFile = new File("functionNaming.txt");

        // creating file with all function names
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:function/src:name\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit/src:name)\"", bufferFileName);
        builder2.redirectOutput(functionNamingFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        // putting all function names into arraylist
        try {
            Scanner scan = new Scanner(functionNamingFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                functionList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // checking function name for camel case
        String camelCase = "([a-z]+[A-Z]+\\w+)+";
        for (i = 0; i < functionList.size(); i++) {
            if (!functionList.get(i).matches(camelCase) && !functionList.get(i).equals("main")) {
                lineNum = tree.findSingle(functionList.get(i) + "(", SmellEnum.funcName, SMELLS);
                addSmell(SmellEnum.funcName, functionList.get(i), lineNum);
            }
        }

        bufferFile.delete();
        functionNamingFile.delete();
    }
    private static void variableNamingHandler(String fileName) {
        int i = 0;
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> varList = new ArrayList<String>();
        ArrayList<String> fullLines = new ArrayList<>();
        int lineNum;

        File bufferFile = new File(bufferFileName);
        File varFile = new File("variables.txt");

        // creating file with all variable names
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:decl/src:name\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit/src:name)\"", bufferFileName);
        builder2.redirectOutput(varFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        // putting all variable names into arraylist
        try {
            Scanner scan = new Scanner(varFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                varList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fullLines = findVarLine(fileName);
        ArrayList<Integer> pastLineNumbers = new ArrayList<>();

        // checking variable name for camel case
        String camelCase = "([a-z]+[A-Z]+\\w+)+";
        for (i = 0; i < varList.size(); i++) {
            if (varList.get(i).contains("[")) {
                varList.set(i, varList.get(i).substring(0, varList.get(i).indexOf("[")));
            }
            if (varList.get(i).contains("<")) {
                varList.set(i, varList.get(i).substring(0, varList.get(i).indexOf("<")));
            }
            if (!varList.get(i).matches(camelCase) && !varList.get(i).equals("argc") && !varList.get(i).equals("argv") && !varList.get(i).equals("i")) {
                lineNum = tree.findSingle(fullLines.get(i), SmellEnum.varName, SMELLS);
                if (!pastLineNumbers.contains(lineNum) && lineNum != -1) {
                    addSmell(SmellEnum.varName, fullLines.get(i), lineNum);
                }
                pastLineNumbers.add(lineNum);
            }
        }

        //bufferFile.delete();
        //varFile.delete();
    }

    private static ArrayList<String> findVarLine(String fileName) {
        ArrayList<String> fullDecLine = new ArrayList<>();
        String buffer = "";
        int i = 0;

        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";

        File bufferFile = new File(bufferFileName);
        File varLinesFile = new File("varLines.txt");

        // creating file with all variable declaration lines
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:decl_stmt | //src:parameter_list\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:decl_stmt | //src:parameter_list)\"", bufferFileName);
        builder2.redirectOutput(varLinesFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        int numCommas = 0;

        // adding the variable declaration lines to an arraylist
        try {
            Scanner scan = new Scanner(varLinesFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                if (buffer.equals("()")) {
                    continue;
                }
                if (!buffer.equals("void")) {
                    if (buffer.contains(",")) { // for multiple var declarations
                        for (i = 0; i < buffer.length(); i++) { // count commas in line
                            if (buffer.charAt(i) == ',') {
                                numCommas++;
                            }
                        }
                        for (i = 0; i <= numCommas; i++) {  // add numCommas + 1 amount of buffer
                            fullDecLine.add(buffer);
                        }
                    } else {
                        fullDecLine.add(buffer);
                    }
                }
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // varLinesFile.delete();

        return fullDecLine;
    }

    private static void continueHandler(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> outputList = new ArrayList<String>();
        int i, lineNum;

        File bufferFile = new File(bufferFileName);
        File outputFile = new File("output.txt");

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:continue\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(outputFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        try {
            Scanner scan = new Scanner(outputFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                outputList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (outputList.size() > 0) {
            if (outputList.get(0) == "") {
                outputList.clear();
            }
        }

        for (i = 0; i < outputList.size(); i++) {
            lineNum = tree.findSingle(outputList.get(i), SmellEnum.contStmt, SMELLS);
            addSmell(SmellEnum.contStmt, outputList.get(i), lineNum);
        }

        bufferFile.delete();
        outputFile.delete();
    }

    private static void breakHandler(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> outputList = new ArrayList<String>();
        int i, lineNum;

        File bufferFile = new File(bufferFileName);
        File outputFile = new File("output.txt");

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:break\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(outputFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        try {
            Scanner scan = new Scanner(outputFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                outputList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (outputList.size() > 0) {
            if (outputList.get(0) == "") {
                outputList.clear();
            }
        }

        for (i = 0; i < outputList.size(); i++) {
            lineNum = tree.findSingle(outputList.get(i), SmellEnum.breakStmt, SMELLS);
            addSmell(SmellEnum.breakStmt, outputList.get(i), lineNum);
        }

        bufferFile.delete();
        outputFile.delete();
    }

    private static void longFuncHandler(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        int i = 0;

        File bufferFile = new File(bufferFileName);
        File functionFile = new File("functions.txt");

        // creating file with all function names
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:function\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:function)\"", bufferFileName);
        builder2.redirectOutput(functionFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        int amountLines, inFunction, bracketCounter, numBracketsOnLine, stopCounting, lineNum;
        amountLines = inFunction = bracketCounter = numBracketsOnLine = stopCounting = lineNum = 0;
        String lineToSend, prevLine;
        lineToSend = prevLine = "";

        try {
            Scanner scan = new Scanner(functionFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                if (buffer.contains("{") && inFunction == 0) {
                    inFunction = 1;
                    if (buffer.length() > 1) {
                        lineToSend = buffer;
                    } else {
                        lineToSend = prevLine;
                    }
                }
                if (inFunction == 1) {
                    amountLines++;
                    if (buffer.contains("{")) {
                        for (i = 0; i < buffer.length(); i++) {
                            if (buffer.charAt(i) == '{') {
                                numBracketsOnLine++;
                            }
                        }
                        bracketCounter += numBracketsOnLine;
                        numBracketsOnLine = 0;
                    }
                    if (buffer.contains("}")) {
                        for (i = 0; i < buffer.length(); i++) {
                            if (buffer.charAt(i) == '}') {
                                numBracketsOnLine++;
                            }
                        }
                        bracketCounter -= numBracketsOnLine;
                        numBracketsOnLine = 0;
                    }
                }
                if (amountLines >= LONGFUNCTIONTHRESHOLD && stopCounting == 0) {
                    lineNum = tree.findSingle(lineToSend, SmellEnum.longFunc, SMELLS);
                    addSmell(SmellEnum.longFunc, lineToSend, lineNum);
                    stopCounting = 1;
                    amountLines = 0;
                }
                if (bracketCounter == 0) {
                    inFunction = 0;
                    amountLines = 0;
                    stopCounting = 0;
                }
                prevLine = buffer;
            }
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find function file.");
            e.printStackTrace();
        }


        functionFile.delete();
        bufferFile.delete();
    }
    private static void noBlockLoopHandler(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> outputList = new ArrayList<String>();
        int i, lineNum;

        File bufferFile = new File(bufferFileName);
        File outputFile = new File("output.txt");

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:block[@type='pseudo']/parent::node()\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(outputFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        try {
            Scanner scan = new Scanner(outputFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                outputList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (i = 0; i < outputList.size(); i++) {
            if ((outputList.get(i).contains("while") || outputList.get(i).contains("for") || outputList.get(i).contains("do")) && !outputList.get(i).contains("//")
                    && ((outputList.get(i).charAt(0) == 'w') || outputList.get(i).charAt(0) == 'f' || outputList.get(i).charAt(0) == 'd')) {
                lineNum = tree.findSingle(outputList.get(i), SmellEnum.blocklessLoop, SMELLS);
                addSmell(SmellEnum.blocklessLoop, outputList.get(i), lineNum);
            }
        }

        bufferFile.delete();
        outputFile.delete();
    }

    private static void noBlockIfHandler(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> outputList = new ArrayList<String>();
        int i, lineNum;

        File bufferFile = new File(bufferFileName);
        File outputFile = new File("output.txt");

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:block[@type='pseudo']/parent::node()\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(outputFile);
        builder2.redirectError(new File("out.txt"));

        try {
            Process p = builder.start();
            p.waitFor();
            Process p2 = builder2.start();
            p2.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }

        try {
            Scanner scan = new Scanner(outputFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                outputList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (i = 0; i < outputList.size(); i++) {
            if ((outputList.get(i).contains("if") || outputList.get(i).contains("else")) && !outputList.get(i).contains("//") &&
                    (outputList.get(i).charAt(0) == 'i' || outputList.get(i).charAt(0) == 'e')) {
                lineNum = tree.findSingle(outputList.get(i), SmellEnum.blocklessLoop, SMELLS);
                addSmell(SmellEnum.blocklessIf, outputList.get(i), lineNum);
            }
        }

        bufferFile.delete();
        outputFile.delete();
    }
    private static Smell emptyStmtHandler(String filename) {
        String xpathName = filename + ".xml";
        String argument;
        String output = "";
        String before = "";
        int iterator = 1;

        do {
            // gets each empty expression
            argument = "string(//src:empty_stmt[" + iterator + "])";
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", argument, xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.print("IOExcpetion detected");
            } catch (InterruptedException e) {
                System.out.print("InterruptedException detected");
            }

            // gets the node behind the empty expression
            argument = "string(//src:empty_stmt[" + iterator + "]/preceding-sibling::*[position() < 2])";
            ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", argument, xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder2.start();
                p.waitFor();
                before = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.print("IOExcpetion detected");
            } catch (InterruptedException e) {
                System.out.print("InterruptedException detected");
            }

            //cleans the string of newline characters and splits it by space
            output = output.replace("\n", "").replace("\r", "");
            before = before.replace("\n", "").replace("\r", "");
            if(output.length() > 0) {System.out.println(before + output);}
            iterator++;
        } while(output.length() > 0); // checks to see if a line was retrieved from the code. If not, end loop


        return new Smell();
    }
    private static void magicNumHandler(String filename) {
        String xpathName = filename + ".xml";
        String argument;
        String output = "";
        int iterator = 1;
        int lineNum = -1;
        String[] arguments = {  "(//src:expr_stmt[src:expr/src:literal[@type='number']])",
                "(//src:condition[src:expr/src:literal[@type='number']])"};

        for (String s : arguments) {
            iterator = 1;
            do {
                argument = "string(" + s + "[" + iterator + "])";
                ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", argument, xpathName);
                builder.redirectError(new File("out.txt"));
                try {
                    Process p = builder.start();
                    p.waitFor();
                    output = new String(p.getInputStream().readAllBytes());
                } catch (IOException e) {
                    System.out.print("IOExcpetion detected");
                } catch (InterruptedException e) {
                    System.out.print("InterruptedException detected");
                }

                output = output.trim();
                if (output.length() > 2) {;
                    if(output.contains("\n")) {lineNum = tree.findMulti(output, SmellEnum.magicNum, SMELLS);}
                    else {lineNum = tree.findSingle(output, SmellEnum.magicNum, SMELLS);}
                    if (lineNum > -1) {
                        output = output.trim();
                        addSmell(SmellEnum.magicNum, output, lineNum);
                    }
                }
                iterator++;
            } while (output.length() > 2); // checks to see if a line was retrieved from the code. If not, end loop
        }
    }

    private static void securityRisks(String filename) {
        String[] riskyMethods = {"gets", "strcpy", "strcat", "strcmp", "sprintf", "vsprintf", "atoi",
                "atof", "atol", "atoll", "scanf", "sscanf"};
        String xpathName = filename + ".xml";
        String argument;
        String output = "";
        int iterator;
        int index;

        for (String riskyMethod : riskyMethods) {
            iterator = 1;
            do {
                argument = "string((//src:expr_stmt[src:expr/src:call/src:name[text()='" + riskyMethod + "']])[" + iterator + "])";
                ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", argument, xpathName);
                builder.redirectError(new File("out.txt"));
                try {
                    Process p = builder.start();
                    p.waitFor();
                    output = new String(p.getInputStream().readAllBytes());
                } catch (IOException e) {
                    System.out.print("IOExcpetion detected");
                } catch (InterruptedException e) {
                    System.out.print("InterruptedException detected");
                }

                //cleans the string of newline characters and splits it by space
                output = output.trim();
                if (output.length() > 2) {
                    if(output.contains("\n")) {index = tree.findMulti(output, SmellEnum.secur, SMELLS);}
                    else {index = tree.findSingle(output, SmellEnum.secur, SMELLS);}
                    if(index > -1) {
                        addSmell(SmellEnum.secur, output, index);
                    }
                }
                iterator++;
            } while (output.length() > 2); // checks to see if a line was retrieved from the code. If not, end loop

        }
    }

    private static void longParamHandler(String filename) {
        String xpathName = filename + ".xml";
        String output = "";
        String function = "";
        String temp = "";
        String[] outputParse;
        int i = 1;
        do{
            int paramNum = 0;
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:function[" + i + "]/src:parameter_list)\"", xpathName);
            ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:function[" + i + "])\"", xpathName);
            ProcessBuilder builder3 = new ProcessBuilder("srcml", "--xpath", "\"count(//src:function[" + i + "]/src:parameter_list/src:parameter)\"", xpathName);

            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
                p = builder2.start();
                p.waitFor();
                function = new String(p.getInputStream().readAllBytes());
                p = builder3.start();
                p.waitFor();
                temp = new String(p.getInputStream().readAllBytes()).trim();
                paramNum = Integer.parseInt(temp);
            } catch (IOException e) {
                System.out.println("IOException");
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
            output = output.replace("\n", "").replace("\r", "");
            outputParse = output.split("\\s+");
            i++;
            if(outputParse.length > 2){
                char[] outChars = output.toCharArray();
                int lineNum = tree.findSingle(function.substring(0,function.indexOf("{")).trim(), SmellEnum.longParam, SMELLS);
                if(paramNum >= LONGPARAMTHRESHOLD){
                    addSmell(SmellEnum.longParam, function.substring(0,function.indexOf("{")).trim(),lineNum);
                }
            }
        } while(outputParse.length > 2);
    }


    public static void embeddedIncrementHandler(String filename){
        String xpathName = filename + ".xml";
        String output = "";
        String function = "";
        String[] outputParse;
        int i = 1;
        do{
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:expr_stmt[" + i + "]/src:expr/src:call/src:argument_list/src:argument/src:expr/src:operator)\"", xpathName);
            ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:expr_stmt[" + i + "]/src:expr/src:call)\"", xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
                p = builder2.start();
                p.waitFor();
                function = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.println("IOException");
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
            output = output.replace("\n", "").replace("\r", "");
            outputParse = function.split("\\s+");
            i++;
            int lineNum = tree.findSingle(function.substring(0,function.length()-1), SmellEnum.embed, SMELLS);
            if(output.length() == 2 && (output.equals("++") || output.equals("--"))) {
                //SMELLS.add(new Smell("Embedded Increment/Decrement", function.substring(0,function.length()-1)));
                addSmell("Embedded Increment/Decrement",function.substring(0,function.length()-1),lineNum);
            }
        } while(outputParse.length > 0);
    }

    public static void gotoHandler(String filename) throws IOException {
        String xpathName = filename + ".xml";
        String output = "";
        int index;
        int iterator = 1;
        do {
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:goto[" + iterator + "])\"", xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.print("IOExcpetion detected");
            } catch (InterruptedException e) {
                System.out.print("InterruptedException detected");
            }
            output = output.strip();
            if(output != "") {
                index = tree.findSingle(output, SmellEnum.gotoStmt, SMELLS);
                addSmell(SmellEnum.gotoStmt, output, index);
            }
            iterator++;
        }while (output.length() > 0);
    }

    private static ArrayList<String> dcHelper(String filename) throws IOException {
        String xpathName = filename + ".xml";
        String ind = "";
        ArrayList<String> output = new ArrayList<String>();
        int index;
        int iterator = 1;

        do {
            ProcessBuilder helper = new ProcessBuilder("srcml", "--xpath", "string((//src:expr_stmt[src:expr/src:call/src:name])["+iterator+"])", xpathName);
            helper.redirectError(new File("out.txt"));
            try {
                Process p = helper.start();
                p.waitFor();
                ind = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.print("IOExcepetion detected");
            } catch (InterruptedException e) {
                System.out.print("InterruptedException detected");
            }
            ind = ind.strip();

            if(ind != "") {
                ind = ind.substring(0, ind.indexOf("("));
                System.out.println("adding " + ind + " to func list");
                output.add(ind);
            }
            iterator++;
        }while(ind.length() > 2);
        return output;
    }
    public static void deadCodeHandler(String filename) throws IOException {
        String xpathName = filename + ".xml";
        String output = "";
        ArrayList<String> funcCalls = dcHelper(filename);
        boolean isDead = true;
        int index;
        int iterator = 1;
        do {
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "string((//src:function/src:name)["+iterator+"])", xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.print("IOExcepetion detected");
            } catch (InterruptedException e) {
                System.out.print("InterruptedException detected");
            }
            output = output.strip();
            isDead = true;
            //System.out.println("test" + output);
            for(String call : funcCalls){
                if(call.compareTo(output) == 0) {
                    isDead = false;
                }
            }
            if(isDead && output != "" && output.compareTo("main") != 0) {
                index = tree.findSingle(output, SmellEnum.deadFunc, SMELLS);
                addSmell(SmellEnum.deadFunc, output, index);
            }
            iterator++;
        }while (output.length() > 0);
    }

    public static void multiVarDec(String filename) {
        String xpathName = filename + ".xml";
        String output = "";
        boolean isDead = true;
        int index;
        int iterator = 1;
        do {
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "string((//src:decl_stmt[src:decl/src:type[@ref='prev']])["+iterator+"])", xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.print("IOExcepetion detected");
            } catch (InterruptedException e) {
                System.out.print("InterruptedException detected");
            }
            output = output.strip();

            if(output != "") {
                index = tree.findSingle(output, SmellEnum.multVar, SMELLS);
                addSmell(SmellEnum.multVar, output, index);
            }
            iterator++;
        }while (output.length() > 0);
    }
    public static void globalVariableHandler(String filename){
        String xpathName = filename + ".xml";
        String output = "";
        String function = "";
        String[] outputParse;
        String[] outputParse2;
        int i = 1;
        int j = 1;
        ArrayList<String> nonGlobalVariables = new ArrayList<>();
        do{
            ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:block_content/src:decl_stmt[" + j + "]/src:decl)\"", xpathName);
            try {
                Process p = builder2.start();
                p.waitFor();
                function = new String(p.getInputStream().readAllBytes());
            }  catch (IOException e) {
                System.out.println("IOException");
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
            outputParse = function.split("\\s+");
            if(function.length() > 0) {
                nonGlobalVariables.add(function);
            }
            j++;
        } while(outputParse.length > 0);

        do{
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:decl_stmt[" + i + "]/src:decl)\"", xpathName);
            builder.redirectError(new File("out.txt"));
            try {
                Process p = builder.start();
                p.waitFor();
                output = new String(p.getInputStream().readAllBytes());
            } catch (IOException e) {
                System.out.println("IOException");
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
            outputParse2 = output.split("\\s+");
            i++;
            boolean valid = true;
            if(output.length() > 0) {
                for(int x = 0;x < nonGlobalVariables.size();x++){
                    //exclude constants
                    if(output.length()>5) {
                        if (output.substring(0, 5).equals("const")) {
                            valid = false;
                            break;
                        }
                    }
                    if(output.equals(nonGlobalVariables.get(x))) {
                        valid = false;
                        break;
                    }
                }
                int lineNum = tree.findSingle(output.substring(0,output.length()-1), SmellEnum.globalVar, SMELLS);
                if(valid) {
                    addSmell(SmellEnum.globalVar,output.substring(0,output.length()-1),lineNum);
                }
            }
        } while(outputParse2.length > 0);
    }

    static class Smell {
        int lineNum;
        String smellType;
        String code;
        // default constructor just in case
        public Smell() {
            lineNum = -1;
            smellType = "";
            code = "";
        }
        // intended constructor once line numbers are figured out
        public Smell(int lineNum, String smell, String code) {
            this.lineNum = lineNum;
            this.smellType = smell;
            this.code = code;
        }
        // this constructor is used while we don't have line numbers figured out
        public Smell(String smell, String code) {
            this.lineNum = -1;
            this.smellType = smell;
            this.code = code;
        }
        // outputs smell in error format
        public String getSmellType() {
            return smellType;
        }
        public int getLineNum() {
            return lineNum;
        }
        public String getCode() {
            return code;
        }
    }

    static class Node {
        private final String line;
        private final int lineNum;
        private Node left;
        private Node right;
        private Node parent;
        boolean color; // true = red, false = black
        public Node(String line, int lineNum) {
            this.line = line;
            this.lineNum = lineNum;
            left = right = parent = null;
            color = false;
        }
        public void setLeft(Node node) {
            left = node;
        }
        public void setRight(Node node) {
            right = node;
        }
        public void setParent(Node node) {
            parent = node;
        }
        public void setColor(boolean bool) {
            color = bool;
        }
        public Node getLeft() {
            return left;
        }
        public Node getRight() {
            return right;
        }
        public Node getParent() {
            return parent;
        }
        public String getLine() {
            return line;
        }
        public int getLineNum() {
            return lineNum;
        }
        public boolean getColor() {
            return color;
        }
    }

    static class SourceBSTree {
        private Node root;
        private Node NIL;
        private int size;
        public SourceBSTree(String filename) {
            size = 0;
            NIL = new Node(null, -1);
            NIL.setParent(NIL);
            NIL.setLeft(NIL);
            NIL.setRight(NIL);
            NIL.setColor(false);
            root = NIL;
            readFile(filename);
        }
        private void insert(Node node) {
            boolean isPlaced = false;
            Node tempNode = root;
            if(root == NIL) {
                root = node;
                size++;
                return;
            }
            while(!isPlaced) {
                // thisNode < tempNode
                if(tempNode.getLine().compareTo(node.getLine()) > 0 ||
                        (tempNode.getLine().compareTo(node.getLine()) == 0 && tempNode.getLineNum() > node.getLineNum())) {
                    if(tempNode.getLeft() == NIL) {
                        tempNode.setLeft(node);
                        node.setParent(tempNode);
                        isPlaced = true;
                    }
                    else {tempNode = tempNode.getLeft();}
                }

                // thisNode > tempNode
                else if(tempNode.getLine().compareTo(node.getLine()) < 0 ||
                        (tempNode.getLine().compareTo(node.getLine()) == 0 && tempNode.getLineNum() < node.getLineNum())) {
                    if (tempNode.getRight() == NIL) {
                        tempNode.setRight(node);
                        node.setParent(tempNode);
                        isPlaced = true;
                    } else {
                        tempNode = tempNode.getRight();
                    }
                }
            }
            size++;
        }
        private void rightRotate(Node node) {
            Node leftChild = node.getLeft();
            Node leftGrandChild = leftChild.getRight();
            Node parent = node.getParent();

            if(parent == NIL) {root = leftChild;}
            else if(node == parent.getLeft()) {parent.setLeft(leftChild);}
            else {parent.setRight(leftChild);}

            node.setParent(leftChild);
            node.setLeft(leftGrandChild);
            if(leftChild != NIL) {
                leftChild.setRight(node);
                leftChild.setParent(parent);
            }
            if(leftGrandChild != NIL) {leftGrandChild.setParent(node);}
        }
        private void leftRotate(Node node) {
            Node rightChild = node.getRight();
            Node rightGrandChild = rightChild.getLeft();
            Node parent = node.getParent();

            if(parent == NIL) {root = rightChild;}
            else if(node == parent.getLeft()) {parent.setLeft(rightChild);}
            else {parent.setRight(rightChild);}

            node.setParent(rightChild);
            node.setRight(rightGrandChild);
            if(rightChild != NIL) {
                rightChild.setLeft(node);
                rightChild.setParent(parent);
            }
            if(rightGrandChild != NIL) {rightGrandChild.setParent(node);}
        }
        private void addLine(String str, int lineNum) {
            Node node = new Node(str, lineNum);
            node.setLeft(NIL);
            node.setRight(NIL);
            node.setParent(NIL);
            node.setColor(true); // true == red, false == black
            Node uncle;
            insert(node);
            if(size > 1) {
                while(node.getParent().getColor()) {

                    // uncle is grandparent's right child
                    if(node.getParent() == node.getParent().getParent().getLeft()) {
                        uncle = node.getParent().getParent().getRight();

                        // case 1: uncle is red
                        if(uncle.getColor()) {
                            node.getParent().setColor(false);
                            uncle.setColor(false);
                            node.getParent().getParent().setColor(true);
                            node = node.getParent().getParent();
                        }
                        else {

                            // case 2: current node is the right child of the parent
                            if(node == node.getParent().getRight()) {
                                node = node.getParent();
                                leftRotate(node);
                            }

                            // case 3: current node is the left child of the parent (happens both cases)
                            node.getParent().setColor(false);
                            node.getParent().getParent().setColor(true);
                            rightRotate(node.getParent().getParent());

                        }
                    }

                    // uncle is grandparent's left child
                    else {
                        uncle = node.getParent().getParent().getLeft();

                        // case 1: uncle is red
                        if(uncle.getColor()) {
                            node.getParent().setColor(false);
                            uncle.setColor(false);
                            node.getParent().getParent().setColor(true);
                            node = node.getParent().getParent();
                        }
                        else {

                            // case 2: current node is the left child of the parent
                            if(node == node.getParent().getLeft()) {
                                node = node.getParent();
                                rightRotate(node);
                            }

                            // case 3: current node is leftChild of parent (happens in both cases)
                            node.getParent().setColor(false);
                            node.getParent().getParent().setColor(true);
                            leftRotate(node.getParent().getParent());
                        }
                    }

                }
            }
            root.setColor(false);
        }
        private void readFile(String filename) {
            Scanner scan = null;
            String line;
            int lineNum = 1;
            try {
                scan = new Scanner(new File(filename));
            } catch (FileNotFoundException e) {
                System.out.println("Could not open " + filename + " in tree");
                System.exit(-1);
            }
            while (scan.hasNextLine()) {
                line = scan.nextLine();
                line = line.replace("\n", "").replace("\r", "").trim();
                addLine(line, lineNum);
                lineNum++;
            }
        }
        private void printNode(Node node) {
            if(node == NIL) {return;}
            if(node.getLeft() != NIL) {printNode(node.getLeft());}
            System.out.println(node.getLineNum() + ": " + node.getLine());
            if(node.getRight() != NIL) {printNode(node.getRight());}
        }
        private int depth(Node node) {
            int left = 0, right = 0;
            if(node.getLeft() != NIL) {
                left = 1 + depth(node.getLeft());
            }
            if(node.getRight() != NIL) {
                right = 1 + depth(node.getRight());
            }
            return Math.max(left, right);
        }
        private void findLine(Node node, ArrayList<Integer> arr, String target) {
            if(node == NIL) {return;}
            if(target.equals(node.getLine())) {
                findLine(node.getLeft(), arr, target);
                linearAdd(arr, node.getLineNum());
                findLine(node.getRight(), arr, target);
            }
            else {
                if (target.compareTo(node.getLine()) < 0) {
                    findLine(node.getLeft(), arr, target);
                } else {
                    findLine(node.getRight(), arr, target);
                }
            }
        }

        private void containsLine(Node node, ArrayList<Integer> arr, String substr, String smellType) {
            if(node == NIL) {return;}
            containsLine(node.getLeft(), arr, substr, smellType);
            if(node.getLine().contains(substr)) {
                linearAdd(arr, node.getLineNum());
            }
            containsLine(node.getRight(), arr, substr, smellType);
        }

        private void linearAdd(ArrayList<Integer> arr, int lineNum) {
            if(arr.isEmpty() || arr.get(arr.size() - 1) <= lineNum) {arr.add(lineNum);}
            else {
                boolean placeFound = false;
                for (int index = arr.size() - 1; index >= 0 && !placeFound; index--) {
                    if(arr.get(index) < lineNum) {
                        arr.add(index + 1, lineNum);
                        placeFound = true;
                    }
                }
                if(!placeFound) {arr.add(0, lineNum);}
            }
        }

        /**
         * Finds and returns the line number of a single line of code.
         * @param target The raw line of code to be found. Do not trim or remove any characters from this line.
         *               This method does all the prep necessary.
         * @param smellType The type of smell the offending code is. Use the SmellEnum class for consistency.
         * @param smells The array list of smells. It is assumed that this array is pre-sorted by line numbers.
         * @return line number of the single line offending smell, or -1 if a line number could not be found
         */
        public int findSingle(String target, String smellType, ArrayList<Smell> smells) {
            boolean usedContains = false;
            boolean found = true;
            ArrayList<Integer> array = new ArrayList<Integer>();
            String temp = target.trim();
            findLine(root, array, temp);
            if(array.size() == 0) {
                containsLine(root, array, temp, smellType);
                if(array.size() == 0) {
                    return -1;
                }
                usedContains = true;
            }
            int index = 0; // keeps track of line number inside array
            for (int x = 0; x < SMELLS.size() && index < array.size(); x++) {
                if (SMELLS.get(x).getSmellType().equals(smellType) && SMELLS.get(x).getLineNum() == array.get(index)
                        && (SMELLS.get(x).getCode().contains(temp) || SMELLS.get(x).getCode().equals(temp))) {
                    index++;
                    if (index >= array.size()) {
                        found = false;
                    }
                }
            }

            if(found) {
                return array.get(index);
            }
            else if(!usedContains) {
                array.clear();
                containsLine(root, array, temp, smellType);
                if(array.size() == 0) {
                    return -1;
                }

                index = 0;
                for (int x = 0; x < SMELLS.size() && index < array.size(); x++) {
                    if (SMELLS.get(x).getSmellType().equals(smellType) && SMELLS.get(x).getLineNum() == array.get(index)
                            && (SMELLS.get(x).getCode().contains(temp) || SMELLS.get(x).getCode().equals(temp))) {
                        index++;
                        if (index >= array.size()) {
                            return -1;
                        }
                    }
                }
                return array.get(index);
            }
            else {
                return -1;
            }
        }

        /**
         * Finds and returns the line number of the first line of multiple lines of code stored
         * in a single string object.
         * @param target The raw line of code to be found. Do not trim or remove any characters from this line.
         *               This method does all the prep necessary.
         * @param smellType The type of smell the offending code is. Use the SmellEnum class for consistency.
         * @param smells The array list of smells. It is assumed that this array is pre-sorted by line numbers.
         * @return the line number of the first line within the multi-line representation, or -1 if it
         * could not find the line number of every single line in the multi-line representation.
         */
        public int findMulti(String target, String smellType, ArrayList<Smell> smells) {
            String tempStr = target.trim();
            String[] strArr = tempStr.split("\n");
            ArrayList<ArrayList<Integer>> array = new ArrayList<ArrayList<Integer>>();
            ArrayList<Integer> temp; // contains -1 if empty line
            for (String str : strArr) {
                str = str.trim();
                temp = new ArrayList<Integer>();
                if (str.length() == 0) {
                    temp.add(-1);
                } else {
                    findLine(root, temp, str);
                    if (temp.size() == 0) {
                        containsLine(root, temp, str, smellType);
                        if (temp.size() == 0) {
                            return -1;
                        }
                    }
                }
                array.add(temp);
            }

            // removes line numbers from the first array list that already appear in the smells array
            int index = 0;
            for(int i = 0; i < smells.size() && index < array.get(0).size(); i++) {
                if(smells.get(i).getSmellType().equals(smellType)) {
                    while(index < array.get(0).size() && smells.get(i).getLineNum() > array.get(0).get(index)
                            && (smells.get(i).getCode().contains(tempStr) || smells.get(i).getCode().equals(tempStr))) {
                        index++;
                    }
                    if(index >= array.get(0).size()) {continue;}
                    if(smells.get(i).getLineNum() == array.get(0).get(index)) {array.get(0).remove(index);}
                }
            }

            // finds the earliest succession of integers
            int current = 0;
            boolean foundBreak = false;
            for(int i = 0; i < array.get(0).size(); i++) {
                foundBreak = false;
                current = array.get(0).get(i);
                for(int j = 1; j < array.size(); j++) {
                    if(array.get(j).get(0) != -1 && !(array.get(j).contains(current + j))) {
                        foundBreak = true;
                        break;
                    }
                }
                if(!foundBreak) {
                    return current;
                }
            }
            return -1;
        }
    }

    static class SmellEnum {
        public static final String gotoStmt = "Goto statement";
        public static final String emptyStmt = "Empty statement";
        public static final String magicNum = "Magic number";
        public static final String blocklessLoop = "Blockless loop";
        public static final String blocklessIf = "Blockless if";
        public static final String longParam = "Long parameter list";
        public static final String longFunc = "Long function";
        public static final String deadFunc = "Dead function";
        public static final String embed = "Embedded inc/dec";
        public static final String secur = "Security issues";
        public static final String contStmt = "Continue statement";
        public static final String breakStmt = "Break statement";
        public static final String varName = "Bad variable name";
        public static final String funcName = "Bad function name";
        public static final String multVar = "Multi-variable declaration";
        public static final String globalVar = "Global Variable";
    }
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    static int LONGPARAMTHRESHOLD = 6;
    static int LONGFUNCTIONTHRESHOLD = 50;
    static boolean[] settings = new boolean[19];
    static ArrayList<Smell> SMELLS = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        String fullStr = "srcml " + args[0] + " -o " + args[0] + ".xml";
        settingsHandler(args);
        String[] strArray;
        strArray = fullStr.split("\\s+");
        System.out.println("\nCreating " + args[0] + " to " + args[0] + ".xml\n");
        Process process;
        process = Runtime.getRuntime().exec(strArray);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        smellHandler(args);

        for (Smell smell : SMELLS) {
            System.out.println(smell.getSmellType());
        }
    }

    public static void settingsHandler(String[] args) {
        Arrays.fill(settings, true);
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
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
                    if (args[i + 1].charAt(0) != '-') {
                        try {
                            LONGPARAMTHRESHOLD = Integer.parseInt(args[i + 1]);
                        } catch (Exception e) {
                            System.out.println("Couldn't parse long parameter list threshold");
                            break;
                        }
                        break;
                    } else {
                        settings[7] = false;
                    }
                    break;
                case "-f":              //Long functions                CURRENT BUG: doesn't disable if last argument
                    if (args[i + 1].charAt(0) != '-') {
                        try {
                            LONGFUNCTIONTHRESHOLD = Integer.parseInt(args[i + 1]);
                        } catch (Exception e) {
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
        if (settings[0]) {                //Dictionary of Symbols
        }
        if (!settings[1]) {                //smell report
            return;
        }
        if (settings[2]) {                //Goto statements
            gotoHandler(args[0]);
        }
        if (settings[3]) {                //Empty Statements
            emptyStmtHandler(args[0]);
        }
        if (settings[4]) {                //Magic numbers
            magicNumHandler(args[0]);
        }
        if (settings[5]) {                //Block-less if statements
            noBlockLoop(args[0]);
        }
        if (settings[6]) {                //Block-less loops
            noBlockIf(args[0]);
        }
        if (settings[7]) {                //Long parameter list
            longParamHandler(args[0]);
        }
        if (settings[8]) {                //Long functions
            longFuncHandler(args[0]);
        }
        if (settings[9]) {                //Dead functions
            deadCodeHandler(args[0]);
        }
        if (settings[10]) {                //Embedded increment/decrement
        }
        if (settings[11]) {                //Conditional complexity
        }
        if (settings[12]) {               //Security issues
        }
        if (settings[13]) {               //Deep block nesting
        }
        if (settings[14]) {               //Continue statements
        }
        if (settings[15]) {               //Break statements
        }
        if (settings[16]) {               //Bad variable names
            variableNaming(args[0]);
        }
        if (settings[17]) {               //Bad function names
            functionNaming(args[0]);
        }
        if (settings[18]) {               //Multiple variable declarations on one line
        }
    }

    private static void functionNaming(String fileName) {
        int i = 0;
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> functionList = new ArrayList<String>();

        File bufferFile = new File(bufferFileName);
        File functionFile = new File("functions.txt");

        // creating file with all function names
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:function/src:name\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit/src:name)\"", bufferFileName);
        builder2.redirectOutput(functionFile);
        builder2.redirectError(new File("out.txt"));
        System.out.println("---------------------");
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
        System.out.println("---------------------");

        // putting all function names into arraylist
        try {
            Scanner scan = new Scanner(functionFile);
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
                SMELLS.add(new Smell("Non camel case function", functionList.get(i)));
            }
        }

        bufferFile.delete();
        functionFile.delete();
    }

    private static void variableNaming(String fileName) {
        int i = 0;
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> varList = new ArrayList<String>();

        File bufferFile = new File(bufferFileName);
        File varFile = new File("variables.txt");

        // creating file with all variable names
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:decl/src:name\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit/src:name)\"", bufferFileName);
        builder2.redirectOutput(varFile);
        builder2.redirectError(new File("out.txt"));
        System.out.println("---------------------");
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
        System.out.println("---------------------");

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

        // checking variable name for camel case
        String camelCase = "([a-z]+[A-Z]+\\w+)+";
        for (i = 0; i < varList.size(); i++) {
            if (!varList.get(i).matches(camelCase) && !varList.get(i).equals("argc") && !varList.get(i).equals("argv[]")) {
                SMELLS.add(new Smell("Non camel case variable", varList.get(i)));
            }
        }

        bufferFile.delete();
        varFile.delete();
    }

    private static void longFuncHandler(String fileName) {
        int i = 0;
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> functionList = new ArrayList<String>();
        File bufferFile = new File(bufferFileName);
        File functionFile = new File("functions.txt");
        // creating file with all function names
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:function/src:name\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit/src:name)\"", bufferFileName);
        builder2.redirectOutput(functionFile);
        builder2.redirectError(new File("out.txt"));
        System.out.println("---------------------");
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
        System.out.println("---------------------");
        // putting all function names into arraylist
        try {
            Scanner scan = new Scanner(functionFile);
            while (scan.hasNextLine()) {
                buffer = scan.nextLine();
                functionList.add(buffer);
            }
            scan.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int lineNum, lineAmount, functionFlag, bracketCounter, stopCounting, origLineNum;
        lineNum = lineAmount = functionFlag = bracketCounter = stopCounting = origLineNum = 0;
        String origCode = "";
        try {
            File origFile = new File(fileName);
            Scanner scan2 = new Scanner(origFile);
            while (scan2.hasNextLine()) {
                lineNum++;
                buffer = scan2.nextLine();
                for (i = 0; i < functionList.size(); i++) {
                    if ((buffer.contains(functionList.get(i))) && (buffer.contains("{")) && (functionFlag == 0) && (bracketCounter == 0)) {
                        functionFlag = 1;
                        origLineNum = lineNum;
                        origCode = buffer;
                        break;
                    }
                }
                if (functionFlag == 1) {
                    if (buffer.contains("{")) {
                        bracketCounter++;
                    }
                    if (buffer.contains("}")) {
                        bracketCounter--;
                    }
                    lineAmount++;
                }
                if (bracketCounter == 0) {
                    functionFlag = lineAmount = stopCounting = 0;
                }
                if ((lineAmount > 20) && (functionFlag == 1) && (bracketCounter > 0) && (stopCounting == 0)) {
                    SMELLS.add(new Smell(origLineNum, "Long method/function on line " + origLineNum, origCode.trim()));
                    stopCounting = 1;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bufferFile.delete();
        functionFile.delete();
    }

    private static void noBlockLoop(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> outputList = new ArrayList<String>();
        int i;

        File bufferFile = new File(bufferFileName);
        File outputFile = new File("output.txt");

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:block[@type='pseudo']/parent::node()\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(outputFile);
        builder2.redirectError(new File("out.txt"));

        System.out.println("---------------------");
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
        System.out.println("---------------------");

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
            if (outputList.get(i).contains("while") || outputList.get(i).contains("for") || outputList.get(i).contains("do"))
                SMELLS.add(new Smell("No block loop", outputList.get(i)));
        }

        bufferFile.delete();
        outputFile.delete();
    }

    private static void noBlockIf(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> outputList = new ArrayList<String>();
        int i;

        File bufferFile = new File(bufferFileName);
        File outputFile = new File("output.txt");

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:block[@type='pseudo']/parent::node()\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(outputFile);
        builder2.redirectError(new File("out.txt"));

        System.out.println("---------------------");
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
        System.out.println("---------------------");

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
            if (outputList.get(i).contains("if"))
                SMELLS.add(new Smell("No block if", outputList.get(i)));
        }

        bufferFile.delete();
        outputFile.delete();
    }

    private static Smell emptyStmtHandler(String filename) {
       // System.out.println("Running XPath: Finding Empty Statements in " + filename + ":");
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
            if (output.length() > 0) {
                System.out.println(before + output);
            }
            iterator++;
        } while (output.length() > 0); // checks to see if a line was retrieved from the code. If not, end loop


        return new Smell();
    }

    private static Smell magicNumHandler(String filename) {
      //  System.out.println("Running XPath: Finding Magic Numbers in " + filename + ":");
        String xpathName = filename + ".xml";
        String argument;
        String output = "";
        String outputParse[];
        int iterator = 1;
        boolean containsConst = false; //used for declaration statements

        // handles expression statements
        do {
            // gets each expression which assigns a literal number to a variable
            argument = "string(//src:expr_stmt[src:expr[src:literal/@type='number']][" + iterator + "])";
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
            output = output.replace("\n", "").replace("\r", "");
            outputParse = output.split("\\s+");
            if (outputParse.length > 1) {
                System.out.println("Magic Number: " + output);
            }
            iterator++;
        } while (outputParse.length > 1); // checks to see if a line was retrieved from the code. If not, end loop

        iterator = 1; // reset to 1 for the next loop

        // handles declaration statements that are not of constant integers
        do {
            // gets each declaration statement that assigns a literal number to a non-constant variable
            argument = "string(//src:decl_stmt[src:decl/src:init/src:expr/src:literal/@type='number'][" + iterator + "])";
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
            output = output.replace("\n", "").replace("\r", "");
            outputParse = output.split("\\s+");

            //checks to see if 'const' type appears in the statement
            for (String s : outputParse) {
                if (s.equals("const")) {
                    containsConst = true;
                }
            }

            if (outputParse.length > 1 && !containsConst) {
                System.out.println("Magic Number: " + output);
            }
            iterator++;
            containsConst = false;
        } while (outputParse.length > 1); // checks to see if a line was retrieved from the code. If not, end loop

        return new Smell();
    }

    private static void longParamHandler(String filename) {
        String xpathName = filename + ".xml";
        String output = "";
        String function = "";
        String[] outputParse;
        int i = 1;
        do {
            int paramNum = 0;
            ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:function[" + i + "]/src:parameter_list)\"", xpathName);
            ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:function[" + i + "])\"", xpathName);
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
            outputParse = output.split("\\s+");
            i++;
            if (outputParse.length > 1) {
                char[] outChars = output.toCharArray();
                for (char outChar : outChars) {
                    if (outChar == ',' || outChar == ')') {
                        paramNum++;
                    }
                }
                if (paramNum >= LONGPARAMTHRESHOLD) {
                    SMELLS.add(new Smell("Long Parameter List", function));
                }
            }
        } while (outputParse.length > 1);
    }

    public static void gotoHandler(String filename) throws IOException {
        String xpathName = filename + ".xml";

       // System.out.println("Running XPath: Finding goto statements");
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "//src:goto", xpathName);
        builder.redirectOutput(new File("results.txt"));
        builder.redirectError(new File("out.txt"));
        System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
        } catch (InterruptedException e) {
            System.out.print("");
        }
        System.out.println("---------------------");
    }

    public static void deadCodeHandler(String filename) throws IOException {
        String xpathName = filename + ".xml";

      //  System.out.println("Running XPath: Finding Dead Code");
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "//block_content/decl_stmt[not(following::*[1]/use | following::*[1]/call)]/decl/name/text() | //block_content/expr_stmt[not(following::*[1]/use | following::*[1]/call)]/expr/*[1]/name/text()", xpathName);
        builder.redirectOutput(new File("results.txt"));
        builder.redirectError(new File("out.txt"));
        System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
        } catch (InterruptedException e) {
            System.out.print("");
        }
        System.out.println("---------------------");
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
            String error = "-" + smellType + ": \n" + code;
            //TODO error formatting
            return error;
        }
    }
}
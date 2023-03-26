import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static int LONGPARAMTHRESHOLD = 6;
    static int LONGFUNCTIONTHRESHOLD = 50;
    static boolean[] settings = new boolean[20];
    static ArrayList<Smell> SMELLS = new ArrayList<>();
    static SourceBSTree tree;
    public static void main(String[] args) throws IOException {

        settingsHandler(args);
        // This must remain unchanged
        String fullStr = "srcml " + args[0] + " -o " + args[0] + ".xml";
        String[] strArray;
        strArray = fullStr.split("\\s+");
        System.out.println("\nCreating " + args[0] + " to " + args[0] + ".xml\n");
        Process process = Runtime.getRuntime().exec(strArray);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Error: " + e.getMessage());
        }
        // here on downwards is editable for testing purposed
        tree = new SourceBSTree(args[0]);
        smellHandler(args);

        for (Smell smell : SMELLS) {
            System.out.print(smell.getLineNum());
            System.out.print(" ");
            System.out.print(smell.getSmellType());
            System.out.print(": ");
            System.out.println(smell.getCode());
        }
    }

    public static void smellHandler(String[] args){
        if(settings[0]){                //Dictionary of Symbols

        }
        if(!settings[1]){                //smell report
            return;
        }
        if(settings[2]){                //Goto statements
            try {
                gotoHandler(args[0]);
            } catch (IOException e){
                System.out.println("IOException: " + e);
            }
        }
        if(settings[3]){                //Empty Statements
            emptyStmtHandler(args[0]);
        }
        if(settings[4]){                //Magic numbers
            magicNumHandler(args[0]);
        }
        if(settings[5]){                //Block-less if statements
            noBlockLoopIf(args[0]);
        }
        if(settings[6]){                //Block-less loops
            //noBlockLoopIf(args[0]);
        }
        if(settings[7]){                //Long parameter list
            longParamHandler(args[0]);
        }
        if(settings[8]){                //Long functions
            longFuncHandler(args[0]);
        }
        if(settings[9]){                //Dead functions
            try {
                deadCodeHandler(args[0]);
            } catch (IOException e){
                System.out.println("IOException: " + e);
            }
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

        }
        if(settings[15]){               //Break statements

        }
        if(settings[16]){               //Bad variable names
            //variableNaming(args[0]);
        }
        if(settings[17]){               //Bad function names
            //functionNaming(args[0]);
        }
        if(settings[18]){               //Multiple variable declarations on one line

        }
        if(settings[19]){               //Global Variables
            globalVariableHandler(args[0]);
        }
    }

    public static void settingsHandler(String[] args){
        for(int i = 0;i<settings.length;i++){
            settings[i] = true;
        }
        for(int i = 0;i<args.length-1;i++){
            switch(args[i]){
                case "-g":              //Goto statements
                    settings[2] = false;
                    continue;
                case "-e":              //Empty statements
                    settings[3] = false;
                    continue;
                case "-m":              //Magic numbers
                    settings[4] = false;
                    continue;
                case "-i":              //Block-less if statements
                    settings[5] = false;
                    continue;
                case "-l":              //Block-less loops
                    settings[6] = false;
                    continue;
                case "-p":              //Long parameter lists
                    if(args[i + 1].charAt(0) != '-'){
                        try{
                            LONGPARAMTHRESHOLD = Integer.parseInt(args[i+1]);
                        } catch(Exception e){
                            System.out.println("Couldn't parse long parameter list threshold");
                            continue;
                        }
                        continue;
                    } else {
                        settings[7] = false;
                    }
                    continue;
                case "-f":              //Long functions
                    if(args[i + 1].charAt(0) != '-'){
                        try{
                            LONGFUNCTIONTHRESHOLD = Integer.parseInt(args[i+1]);
                        } catch(Exception e){
                            System.out.println("Couldn't parse long function threshold");
                            continue;
                        }
                        continue;
                    } else {
                        settings[8] = false;
                    }
                    continue;
                case "-d":              //Dead functions
                    settings[9] = false;
                    continue;
                case "-x":              //Conditional complexity
                    settings[11] = false;
                    continue;
                case "-s":              //Security issues
                    settings[12] = false;
                    continue;
                case "-c":              //Continue statements
                    settings[14] = false;
                    continue;
                case "-b":              //Break statements
                    settings[15] = false;
                    continue;
                case "-v":              //Bad variable names
                    settings[16] = false;
                    continue;
                case "-n":              //Bad function names
                    settings[17] = false;
                    continue;
                default:
                    continue;
            }
        }
    }

    public static void addSmell(String smellType, String code, int line) {
        Smell smell = new Smell(line, smellType, code);
        boolean isPlaced = false;
        if(SMELLS.size() == 0 ||
                SMELLS.get(SMELLS.size() - 1).getLineNum() <= line) {
            SMELLS.add(smell);
            return;
        }
        for(int i = 0; i < SMELLS.size() && !isPlaced; i++) {
            if(SMELLS.get(i).getLineNum() >= line) {
                SMELLS.add(i, smell);
                isPlaced = true;
            }
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
        //System.out.println("---------------------");
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
        //System.out.println("---------------------");

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
        //System.out.println("---------------------");
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
        //System.out.println("---------------------");

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
            if (!varList.get(i).matches(camelCase)) {
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
    private static void noBlockLoopIf(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";

        File bufferFile = new File(bufferFileName);

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:block[@type='pseudo']/parent::node()\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        //builder2.redirectOutput(Redirect.INHERIT);
        builder2.redirectError(new File("out.txt"));

        //System.out.println("No block loop/if:");
        //System.out.println("---------------------");
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
        //System.out.println("---------------------");
        bufferFile.delete();

    }
    private static Smell emptyStmtHandler(String filename) {
        //System.out.println("Running XPath: Finding Empty Statements in " + filename + ":");
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
        //System.out.println("Running XPath: Finding Magic Numbers in " + filename + ":");
        String xpathName = filename + ".xml";
        String argument;
        String output = "";
        String outputParse[];
        int iterator = 1;
        int lineNum = -1;
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
//            output = output.replace("\n", "").replace("\r", "");
            outputParse = output.split("\\s+");
            if(outputParse.length > 1) {
//                System.out.println("Magic Number: " + output);
                lineNum = tree.findSingle(output, SmellEnum.magicNum, SMELLS);
                if(lineNum > -1) {
                    addSmell(SmellEnum.magicNum, output, lineNum);
                }
                else {
                    System.out.println("Unable to find line number on iteration" + iterator);
                    System.out.println("Output: '" + output + "'");
                }
            }
            iterator++;
        } while(outputParse.length > 1); // checks to see if a line was retrieved from the code. If not, end loop

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
//            output = output.replace("\n", "").replace("\r", "");
            outputParse = output.split("\\s+");

            //checks to see if 'const' type appears in the statement
            for(String s : outputParse) {
                if(s.equals("const")) {containsConst = true;}
            }

            if(outputParse.length > 1 && !containsConst) {
                lineNum = tree.findSingle(output, SmellEnum.magicNum, SMELLS);
                if(lineNum > -1) {
                    addSmell(SmellEnum.magicNum, output, lineNum);
                }
                else {
                    System.out.println("Unable to find line number on iteration" + iterator);
                    System.out.println("Output: '" + output + "'");
                }
            }
            iterator++;
            containsConst = false;
        } while(outputParse.length > 1); // checks to see if a line was retrieved from the code. If not, end loop
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
        String[] outputParse;
        int i = 1;
        do{
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
            if(outputParse.length > 1){
                char[] outChars = output.toCharArray();
                for (char outChar : outChars) {
                    if (outChar == ',' || outChar == ')') {
                        paramNum++;
                    }
                }
                int lineNum = tree.findSingle(function.substring(0,function.indexOf("{")), SmellEnum.longParam, SMELLS);
                if(paramNum >= LONGPARAMTHRESHOLD){
                    //SMELLS.add(new Smell("Long Parameter List", function.substring(0,function.indexOf("{"))));
                    addSmell(SmellEnum.longParam, function.substring(0,function.indexOf("{")),lineNum);
                }
            }

        } while(outputParse.length > 1);
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
////        Process process;
////        process = Runtime.getRuntime().exec(filename);
//        try {
//            process.waitFor();
//        } catch (InterruptedException e) {
//            System.out.println("Error: " + e.getMessage());
//        }
        //System.out.println("Running XPath: Finding Dead Code");
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "//src:goto",xpathName);
        builder.redirectOutput(new File("results.txt"));
        builder.redirectError(new File("out.txt"));
        //System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
        }catch (InterruptedException e){
            System.out.print("");
        }
        //System.out.print("---------------------");
    }
    public static void deadCodeHandler(String filename) throws IOException {
        String xpathName = filename + ".xml";
//        Process process;
//        process = Runtime.getRuntime().exec(xpathName);
//        try {
//            process.waitFor();
//        } catch (InterruptedException e) {
//            System.out.println("Error: " + e.getMessage());
//        }
        //System.out.println("Running XPath: Finding Dead Code");
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "//block_content/decl_stmt[not(following::*[1]/use | following::*[1]/call)]/decl/name/text() | //block_content/expr_stmt[not(following::*[1]/use | following::*[1]/call)]/expr/*[1]/name/text()",xpathName);
        builder.redirectOutput(new File("results.txt"));
        builder.redirectError(new File("out.txt"));
        try {
            Process p = builder.start();
            p.waitFor();
        }catch (InterruptedException e){
            System.out.print("");
        }
        //System.out.print("---------------------");
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
                    //SMELLS.add(new Smell("Global Variable", output.substring(0,output.length()-1)));
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
            if(node == NIL) {
                return;
            }
            if(target.equals(node.getLine())) {
                findLine(node.getLeft(), arr, target);
                arr.add(node.getLineNum());
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
            if(node.getLine().contains(substr)) {
                if(smellType.equals(SmellEnum.magicNum)) {
                    int index = node.getLine().indexOf("int ");

                    // if it doesn't follow a declaratory int type, then we're good
                    if (index == -1 || index + 4 != node.getLine().indexOf(substr)) {
                        containsLine(node.getLeft(), arr, substr, smellType);
                        arr.add(node.getLineNum());
                        containsLine(node.getRight(), arr, substr, smellType);
                    }
                }
                else {
                    containsLine(node.getLeft(), arr, substr, smellType);
                    arr.add(node.getLineNum());
                    containsLine(node.getRight(), arr, substr, smellType);
                }
            }
            containsLine(node.getLeft(), arr, substr, smellType);
            containsLine(node.getRight(), arr, substr, smellType);
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
            ArrayList<Integer> array = new ArrayList<Integer>();
            String temp = target.trim();
            findLine(root, array, temp);
            if(array.size() == 0) {
                containsLine(root, array, temp, smellType);
                if(array.size() == 0) {
                    return -1;
                }
            }
            int index = 0; // keeps track of line number inside array
            for (Smell smell : smells) {
                if (smell.getSmellType().equals(smellType) && smell.getLineNum() == array.get(index)) {
                    index++;
                    if (index >= array.size()) {
                        return -1;
                    }
                }
            }
            return array.get(index);
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
                    while(index < array.get(0).size() && smells.get(i).getLineNum() > array.get(0).get(index)) {
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
        public static final String gotoStmt = "Goto Statement";
        public static final String emptyStmt = "Empty Statement";
        public static final String magicNum = "Magic Number";
        public static final String blockless = "Blockless If/Loop";
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class Main {
    static int LONGPARAMTHRESHOLD = 6;
    static ArrayList<Smell> SMELLS = new ArrayList<>();
    public static void main(String[] args) throws IOException {

        // This must remain unchanged
        String fullStr = "srcml " + args[0] + " -o " + args[0] + ".xml";
        String[] strArray;
        strArray = fullStr.split("\\s+");
        System.out.println("\nCreating " + args[0] + " to " + args[0] + ".xml\n");
        Process process = Runtime.getRuntime().exec(strArray);
        try {
            process.waitFor();
        } catch(InterruptedException e) {
            System.out.println("srcml was interrupted. Terminating...");
            process.destroy();
            System.exit(-1);
        }
        if(process.exitValue() != 0) {
            System.out.println("xml creation failed. Terminating...");
            System.exit(-1);
        }

        // here on downwards is editable for testing purposed
        // longParamHandler(args[0]);
        // gotoHandler(args[0]);
        //longFuncHandler(args[0]);
        //noBlockLoopIf(args[0]);
        functionNaming(args[0]);
       // variableNaming(args[0]);

        for (Smell smell : SMELLS) {
            System.out.println(smell.getSmellType());
        }
    }

    private static Smell gotoHandler(String filename) {
        System.out.println("Running XPath: Finding GoTo Statements in " + filename + ":");
        String xpathName = filename + ".xml";
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:goto)\"", xpathName);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(new File("out.txt"));
        System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }
        System.out.print("---------------------");

        // outputs nothing right now. Will output stuff after smells are complete
        return new Smell();
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
                if(paramNum >= LONGPARAMTHRESHOLD){
                    SMELLS.add(new Smell("Long Parameter List", function.substring(0,function.indexOf("{"))));
                }
            }

        } while(outputParse.length > 1);
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
            if (!varList.get(i).matches(camelCase)) {
                SMELLS.add(new Smell("Non camel case variable", varList.get(i)));
            }
        }

        bufferFile.delete();
        varFile.delete();
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

    private static void noBlockLoopIf(String fileName) {
        String xpathName = fileName + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";

        File bufferFile = new File(bufferFileName);

        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"//src:block[@type='pseudo']/parent::node()\"", xpathName);
        builder.redirectOutput(bufferFile);
        builder.redirectError(new File("out.txt"));
        ProcessBuilder builder2 = new ProcessBuilder("srcml", "--xpath", "\"string(//src:unit)\"", bufferFileName);
        builder2.redirectOutput(Redirect.INHERIT);
        builder2.redirectError(new File("out.txt"));

        System.out.println("No block loop/if:");
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
        bufferFile.delete();

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


        // check if file contains long functions
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

}




class Smell {
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
        String error = smellType + ": \n" + code;
        //TODO error formatting
        return error;
    }

}
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
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
        //gotoHandler(args[0]);
        longFuncHandler(args[0]);
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

    private static Smell longFuncHandler(String filename) {
        int n = 10;
        int i = 0;
        String xpathName = filename + ".xml";
        String bufferFileName = "buffer.xml";
        String buffer = "";
        ArrayList<String> functionList = new ArrayList<String>(n);


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
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check if file contains long functions
        int lineNum, lineAmount, functionFlag, bracketCounter, stopCounting, origLineNum;
        lineNum = lineAmount = functionFlag = bracketCounter = stopCounting = origLineNum = 0;
        int j = 0;
        try {
            File origFile = new File(filename);
            Scanner scan2 = new Scanner(origFile);
            while (scan2.hasNextLine()) {
                lineNum++;
                buffer = scan2.nextLine();
                for (i = 0; i < functionList.size(); i++) {
                    if ((buffer.contains(functionList.get(i))) && (buffer.contains("{")) && (functionFlag == 0) && (bracketCounter == 0)) {
                        functionFlag = 1;
                        origLineNum = lineNum;
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
                    j++;
                    System.out.println("found a smell " + j + " " + origLineNum + "-" + lineNum);
                    stopCounting = 1;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        return new Smell();
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
        String error = new String();
        //TODO error formatting

        return error;
    }

}
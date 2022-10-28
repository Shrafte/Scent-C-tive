import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
        longParamHandler(args[0]);
        gotoHandler(args[0]);

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
                    SMELLS.add(new Smell("Long Parameter List", function));
                }
            }

        } while(outputParse.length > 1);
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
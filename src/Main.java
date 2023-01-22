import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    static int LONGPARAMTHRESHOLD = 6;
    static int LONGFUNCTIONTHRESHOLD = 50;
    static boolean[] settings = new boolean[19];
    static ArrayList<Smell> SMELLS = new ArrayList<>();
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
        smellHandler(args);

        for (Smell smell : SMELLS) {
            System.out.println(smell.getSmellType());
        }
    }

    public static void smellHandler(String[] args){
        if(settings[0]){                //Dictionary of Symbols

        }
        if(!settings[1]){                //smell report
            return;
        }
        if(settings[2]){                //Goto statements
            gotoHandler(args[0]);
        }
        if(settings[3]){                //Empty Statements

        }
        if(settings[4]){                //Magic numbers

        }
        if(settings[5]){                //Block-less if statements

        }
        if(settings[6]){                //Block-less loops

        }
        if(settings[7]){                //Long parameter list
            longParamHandler(args[0]);
        }
        if(settings[8]){                //Long functions

        }
        if(settings[9]){                //Dead functions

        }
        if(settings[10]){                //Embedded increment/decrement

        }
        if(settings[11]){                //Conditional complexity

        }
        if(settings[12]){               //Security issues

        }
        if(settings[13]){               //Deep block nesting

        }
        if(settings[14]){               //Continue statements

        }
        if(settings[15]){               //Break statements

        }
        if(settings[16]){               //Bad variable names

        }
        if(settings[17]){               //Bad function names

        }
        if(settings[18]){               //Multiple variable declarations on one line

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
                    if(!(args[i+1].substring(0,1).equals("-"))){
                        try{
                            LONGPARAMTHRESHOLD = Integer.parseInt(args[i+1]);
                        } catch(Exception e){
                            System.out.println("Couldn't parse long parameter list threshold");
                            break;
                        }
                        break;
                    } else {
                        settings[7] = false;
                    }
                    break;
                case "-f":              //Long functions                CURRENT BUG: doesn't disable if last argument
                    if(!args[i+1].substring(0,1).equals("-")){
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

    private static Smell gotoHandler(String filename) {
        System.out.println("Running XPath: Finding GoTo Statements in " + filename + ":");
        String xpathName = filename + ".xml";
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "\"string(//src:goto)\"", xpathName);
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.redirectError(new File("out.txt"));
        //System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
        } catch (IOException e) {
            System.out.print("");
        } catch (InterruptedException e) {
            System.out.print("");
        }
        //System.out.print("---------------------");

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
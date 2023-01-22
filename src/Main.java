import java.io.File;
import java.io.IOException;

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
        emptyStmtHandler(args[0]);
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

    private static Smell magicNumHandler(String filename) {
        System.out.println("Running XPath: Finding Magic Numbers in " + filename + ":");
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
            if(outputParse.length > 1) {
                System.out.println("Magic Number: " + output);
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
            output = output.replace("\n", "").replace("\r", "");
            outputParse = output.split("\\s+");

            //checks to see if 'const' type appears in the statement
            for(String s : outputParse) {
                if(s.equals("const")) {containsConst = true;}
            }

            if(outputParse.length > 1 && !containsConst) {
                System.out.println("Magic Number: " + output);
            }
            iterator++;
            containsConst = false;
        } while(outputParse.length > 1); // checks to see if a line was retrieved from the code. If not, end loop

        return new Smell();
    }

    private static Smell emptyStmtHandler(String filename) {
        System.out.println("Running XPath: Finding Empty Statements in " + filename + ":");
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
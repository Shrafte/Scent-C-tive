import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String fullStr = "srcml " + args[0] + " -o " + args[0] + ".xml";
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
        goTo(args[0]);
        deadCode(args[0]);
    }

    private static void goTo(String filename) throws IOException {
        String xpathName = filename + ".xml";
        String output = "";

        System.out.println("Running XPath: Finding GoTo Statements");
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "//src:goto",xpathName);
        builder.redirectOutput(new File("results.txt"));
        builder.redirectError(new File("out.txt"));
        System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
            output = new String(p.getInputStream().readAllBytes());
        }catch (InterruptedException e){
            System.out.print("");
        }
        System.out.print("---------------------");
    }
    private static void deadCode(String filename) throws IOException {
        String xpathName = filename + ".xml";
        String output = "";

        System.out.println("Running XPath: Finding Dead Code");
        ProcessBuilder builder = new ProcessBuilder("srcml", "--xpath", "//block_content/decl_stmt[not(following::*[1]/use | following::*[1]/call)]/decl/name/text() | //block_content/expr_stmt[not(following::*[1]/use | following::*[1]/call)]/expr/*[1]/name/text()",xpathName);
        builder.redirectOutput(new File("results.txt"));
        builder.redirectError(new File("out.txt"));
        System.out.println("---------------------");
        try {
            Process p = builder.start();
            p.waitFor();
        }catch (InterruptedException e){
            System.out.print("");
        }
        System.out.print("---------------------");
    }
}
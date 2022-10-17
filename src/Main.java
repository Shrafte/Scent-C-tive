import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String fullStr = "srcml " + args[0] + " -o HelloWorld.cpp.xml";
        String[] strArray;
        strArray = fullStr.split("\\s+");
        System.out.println("\nCreating " + args[0] + " to " + args[0] + ".xml\n");
        Process process = Runtime.getRuntime().exec(strArray);
        System.out.println("Running XPath: Finding GoTo Statements in " + args[0] + ":");
        ProcessBuilder builder = new ProcessBuilder("srcml","--xpath","\"string(//src:goto)\"","HelloWorld.cpp.xml");
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
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
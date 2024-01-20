package org.com;

import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "wctool", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "A simple WC Tool to print number of words, lines and bytes in a file")
public class WCTool implements Callable<Integer> {

    @CommandLine.Option(names = "-c", description = "number of bytes")
    private boolean shouldCountBytes;

    @CommandLine.Option(names = "-l", description = "number of lines")
    private boolean shouldCountLines;

    @CommandLine.Option(names = "-w", description = "number of words")
    private boolean shouldCountWords;

    @CommandLine.Option(names = "-m", description = "number of characters")
    private boolean shouldCountCharacters;

    @CommandLine.Parameters(index = "0", description = "The file whose count should be calculated.")
    private File file;

    @Override
    public Integer call() throws Exception {
        if (shouldCountBytes || shouldCountCharacters) {
            System.out.println(countBytes() + " " + file.getName());
        }
        if (shouldCountWords) {
            System.out.println(countWords() + " " + file.getName());
        }
        if (shouldCountLines) {
            System.out.println(countLines() + " " + file.getName());
        }
        if (!(shouldCountLines || shouldCountWords || shouldCountBytes || shouldCountCharacters)) {
            System.out.println(countBytes() + " " + countLines() + " " + countWords() + " " + file.getName());
        }
        return 0;
    }

    private Integer countBytes() throws IOException {
        byte[] fileContents = Files.readAllBytes(file.toPath());
        return fileContents.length;
    }

    private Integer countLines() {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            int lineCount = 0;
            while (line != null) {
                lineCount++;
                line = br.readLine();
            }
            return (lineCount);
        } catch (IOException exception) {
            System.out.println("File not found!");
            return 0;
        }
    }

    private Integer countWords() {
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            int wordCount = 0;
            while (line != null) {
               wordCount += line.split(" ").length;
               line = br.readLine();
            }
            return (wordCount);
        } catch (IOException exception) {
            System.out.println("File not found!");
            return 0;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new WCTool()).execute(args);
        System.exit(exitCode);
    }
}

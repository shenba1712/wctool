package org.com;

import picocli.CommandLine;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.Objects.nonNull;

@CommandLine.Command(name = "wctool", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "A simple WC Tool to print number of words, lines, characters, and bytes in a file")
public class WCTool implements Callable<Integer> {

    @CommandLine.Option(names = "-c", description = "calculate number of bytes")
    private boolean shouldCountBytes;

    @CommandLine.Option(names = "-l", description = "calculate number of lines")
    private boolean shouldCountLines;

    @CommandLine.Option(names = "-w", description = "calculate number of words")
    private boolean shouldCountWords;

    @CommandLine.Option(names = "-m", description = "calculate number of characters")
    private boolean shouldCountCharacters;

    // If true, treat input as stdin
    // If false, treat input as file
    @CommandLine.Option(names = "--isPipedInput", hidden = true)
    private boolean isPipedInput;

    @CommandLine.Parameters(index = "0", description = "The file/text whose count should be calculated.")
    private String input;

    // check each flag individually to support multiple flags.
    // For example, -wl will calculate both number of words and lines.
    @Override
    public Integer call() {
        try {
            StringBuilder finalOutput = new StringBuilder();
            if (isPipedInput) {
                if (shouldCountBytes) {
                    finalOutput.append(countBytes(input)).append(" ");
                }
                if (shouldCountWords) {
                    finalOutput.append(countWords(input)).append(" ");
                }
                if (shouldCountLines) {
                    finalOutput.append(countLines(input)).append(" ");
                }
                if (shouldCountCharacters) {
                    finalOutput.append(countCharacters(input)).append(" ");
                }
                if (!(shouldCountLines || shouldCountWords || shouldCountBytes || shouldCountCharacters)) {
                    finalOutput.append(countBytes(input)).append(" ")
                            .append(countWords(input)).append(" ")
                            .append(countLines(input)).append(" ")
                            .append(countCharacters(input));
                }
                System.out.println(finalOutput);
            } else {
                File file = new File(input.trim());
                if (file.isFile()) {
                    if (shouldCountBytes) {
                        finalOutput.append(countBytes(file)).append(" ");
                    }
                    if (shouldCountWords) {
                        finalOutput.append(countWords(file)).append(" ");
                    }
                    if (shouldCountLines) {
                        finalOutput.append(countLines(file)).append(" ");
                    }
                    if (shouldCountCharacters) {
                        finalOutput.append(countCharacters(file)).append(" ");
                    }
                    if (!(shouldCountLines || shouldCountWords || shouldCountBytes || shouldCountCharacters)) {
                        finalOutput.append(countBytes(file)).append(" ")
                                .append(countWords(file)).append(" ")
                                .append(countLines(file)).append(" ")
                                .append(countCharacters(file)).append(" ");
                    }
                    finalOutput.append(file.getName());
                    System.out.println(finalOutput);
                } else {
                    System.err.println("File " + input.trim() + " not found!");
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private Integer countBytes(File file) {
        try {
            byte[] fileContents = Files.readAllBytes(file.toPath());
            return fileContents.length;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private Integer countBytes(String contents) {
        return contents.getBytes().length;
    }

    private Integer countLines(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            int lineCount = 0;
            while (nonNull(line)) {
                lineCount++;
                line = br.readLine();
            }
            return (lineCount);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private Long countLines(String input) {
        return input.lines().count();
    }

    private Long countWords(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            long wordCount = 0;
            while (nonNull(line)) {
                String[] words = line.trim().split("\\s+");
                wordCount += Arrays.stream(words)
                        .filter(w -> !w.isEmpty() && !w.matches("\\s+"))
                        .count();
                line = br.readLine();
            }
            return (wordCount);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return 0L;
    }

    private Long countWords(String contents) {
        String[] words = contents.split("\\s+");
        return Arrays.stream(words)
                .filter(w -> !w.isEmpty() && !w.matches("\\s+"))
                .count();
    }

    private Integer countCharacters(File file) {
        try {
            byte[] fileContents = Files.readAllBytes(file.toPath());
            String content = new String(fileContents);
            return content.length();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    private Long countCharacters(String contents) {
        return contents.chars().count();
    }

    public static void main(String[] args) throws IOException {
        // Check if piped input has value. If available, read the value and set it to the args
        // else, continue (to throw error that input is missing or work with the file)
        if (System.in.available() > 0) {
            StringBuilder fileContents = new StringBuilder();
            InputStreamReader isReader = new InputStreamReader(System.in);
            BufferedReader bufReader = new BufferedReader(isReader);
            String inputStr;
            while (nonNull(inputStr = bufReader.readLine())) {
                try {
                    fileContents.append(inputStr).append("\n");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            // Add the read input as a commandline argument.
            List<String> argsList = new ArrayList<>(Arrays.asList(args));
            argsList.add(fileContents.toString());
            argsList.add("--isPipedInput");
            args = argsList.toArray(new String[0]);
        }
        int exitCode = new CommandLine(new WCTool()).execute(args);
        System.exit(exitCode);
    }
}

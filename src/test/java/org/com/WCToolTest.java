package org.com;

import org.junit.jupiter.api.*;

import java.io.*;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

public class WCToolTest {

    private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final static ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final static PrintStream originalOut = System.out;
    private final static PrintStream originalErr = System.err;
    private final static InputStream originalIn = System.in;

    private final static String testFilePath = "/Users/shenba/Documents/workspace/wctool/src/main/resources/test.txt";
    private final static String testInput = "This is a test input";

    private static final String inputMissingError = """
                    Missing required parameter: '<input>'
                    Usage: wctool [-chlmVw] <input>
                    A simple WC Tool to print number of words, lines, characters, and bytes in a
                    file
                          <input>     The file/text whose count should be calculated.
                      -c              calculate number of bytes
                      -h, --help      Show this help message and exit.
                      -l              calculate number of lines
                      -m              calculate number of characters
                      -V, --version   Print version information and exit.
                      -w              calculate number of words
                    """;

    @BeforeAll
    public static void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterAll
    public static void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(originalIn);
    }

    @AfterEach
    public void resetStreams() {
        outContent.reset();
        errContent.reset();
    }

    @Test
    public void helperOptionsTest() throws Exception {
        String[] helperOptions = {"-h", "--help"};
        for (String option : helperOptions) {
            String[] args = {option};
            String expectedOutput = """
                    Usage: wctool [-chlmVw] <input>
                    A simple WC Tool to print number of words, lines, characters, and bytes in a
                    file
                          <input>     The file/text whose count should be calculated.
                      -c              calculate number of bytes
                      -h, --help      Show this help message and exit.
                      -l              calculate number of lines
                      -m              calculate number of characters
                      -V, --version   Print version information and exit.
                      -w              calculate number of words
                    """;
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            assertEquals(0, exitCode);
            assertEquals(expectedOutput, outContent.toString());
            outContent.reset();
        }
    }

    @Test
    public void versionOptionsTest() throws Exception {
        String[] versionOptions = {"-V", "--version"};
        for (String option : versionOptions) {
            String[] args = {option};
            String expectedOutput = "1.0.0\n";
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            assertEquals(0, exitCode);
            assertEquals(expectedOutput, outContent.toString());
            outContent.reset();
        }
    }

    @Nested
    public class FileTest {
        @Test
        public void FileNotFoundTest() throws Exception {
            String[] args = {"-w", "/path/to/random-file"};
            String expectedError = "File /path/to/random-file not found!\n";
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            assertEquals(0, exitCode);
            assertEquals(expectedError, errContent.toString());
            assertEquals("", outContent.toString());
        }

        @Test
        public void EmptyFilePathTest() throws Exception {
            String[] args = {"-w", ""};
            String expectedError = "File  not found!\n";
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            assertEquals(0, exitCode);
            assertEquals(expectedError, errContent.toString());
            assertEquals("", outContent.toString());
        }

        @Test
        public void NullFilePathTest() throws Exception {
            String[] args = {"-w", null};
            String expectedError = "java.lang.NullPointerException: Cannot invoke \"String.equals(Object)\" because \"arg\" is null";
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            assertEquals(1, exitCode);
            assertTrue(errContent.toString().contains(expectedError));
            assertEquals("", outContent.toString());
        }
    }

    @Nested
    public class WordCount {
        @Test
        public void countWordsNoFileTest() throws Exception {
            String[] args = {"-w"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countWordsWithFileTest() throws Exception {
            String[] args = {"-w", testFilePath};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "58164 test.txt\n");
        }

        @Test
        public void countWordsNoValueTest() throws Exception {
            String[] args = {"-w"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countWordsFromStdInTest() throws Exception {
            ByteArrayInputStream in = new ByteArrayInputStream(testInput.getBytes());
            System.setIn(in);

            String[] args = {"-w"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "5 \n");
        }
    }

    @Nested
    public class LineCount {
        @Test
        public void countLinesNoFileTest() throws Exception {
            String[] args = {"-l"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countLinesWithFileTest() throws Exception {
            String[] args = {"-l", testFilePath};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "7145 test.txt\n");
        }

        @Test
        public void countLinesNoValueTest() throws Exception {
            String[] args = {"-l"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countLinesFromStdInTest() throws Exception {
            ByteArrayInputStream in = new ByteArrayInputStream(testInput.getBytes());
            System.setIn(in);

            String[] args = {"-l"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "1 \n");
        }
    }

    @Nested
    public class ByteCount {
        @Test
        public void countBytesNoFile() throws Exception {
            String[] args = {"-c"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countBytesWithFileTest() throws Exception {
            String[] args = {"-c", testFilePath};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "335045 test.txt\n");
        }

        @Test
        public void countBytesNoValueTest() throws Exception {
            String[] args = {"-c"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countBytessFromStdInTest() throws Exception {
            ByteArrayInputStream in = new ByteArrayInputStream(testInput.getBytes());
            System.setIn(in);

            String[] args = {"-c"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "21 \n");
        }
    }

    @Nested
    public class CharacterCount {
        @Test
        public void countCharacterssNoFileTest() throws Exception {
            String[] args = {"-m"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countCharactersWithFileTest() throws Exception {
            String[] args = {"-m", testFilePath};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "332147 test.txt\n");
        }

        @Test
        public void countCharactersNoValueTest() throws Exception {
            String[] args = {"-m"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countCharactersFromStdInTest() throws Exception {
            ByteArrayInputStream in = new ByteArrayInputStream(testInput.getBytes());
            System.setIn(in);

            String[] args = {"-m"};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "21 \n");
        }
    }

    @Nested
    public class CountAll {
        @Test
        public void countAllNoFileTest() throws Exception {
            String[] args = {};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countAllWithFileTest() throws Exception {
            String[] args = {testFilePath};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "335045 58164 7145 332147 test.txt\n");
        }

        @Test
        public void countAllNoValueTest() throws Exception {
            String[] args = {};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            missingInputAssertion(exitCode);
        }

        @Test
        public void countAllFromStdInTest() throws Exception {
            ByteArrayInputStream in = new ByteArrayInputStream(testInput.getBytes());
            System.setIn(in);

            String[] args = {};
            int exitCode = catchSystemExit(() -> WCTool.main(args));
            successAssertion(exitCode, "21 5 1 21\n");
        }
    }

    private void missingInputAssertion(int exitCode) {
        assertEquals(2, exitCode);
        assertEquals(inputMissingError, errContent.toString());
        assertEquals("", outContent.toString());
    }

    private void successAssertion(int exitCode, String expectedOutput) {
        assertEquals(0, exitCode);
        assertEquals(expectedOutput, outContent.toString());
        assertEquals("", errContent.toString());
    }
}

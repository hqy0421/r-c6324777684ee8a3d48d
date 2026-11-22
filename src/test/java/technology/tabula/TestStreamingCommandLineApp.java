package technology.tabula;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestStreamingCommandLineApp {

    private static final String TWOTABLES = "src/test/resources/technology/tabula/twotables.pdf";
    private static final String SPANNING_CELLS = "src/test/resources/technology/tabula/spanning_cells.pdf";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void run(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);
        new CommandLineApp(new StringBuilder(), cmd).extractTables(cmd);
    }

    private String stdoutOf(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CommandLineApp.buildOptions(), args);
        StringBuilder out = new StringBuilder();
        new CommandLineApp(out, cmd).extractTables(cmd);
        return out.toString();
    }

    @Test
    public void testStreamingFileOutputMatchesDefaultOutput() throws ParseException, IOException {
        for (String format : new String[]{"CSV", "TSV", "JSON"}) {
            File regular = folder.newFile("regular." + format);
            File streaming = folder.newFile("streaming." + format);

            run(new String[]{TWOTABLES, "-p", "all", "-f", format, "-o", regular.getAbsolutePath()});
            run(new String[]{TWOTABLES, "-p", "all", "-f", format, "-o", streaming.getAbsolutePath(), "--streaming"});

            assertArrayEquals(format + " output must be identical",
                    Files.readAllBytes(regular.toPath()), Files.readAllBytes(streaming.toPath()));
            assertTrue(streaming.length() > 0);
        }
    }

    @Test
    public void testStreamingStdoutMatchesDefaultOutput() throws ParseException {
        for (String format : new String[]{"CSV", "TSV", "JSON"}) {
            String regular = stdoutOf(new String[]{TWOTABLES, "-p", "all", "-f", format});
            String streaming = stdoutOf(new String[]{TWOTABLES, "-p", "all", "-f", format, "--streaming"});
            assertEquals(format + " output must be identical", regular, streaming);
        }
    }

    @Test
    public void testStreamingMultiplePagesAndAreasKeepOrder() throws ParseException, IOException {
        File regular = folder.newFile("areas.csv");
        File streaming = folder.newFile("areas-streaming.csv");

        run(new String[]{SPANNING_CELLS, "-p", "1", "-a", "%0,0,100,50", "-a", "%0,50,100,100",
                "-f", "CSV", "-o", regular.getAbsolutePath()});
        run(new String[]{SPANNING_CELLS, "-p", "1", "-a", "%0,0,100,50", "-a", "%0,50,100,100",
                "-f", "CSV", "-o", streaming.getAbsolutePath(), "--streaming"});

        assertArrayEquals(Files.readAllBytes(regular.toPath()), Files.readAllBytes(streaming.toPath()));
    }

    @Test
    public void testStreamingJSONEmptyResultIsEmptyArray() throws ParseException, IOException {
        File output = folder.newFile("empty.json");

        run(new String[]{SPANNING_CELLS, "-p", "1", "-a", "0,0,10,10", "-l", "-f", "JSON",
                "-o", output.getAbsolutePath(), "--streaming"});

        assertEquals("[]", new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void testStreamingCSVEmptyResultIsEmptyFile() throws ParseException, IOException {
        File output = folder.newFile("empty.csv");

        run(new String[]{SPANNING_CELLS, "-p", "1", "-a", "0,0,10,10", "-l", "-f", "CSV",
                "-o", output.getAbsolutePath(), "--streaming"});

        assertEquals(0, output.length());
    }

    @Test
    public void testStreamingFailureKeepsExistingOutputAndRemovesTemporaryFile() throws ParseException, IOException {
        File output = folder.newFile("product.csv");
        Files.write(output.toPath(), "PREVIOUS PRODUCT\n".getBytes(StandardCharsets.UTF_8));

        try {
            run(new String[]{TWOTABLES, "-p", "99", "-f", "CSV", "-o", output.getAbsolutePath(), "--streaming"});
            fail("Expected a ParseException");
        } catch (ParseException e) {
            // expected
        }

        assertEquals("PREVIOUS PRODUCT\n", new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8));
        assertNoTemporaryOutputFiles(output.toPath().getParent());
    }

    @Test
    public void testStreamingWrongPasswordKeepsExistingOutputAndRemovesTemporaryFile() throws ParseException, IOException {
        File output = folder.newFile("encrypted.csv");
        Files.write(output.toPath(), "PREVIOUS PRODUCT\n".getBytes(StandardCharsets.UTF_8));

        try {
            run(new String[]{"src/test/resources/technology/tabula/encrypted.pdf", "-s", "wrongpassword",
                    "-p", "1", "-f", "CSV", "-o", output.getAbsolutePath(), "--streaming"});
            fail("Expected a ParseException");
        } catch (ParseException e) {
            // expected
        }

        assertEquals("PREVIOUS PRODUCT\n", new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8));
        assertNoTemporaryOutputFiles(output.toPath().getParent());
    }

    @Test
    public void testStreamingBatchMode() throws ParseException, IOException {
        Path pdfDirectory = folder.newFolder("batch").toPath();
        Files.copy(new File(TWOTABLES).toPath(), pdfDirectory.resolve("one.pdf"));
        Files.copy(new File(SPANNING_CELLS).toPath(), pdfDirectory.resolve("two.pdf"));

        run(new String[]{"-b", pdfDirectory.toString(), "-p", "1", "-f", "CSV", "--streaming"});

        Path oneCsv = pdfDirectory.resolve("one.csv");
        Path twoCsv = pdfDirectory.resolve("two.csv");
        assertTrue(Files.exists(oneCsv));
        assertTrue(Files.exists(twoCsv));

        String expected = stdoutOf(new String[]{TWOTABLES, "-p", "1", "-f", "CSV"});
        assertEquals(expected, new String(Files.readAllBytes(oneCsv), StandardCharsets.UTF_8));
        assertNoTemporaryOutputFiles(pdfDirectory);
    }

    private static void assertNoTemporaryOutputFiles(Path directory) {
        if (directory == null) {
            return;
        }
        File[] entries = directory.toFile().listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            String name = entry.getName();
            assertFalse("Temporary file was not cleaned up: " + name, name.endsWith(".tmp"));
        }
    }

}

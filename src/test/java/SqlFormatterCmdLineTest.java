import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqlFormatterCmdLineTest {

	Path testResources = Paths.get("src/test/resources/");

	@TempDir
	Path tempDir;

	private final PrintStream standardOut = System.out;
	private final PrintStream standardErr = System.err;
	private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		System.setOut(new PrintStream(outputStreamCaptor));
		System.setErr(new PrintStream(outputStreamCaptor));
	}

	@AfterEach
	void tearDown() throws Exception {
		System.setOut(standardOut);
		System.setErr(standardErr);
	}

	@Test
	void testCmdArgumentsNoArguments() throws IOException, ParseException {
		SqlFormatter.main(new String[] {});

		assertEquals("A SQL file needs to be provided", outputStreamCaptor.toString().trim());
	}

	@Test
	void testCmdArgumentsFileNoArgument() throws IOException, ParseException {
		SqlFormatter.main(new String[] { "-file" });

		assertEquals("Missing argument for option: file", outputStreamCaptor.toString().trim());
	}

	@Test
	void testCmdArgumentsInvalidFile() throws IOException, ParseException {
		SqlFormatter.main(new String[] { "-file", "asdasd" });

		assertEquals("The provided file 'asdasd' does not exist", outputStreamCaptor.toString().trim());
	}

	@Test
	void testCmdArgumentsValidFile() throws IOException, ParseException {
		String testFile = "single_table.sql";
		// copy test SQL file to temporary directory
		Files.copy(testResources.resolve(testFile), tempDir.resolve(testFile));

		SqlFormatter.main(new String[] { "-file", tempDir.resolve(testFile).toString() });

		assertTrue(outputStreamCaptor.toString().trim().contains("Formatted file:"));
	}

	@Test
	void testCmdArgumentsValidFileSplitNoArgument() throws IOException, ParseException {
		SqlFormatter.main(new String[] { "-file", "src/test/resources/single_table.sql", "-split" });

		assertEquals("Missing argument for option: split", outputStreamCaptor.toString().trim());
	}

	@Test
	void testCmdArgumentsValidFileInvalidSplit() throws IOException, ParseException {
		SqlFormatter.main(new String[] { "-file", "src/test/resources/single_table.sql", "-split", "asd" });

		assertEquals("The value provided for the -split parameter needs to be an integer",
				outputStreamCaptor.toString().trim());
	}

	@Test
	void testCmdArgumentsValidFileValidSplit() throws IOException, ParseException {
		String testFile = "single_table.sql";
		// copy test SQL file to temporary directory
		Files.copy(testResources.resolve(testFile), tempDir.resolve(testFile));

		SqlFormatter.main(new String[] { "-file", tempDir.resolve(testFile).toString(), "-split", "10" });

		assertTrue(outputStreamCaptor.toString().trim().contains("Formatted file:"));
	}

}

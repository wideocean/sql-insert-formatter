import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqlFormatterTest {

	Path testResources = Paths.get("src/test/resources/");

	@TempDir
	Path tempDir;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	private void checkFormattedEqualsExpected(String fileName) throws IOException {
		String testFile = fileName + ".sql";
		String expectedFile = fileName + "_preformatted.sql";

		// copy test SQL file to temporary directory
		Files.copy(testResources.resolve(testFile), tempDir.resolve(testFile));

		String formattedFile = SqlFormatter.format(tempDir.resolve(testFile).toString());

		assertEquals(Files.readAllLines(testResources.resolve(expectedFile)),
				Files.readAllLines(Paths.get(formattedFile)));
	}

	@Test
	void testSingleTable() throws IOException {
		checkFormattedEqualsExpected("single_table");
	}

	@Test
	void testSingleTableMultiple() throws IOException {
		checkFormattedEqualsExpected("single_table_multiple");
	}

}

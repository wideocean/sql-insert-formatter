import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqlFormatterTest {

	Path testResources = Paths.get("src/test/resources/");

	@TempDir
	Path tempDir;

	private void checkFormattedEqualsExpected(String fileName) throws IOException {
		checkFormattedEqualsExpected(fileName, 100, false);
	}

	private void checkFormattedEqualsExpected(String fileName, int split, boolean formatWhitespaces)
			throws IOException {
		String testFile = fileName + ".sql";
		String expectedFile = fileName + "_preformatted.sql";

		// copy test SQL file to temporary directory
		Files.copy(testResources.resolve(testFile), tempDir.resolve(testFile));

		String formattedFile = SqlFormatter.format(tempDir.resolve(testFile).toString(), split, formatWhitespaces);

		byte[] f1 = Files.readAllBytes(testResources.resolve(expectedFile));
		byte[] f2 = Files.readAllBytes(Paths.get(formattedFile));

		assertArrayEquals(f1, f2);
	}

	@Test
	void testSingleTable() throws IOException {
		checkFormattedEqualsExpected("single_table");
	}

	@Test
	void testSingleTableMultiple() throws IOException {
		checkFormattedEqualsExpected("single_table_multiple");
	}

	@Test
	void testSingleTableMultipleLines() throws IOException {
		checkFormattedEqualsExpected("single_table_multiple_lines");
	}

	@Test
	void testSingleTableCaseAndWhitespaces() throws IOException {
		checkFormattedEqualsExpected("single_table_case_whitespaces");
	}

	@Test
	void testSingleTableWithSchema() throws IOException {
		checkFormattedEqualsExpected("single_table_schema");
	}

	@Test
	void testMultipleTablesWithNewlineBetweenInserts() throws IOException {
		checkFormattedEqualsExpected("multiple_tables_newlines");
	}

	@Test
	void testSingleTableWithDefaultSplitLimit100() throws IOException {
		checkFormattedEqualsExpected("single_table_split_default");
	}

	@Test
	void testSingleTableWithCustomSplitLimit50() throws IOException {
		checkFormattedEqualsExpected("single_table_split_custom", 50, false);
	}

	@Test
	void testMultipleTablesWithDefaultSplitLimit100() throws IOException {
		checkFormattedEqualsExpected("multiple_tables_split_default");
	}

	@Test
	void testIrrelevantSqlStatements() throws IOException {
		String testFile = "irrelevant_sql_statements.sql";
		Files.copy(testResources.resolve(testFile), tempDir.resolve(testFile));
		assertEquals("", SqlFormatter.format(tempDir.resolve(testFile).toString()));
	}

	@Test
	void testWhitespacesOutsideQuotesFormatted() throws IOException {
		checkFormattedEqualsExpected("whitespaces_outside_quotes", 100, true);
	}

}

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class SqlFormatterRegexTest {

	Pattern pattern = Pattern.compile(SqlFormatter.INSERT_REGEX);

	private void matchesPattern(String s) {
		Matcher matcher = pattern.matcher(s);
		assertTrue(matcher.matches(), "Pattern should match but does not");
	}

	private void matchesNotPattern(String s) {
		Matcher matcher = pattern.matcher(s);
		assertFalse(matcher.matches(), "Pattern does match but should not");
	}

	@Test
	void testRegexSQLKeywordsUppercase() {
		matchesPattern("INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexSQLKeywordsLowercase() {
		matchesPattern("insert into TableA (ID,AGE,NAME) values (1,12,'asd');");
	}

	@Test
	void testRegexSQLKeywordsMixcase() {
		matchesPattern("InSert INto TableA (ID,AGE,NAME) valUES (1,12,'asd');");
	}

	@Test
	void testRegexStatementAtDelimiter() {
		matchesPattern("INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd')@");
	}

	@Test
	void testRegexMultipleWhitespaces() {
		matchesPattern(" INSERT  INTO   TableA    (   ID,AGE , NAME  )   VALUES  ( 1,12 , 'asd')  ; ");
	}

	@Test
	void testRegexMinimalWhitespaces() {
		matchesPattern("INSERT INTO TableA(ID,AGE,NAME)VALUES(1,12,'asd');");
	}

	@Test
	void testRegexValidCharactersInTableAndColumns() {
		matchesPattern("INSERT INTO TableA_23 (ID_asd ,AGE, NAME2) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexSchema() {
		matchesPattern("INSERT INTO SchEmA_23.TableA (ID,AGE,NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexTableWithoutColumns() {
		matchesPattern("INSERT INTO TableA VALUES (1,12,'asd');");
	}

	@Test
	void testRegexTableAndSchemaWithoutColumns() {
		matchesPattern("INSERT INTO SchEmA_23.TableA VALUES (1,12,'asd');");
	}

	@Test
	void testRegexInvalidSchema() {
		matchesNotPattern("INSERT INTO SchEmA_23.Schema2.TableA (ID,AGE,NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexColumnsOtherDelimitersThanComma() {
		matchesNotPattern("INSERT INTO SchEmA_23.Schema2.TableA (ID;AGE;NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexMissingInsertKeyword() {
		matchesNotPattern(" INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexMissingIntoKeyword() {
		matchesNotPattern("INSERT TableA (ID,AGE,NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexMissingValuesKeyword() {
		matchesNotPattern("INSERT INTO TableA (ID,AGE,NAME) (1,12,'asd');");
	}

	@Test
	void testRegexMissingTable() {
		matchesNotPattern("INSERT INTO (ID,AGE,NAME) VALUES (1,12,'asd');");
	}

	@Test
	void testRegexEmptyColumns() {
		matchesNotPattern("INSERT INTO TableA () VALUES (1,12,'asd');");
	}

	@Test
	void testRegexMissingValues() {
		matchesNotPattern("INSERT INTO TableA (ID,AGE,NAME) VALUES ;");
	}

	@Test
	void testRegexEmptyValues() {
		matchesNotPattern("INSERT INTO TableA (ID,AGE,NAME) VALUES ();");
	}

	@Test
	void testRegexMissingDelimiter() {
		matchesNotPattern("INSERT INTO TableA (ID,AGE,NAME) VALUES (1,12,'asd')");
	}

	@Test
	void testRegexRemoveWhitespacesOutsideQuotes() {
		String s = "(   1  ,    2   ,   '  a s  d'  ) ";
		String s2 = "(1,2,'  a s  d')";
		assertEquals(s2, s.replaceAll(SqlFormatter.WHITESPACE_REGEX, ""));
	}

}

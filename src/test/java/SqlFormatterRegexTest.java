import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class SqlFormatterRegexTest {

	Pattern pattern = Pattern.compile(SqlFormatter.regex);

	private void matchesPattern(String s) {
		Matcher matcher = pattern.matcher(s);
		assertTrue("Pattern should match but does not", matcher.matches());
	}

	private void matchesNotPattern(String s) {
		Matcher matcher = pattern.matcher(s);
		assertFalse("Pattern does match but should not", matcher.matches());
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
	void testRegexMissingColumns() {
		matchesNotPattern("INSERT INTO TableA VALUES (1,12,'asd');");
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

}

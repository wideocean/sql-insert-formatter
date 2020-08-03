import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlFormatter {

	/**
	 * Pattern for one INSERT statement. For further testing (without double
	 * backslashes):
	 * (\s*(?i)INSERT\s+INTO\s+)([a-zA-Z0-9_]+\.{0,1}[a-zA-Z0-9_]+\s*\([a-zA-Z0-9_,\s]+\))\s*(?i)VALUES\s*(\(.+\))\s*[;@]\s*
	 */
	public static String regex = "(\\s*(?i)INSERT\\s+INTO\\s+)([a-zA-Z0-9_]+\\.{0,1}[a-zA-Z0-9_]+\\s*\\([a-zA-Z0-9_,\\s]+\\))\\s*(?i)VALUES\\s*(\\(.+\\))\\s*[;@]\\s*";

	public static void main(String[] args) throws IOException {

		Path newFile = Paths.get("new.sql");
		System.out.println(newFile);

		Path path = Paths.get("test.sql");

		Stream<String> lines = Files.lines(path);
		List<String> collect = lines.filter(line -> !line.isEmpty()).collect(Collectors.toList());
		lines.close();

		Pattern pattern = Pattern.compile(regex);

		List<String> finalList = new ArrayList<String>();

		String s = "";
		for (String line : collect) {
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				finalList.add(line);
			} else {
				s = s + " " + line;
				matcher = pattern.matcher(s);
				if (matcher.matches()) {
					finalList.add(s);
					s = "";
				}
			}
		}

		List<TableEntry> tableList = new ArrayList<TableEntry>();

		String table = "";
		List<String> values = new ArrayList<String>();
		for (String line : finalList) {
			Matcher matcher = pattern.matcher(line);
			System.out.println(line);
			if (matcher.find()) {
				System.out.println("group 2: " + matcher.group(2));
				System.out.println("group 3: " + matcher.group(3));

				String currentTable = matcher.group(2).toUpperCase();
				String currentValues = matcher.group(3);

				// TODO optional formatting
				// currentTable = currentTable.replaceAll("\\s", "");

				// regex for removing whitespaces outside quotes
				// currentValues =
				// currentVales.replaceAll("\s+(?=(?:[^\'"]*[\'"][^\'"]*[\'"])*[^\'"]*$)", "");

				if (table.equals(currentTable)) {
					values.add(currentValues);
				} else {
					table = currentTable;
					values = new ArrayList<String>();
					values.add(currentValues);
					tableList.add(new TableEntry(table, values));
				}

			} else
				// should not happen since finalList is already processed and should contain
				// only valid entries
				throw new Error();
		}

		System.out.println("Result");
		for (TableEntry entry : tableList) {
			System.out.println(entry.getTable());
			entry.getValues().forEach(x -> System.out.println(x));
		}

		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.forName("UTF-8"))) {
			for (TableEntry entry : tableList) {
				writer.write("INSERT INTO " + entry.getTable() + " VALUES ");
				writer.newLine();
				String joinedValues = entry.getValues().stream()
						.collect(Collectors.joining("," + System.lineSeparator(), "", ";"));
				writer.write(joinedValues);
				writer.newLine();

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}

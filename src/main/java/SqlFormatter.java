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
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SqlFormatter {

	/**
	 * Pattern for one INSERT statement. For further testing (without double
	 * backslashes):
	 * (\s*(?i)INSERT\s+INTO\s+)([a-zA-Z0-9_]+\.{0,1}[a-zA-Z0-9_]+\s*\([a-zA-Z0-9_,\s]+\))\s*(?i)VALUES\s*(\(.+\))\s*[;@]\s*
	 */
	public static String regex = "(\\s*(?i)INSERT\\s+INTO\\s+)([a-zA-Z0-9_]+\\.{0,1}[a-zA-Z0-9_]+\\s*)(\\([a-zA-Z0-9_,\\s]+\\))\\s*(?i)VALUES\\s*(\\(.+\\))\\s*[;@]\\s*";

	public static <T> Stream<List<T>> batches(List<T> source, int length) {
		if (length <= 0)
			throw new IllegalArgumentException("length = " + length);
		int size = source.size();
		if (size <= 0)
			return Stream.empty();
		int fullChunks = (size - 1) / length;
		return IntStream.range(0, fullChunks + 1)
				.mapToObj(n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
	}

	public static String format(String inputFilePath) throws IOException {
		return format(inputFilePath, 100);
	}

	public static String format(String inputFilePath, int amountValues) throws IOException {
		Path inputPath = Paths.get(inputFilePath);
		System.out.println(inputPath.toAbsolutePath());

		Stream<String> lines = Files.lines(inputPath);
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
		String columns = "";
		List<String> values = new ArrayList<String>();
		for (String line : finalList) {
			Matcher matcher = pattern.matcher(line);
			System.out.println(line);
			if (matcher.find()) {
				System.out.println("group 2: " + matcher.group(2));
				System.out.println("group 3: " + matcher.group(3));
				System.out.println("group 4: " + matcher.group(4));

				String currentTable = matcher.group(2).toUpperCase().trim();
				String currentColumns = matcher.group(3).replaceAll("\\s+", "");
				String currentValues = matcher.group(4);

				// TODO optional
				// regex for removing whitespaces outside quotes
				// currentValues =
				// currentVales.replaceAll("\s+(?=(?:[^\'"]*[\'"][^\'"]*[\'"])*[^\'"]*$)", "");

				if (table.equals(currentTable) && columns.equals(currentColumns)) {
					values.add(currentValues);
				} else {
					table = currentTable;
					columns = currentColumns;
					values = new ArrayList<String>();
					values.add(currentValues);
					tableList.add(new TableEntry(table, columns, values));
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

		Path newFile = Paths.get(inputFilePath.substring(0, inputFilePath.lastIndexOf(".")) + "_formatted.sql");
		System.out.println(newFile);
		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.forName("UTF-8"))) {
			boolean firstInsert = true;
			for (TableEntry entry : tableList) {
				if (!firstInsert) {
					writer.newLine();
				}

				List<String> valuesList = entry.getValues();
				batches(valuesList, amountValues).forEach(batch -> {
					try {
						writer.write("INSERT INTO " + entry.getTable() + " " + entry.getColumns() + " VALUES ");
						writer.newLine();
						String joinedValues = batch.stream()
								.collect(Collectors.joining("," + System.lineSeparator(), "", ";"));
						writer.write(joinedValues);
						writer.newLine();
					} catch (IOException e) {
						e.printStackTrace();
					}

				});
				firstInsert = false;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return newFile.toAbsolutePath().toString();
	}

	public static void main(String[] args) throws IOException {
		String formattedFile = format("test.sql");
		System.out.println(formattedFile);

	}

}

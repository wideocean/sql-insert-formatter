import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class SqlFormatter {

	/**
	 * Pattern for one INSERT statement. For further testing (without double
	 * backslashes):
	 * (\s*(?i)INSERT\s+INTO\s+)([a-zA-Z0-9_]+\.{0,1}[a-zA-Z0-9_]+\s*\([a-zA-Z0-9_,\s]+\))\s*(?i)VALUES\s*(\(.+\))\s*[;@]\s*
	 */
	public static String regex = "(\\s*(?i)INSERT\\s+INTO\\s+)([a-zA-Z0-9_]+\\.{0,1}[a-zA-Z0-9_]+\\s*)(\\([a-zA-Z0-9_,\\s]+\\))\\s*(?i)VALUES\\s*(\\(.+\\))\\s*[;@]\\s*";

	/**
	 * Returns a stream of List where the elements are partitioned chunks of the
	 * given source list and have the given length.
	 * 
	 * @param <T>
	 * @param source - source list which should be partitioned
	 * @param length - the length of each chunk element
	 * @return
	 */
	private static <T> Stream<List<T>> batches(List<T> source, int length) {
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
//		System.out.println(inputPath.toAbsolutePath());

		List<String> collect = new ArrayList<String>();
		try {
			Stream<String> lines = Files.lines(inputPath);
			collect = lines.filter(line -> !line.isEmpty()).collect(Collectors.toList());
			lines.close();
		} catch (NoSuchFileException e) {
			System.err.println("The provided file '" + inputFilePath + "' does not exist");
			return "";
		}

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
			if (matcher.find()) {
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

		if (tableList.isEmpty())
			return "Nothing to be formatted";

		Path newFile = Paths.get(inputFilePath.substring(0, inputFilePath.lastIndexOf(".")) + "_formatted.sql");
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
		}

		return newFile.toAbsolutePath().toString();
	}

	public static void main(String[] args) throws IOException {
		String formattedFile = "";

		// Debug purpose, test SQL file needs to be in project root directory
//		formattedFile = format("test.sql");
//		System.out.println(formattedFile);

		// create Options object
		Options options = new Options();

		// add a option
		options.addOption("file", true, "the SQL file to be formatted");
		options.addOption("split", true, "the amount of values for one insert statement");

		// Create a parser
		CommandLineParser parser = new DefaultParser();

		// parse the options passed as command line arguments
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return;
		}

		if (cmd.hasOption("file")) {
			String file = cmd.getOptionValue("file");
			if (cmd.hasOption("split")) {
				String split = cmd.getOptionValue("split");
				try {
					int splitValue = Integer.valueOf(split);
					formattedFile = format(file, splitValue);
				} catch (NumberFormatException e) {
					System.err.println("The value provided for the -split parameter needs to be an integer");
					return;
				}
			} else {
				formattedFile = format(file);
			}

			if (!formattedFile.isEmpty())
				System.out.println("Formatted file: " + formattedFile);

		} else {
			System.err.println("A SQL file needs to be provided");
		}

	}

}

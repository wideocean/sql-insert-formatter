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
	 * (\s*(?i)INSERT\s+INTO\s+)([a-zA-Z0-9_]+\.{0,1}[a-zA-Z0-9_]+\s*)(\([a-zA-Z0-9_,\s]+\)){0,1}\s*(?i)VALUES\s*(\(.+\))\s*[;@]\s*
	 */
	public static final String INSERT_REGEX = "(\\s*(?i)INSERT\\s+INTO\\s+)([a-zA-Z0-9_]+\\.{0,1}[a-zA-Z0-9_]+\\s*)(\\([a-zA-Z0-9_,\\s]+\\)){0,1}\\s*(?i)VALUES\\s*(\\(.+\\))\\s*[;@]\\s*";

	/**
	 * Pattern for removing whitespaces outside quotes
	 * \s+(?=(?:[^\'"]*[\'"][^\'"]*[\'"])*[^\'"]*$)
	 */
	public static final String WHITESPACE_REGEX = "\\s+(?=(?:[^\'\"]*[\'\"][^\'\"]*[\'\"])*[^\'\"]*$)";

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

	/**
	 * Formats the given SQL file specified by inputFilePath, splitting the insert
	 * statements each containing 100 inserts. The formatted SQL file contains the
	 * suffix "_formatted" in the filename and will be at the same location as the
	 * input SQL file.
	 * 
	 * @param inputFilePath - the path to the SQL file
	 * @return
	 * @throws IOException
	 */
	public static String format(String inputFilePath) throws IOException {
		return format(inputFilePath, 100, false);
	}

	/**
	 * Formats the given SQL file specified by inputFilePath, splitting the insert
	 * statements each containing the amount specified by amountValues. Optionally,
	 * redundant whitespaces outside quotes inside the VALUES (...) clause can be
	 * removed. The formatted SQL file contains the suffix "_formatted" in the
	 * filename and will be at the same location as the input SQL file.
	 * 
	 * @param inputFilePath     - the path to the SQL file
	 * @param amountValues      - the amount each insert statement should insert at
	 *                          once
	 * @param formatWhitespaces - whether whitespaces outside quotes inside VALUES
	 *                          (...) clause should be removed
	 * @return Returns the path to the formatted SQL file; in case of any errors
	 *         during formatting or in case of no formatting, an empty string will
	 *         be returned
	 * @throws IOException
	 */
	public static String format(String inputFilePath, int amountValues, boolean formatWhitespaces) throws IOException {
		Path inputPath = Paths.get(inputFilePath);

		List<String> collect = new ArrayList<String>();
		try {
			Stream<String> lines = Files.lines(inputPath);
			collect = lines.filter(line -> !line.isEmpty()).collect(Collectors.toList());
			lines.close();
		} catch (NoSuchFileException e) {
			System.err.println("The provided file '" + inputFilePath + "' does not exist");
			return "";
		}

		Pattern pattern = Pattern.compile(INSERT_REGEX);

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
				String currentColumns = "";
				if (matcher.group(3) != null)
					currentColumns = matcher.group(3).replaceAll("\\s+", "");
				String currentValues = matcher.group(4);

				if (formatWhitespaces)
					currentValues = currentValues.replaceAll(WHITESPACE_REGEX, "");

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

		if (tableList.isEmpty()) {
			System.out.println("Nothing to be formatted");
			return "";
		}

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
						String insertString = Stream.of("INSERT INTO", entry.getTable(), entry.getColumns(), "VALUES")//
								.filter(s2 -> s2 != null && !s2.isEmpty())//
								.collect(Collectors.joining(" ", "", " "));
						writer.write(insertString);

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
//		formattedFile = format("test.sql", 100, true);
//		System.out.println(formattedFile);

		// create Options object
		Options options = new Options();

		// add a option
		options.addOption("file", true, "the SQL file to be formatted");
		options.addOption("split", true, "the amount of values for one insert statement");
		options.addOption("formatspaces", false, "remove whitespaces outside quotes in VALUES (...)");

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
			int splitValue = 100;
			boolean formatWhitespaces = false;
			if (cmd.hasOption("split")) {
				String split = cmd.getOptionValue("split");
				try {
					splitValue = Integer.valueOf(split);
				} catch (NumberFormatException e) {
					System.err.println("The value provided for the -split parameter needs to be an integer");
					return;
				}
			}
			if (cmd.hasOption("formatspaces")) {
				formatWhitespaces = true;
			}

			formattedFile = format(file, splitValue, formatWhitespaces);

			if (!formattedFile.isEmpty())
				System.out.println("Formatted file: " + formattedFile);

		} else {
			System.err.println("A SQL file needs to be provided using the -file option");
		}

	}

}

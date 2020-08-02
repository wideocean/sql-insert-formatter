import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class Main {

	public static void main(String[] args) throws IOException {

		File file = new File("aaa.txt");
		System.out.println(file.getAbsolutePath());

		Path path = Paths.get("test.sql");

		Stream<String> lines = Files.lines(path);
		List<String> collect = lines.filter(line -> !line.isEmpty()).collect(Collectors.toList());
		lines.close();

		// Pattern for one INSERT statement
		// for further testing, full regex:
		// (\s*(?i)INSERT\s+INTO\s+)([a-zA-Z0-9_]+\.{0,1}[a-zA-Z0-9_]+\s*\([a-zA-Z0-9_,\s]+\))\s*(?i)VALUES\s*(\(.+\))\s*[;@]
		Pattern pattern = Pattern.compile(
				"(\\s*(?i)INSERT\\s+INTO\\s+)([a-zA-Z0-9_]+\\.{0,1}[a-zA-Z0-9_]+\\s*\\([a-zA-Z0-9_,\\s]+\\))\\s*(?i)VALUES\\s*(\\(.+\\))\\s*[;@]");

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

		Multimap<String, String> map = ArrayListMultimap.create();

		for (String line : finalList) {
			Matcher matcher = pattern.matcher(line);

			System.out.println(line);
			if (matcher.find()) {
				System.out.println("Found value: " + matcher.group(0));
				System.out.println("Found value: " + matcher.group(1));
				System.out.println("Found value: " + matcher.group(2));
				System.out.println("Found value: " + matcher.group(3));

				String tableWithColumns = matcher.group(2).replaceAll("\\s", "").toUpperCase();
				System.out.println(tableWithColumns);
				String values = matcher.group(3);
				map.put(tableWithColumns, values);
			} else
				// should not happen since finalList is already processed and should contain
				// only valid entries
				throw new Error();
		}

		System.out.println("Result");
		for (String key : map.keySet()) {
			System.out.println(key);
			System.out.println(map.get(key));
		}

	}

}

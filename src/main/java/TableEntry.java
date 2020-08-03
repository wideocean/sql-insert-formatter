import java.util.List;

public class TableEntry {
	private String table;
	private List<String> values;

	public TableEntry(String table, List<String> values) {
		this.table = table;
		this.values = values;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}

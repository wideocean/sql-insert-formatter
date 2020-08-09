import java.util.List;

public class TableEntry {
	private String table;
	private String columns;
	private List<String> values;

	public TableEntry(String table, String columns, List<String> values) {
		this.table = table;
		this.columns = columns;
		this.values = values;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

}

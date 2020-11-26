package gr.athenarc.imsi.visualfacts;

import com.univocity.parsers.csv.CsvParserSettings;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Schema {
    private final String csv;
    private final int xColumn;
    private final int yColumn;
    private final Rectangle bounds;
    private final Map<Integer, CategoricalColumn> categoricalColumns = new HashMap();
    private Character delimiter = ',';
    private int objectCount;

    public Schema(String csv, Character delimiter, int xColumn, int yColumn, Rectangle bounds, int objectCount) {
        this.csv = csv;
        this.delimiter = delimiter;
        this.xColumn = xColumn;
        this.yColumn = yColumn;
        this.bounds = bounds;
        this.objectCount = objectCount;
    }

    public String getCsv() {
        return csv;
    }

    public Short getColValue(String[] parsedRow, int col) {
        String rawValue = parsedRow[col];
        return categoricalColumns.get(col).getValueKey(rawValue);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getxColumn() {
        return xColumn;
    }

    public int getyColumn() {
        return yColumn;
    }

    public List<CategoricalColumn> getCategoricalColumns() {
        return categoricalColumns.values().stream().sorted(Comparator.comparingInt(CategoricalColumn::getIndex)).collect(Collectors.toList());
    }

    public void setCategoricalColumns(List<CategoricalColumn> categoricalCols) {
        for (CategoricalColumn categoricalColumn : categoricalCols) {
            categoricalColumns.put(categoricalColumn.getIndex(), categoricalColumn);
        }
    }

    public CategoricalColumn getCategoricalColumn(int colIndex) {
        return categoricalColumns.get(colIndex);
    }

    public int getObjectCount() {
        return objectCount;
    }

    public CsvParserSettings createCsvParserSettings() {
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.getFormat().setDelimiter(delimiter);
        parserSettings.setIgnoreLeadingWhitespaces(false);
        parserSettings.setIgnoreTrailingWhitespaces(false);
        return parserSettings;
    }
}

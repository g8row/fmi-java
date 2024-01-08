package bg.sofia.uni.fmi.mjt.csvprocessor.table;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.column.BaseColumn;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.column.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BaseTable implements Table {
    List<String> columnNames;
    List<Column> columns;

    public BaseTable() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public BaseTable(List<String> columnNames, List<Column> columns) {
        this.columnNames = columnNames;
        this.columns = columns;
    }

    /**
     * Adds data to the table. If the table doesn't have any columns,
     * the data parameter is interpreted as the table headers.
     * If the table contains columns, then the data parameter is interpreted as a row of data.
     *
     * @param data - the data to be added
     * @throws CsvDataNotCorrectException - if the data is in incorrect format - if the count of
     *                                    the provided data parts is less or more than the number of
     *                                    columns in the table
     * @throws IllegalArgumentException   if data is null
     */
    @Override
    public void addData(String[] data) throws CsvDataNotCorrectException {
        if (data == null) {
            throw new IllegalArgumentException("data is null");
        }
        if (columnNames.isEmpty()) {
            columnNames.addAll(Arrays.asList(data));
            for (int i = 0; i < columnNames.size(); i++) {
                columns.add(new BaseColumn());
            }
        } else {
            if (data.length != columnNames.size()) {
                throw new CsvDataNotCorrectException("the count of" +
                        "the provided data parts is less or more than the number of columns in the table");
            }
            for (int i = 0; i < data.length; i++) {
                columns.get(i).addData(data[i]);
            }
        }

    }

    /**
     * Returns unmodifiable collection of the names of all columns
     */
    @Override
    public Collection<String> getColumnNames() {
        return List.copyOf(columnNames);
    }

    /**
     * Returns unmodifiable collection of all strings in a specific column
     *
     * @param column - the column for which the data will be returned
     * @throws IllegalArgumentException is column is null or blank or there is no corresponding column with that name
     *                                  in the table
     */
    @Override
    public Collection<String> getColumnData(String column) {
        if (column == null || column.isBlank() || !columnNames.contains(column)) {
            throw new IllegalArgumentException("column is null or blank or there is " +
                    "no corresponding column with that name in the table");
        }
        return columns.get(columnNames.indexOf(column)).getData();
    }

    /**
     * Returns the total count of all rows.
     */
    @Override
    public int getRowsCount() {
        if (columnNames.isEmpty()) {
            return 0;
        }
        if (columns.isEmpty()) {
            return 1;
        }
        return columns.getFirst().getData().size() + 1;
    }
}

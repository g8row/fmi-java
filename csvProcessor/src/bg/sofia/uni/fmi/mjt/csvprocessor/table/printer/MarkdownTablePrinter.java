package bg.sofia.uni.fmi.mjt.csvprocessor.table.printer;

import bg.sofia.uni.fmi.mjt.csvprocessor.table.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MarkdownTablePrinter implements TablePrinter {
    final int minLength = 3;

    List<Integer> getColumnWidths(Table table) {
        List<Integer> integers = new ArrayList<>(table.getColumnNames().size());
        for (String str : table.getColumnNames()) {
            int currentLongestWordLength = minLength;
            if (str.length() > currentLongestWordLength) {
                currentLongestWordLength = str.length();
            }
            for (String entry : table.getColumnData(str)) {
                if (entry.length() > currentLongestWordLength) {
                    currentLongestWordLength = entry.length();
                }
            }
            integers.add(currentLongestWordLength);
        }
        return integers;
    }

    String createRow(Collection<String> values, List<Integer> columnWidths) {
        StringBuilder builder = new StringBuilder();
        int currColumn = 0;
        for (String value : values) {
            builder.append("| ");
            builder.append(value);
            builder.append(" ".repeat(Math.max(0, columnWidths.get(currColumn) - value.length() + 1)));
            currColumn++;
        }
        builder.append("|");
        return builder.toString();
    }

    String createRow(ColumnAlignment[] columnAlignments, List<Integer> columnWidths) {
        StringBuilder builder = new StringBuilder();
        int currColumn = 0;
        for (ColumnAlignment value : columnAlignments) {
            builder.append("| ");
            if (value == ColumnAlignment.LEFT || value == ColumnAlignment.CENTER) {
                builder.append(':');
            }
            builder.append("-".repeat(Math.max(0, columnWidths.get(currColumn) - value.getAlignmentCharactersCount())));
            if (value == ColumnAlignment.RIGHT || value == ColumnAlignment.CENTER) {
                builder.append(':');
            }
            builder.append(' ');
            currColumn++;
        }
        for (int i = 0; i < columnWidths.size() - columnAlignments.length; i++) {
            builder.append("| ");
            builder.append("-".repeat(Math.max(0, columnWidths.get(currColumn))));
            builder.append(' ');
            currColumn++;
        }
        builder.append("|");
        return builder.toString();
    }

    List<List<String>> getRowsWithData(Table table) {
        if (table.getRowsCount() == 0) {
            return new ArrayList<>();
        }
        List<List<String>> dataMatrix = new ArrayList<>(table.getRowsCount() - 1);
        for (int i = 0; i < table.getRowsCount() - 1; i++) {
            dataMatrix.add(new ArrayList<>(table.getColumnNames().size()));
        }
        for (String column : table.getColumnNames()) {
            int index = 0;
            for (String value : table.getColumnData(column)) {
                dataMatrix.get(index).add(value);
                index++;
            }
        }
        return dataMatrix;
    }

    /**
     * Returns unmodifiable collection of strings, each one representing a formatted single row that needs to be printed
     *
     * @param table      - the table to be printed
     * @param alignments - the applied alignments for columns; if the number of given alignments is
     *                   smaller than the number of columns, the remaining columns have NOALIGNMENT. If it's more,
     *                   ignore the extra ones.
     */
    @Override
    public Collection<String> printTable(Table table, ColumnAlignment... alignments) {
        if (table == null) {
            throw new IllegalArgumentException();
        }
        List<String> toReturn = new ArrayList<>();
        List<Integer> columnWidths = getColumnWidths(table);
        toReturn.add(createRow(table.getColumnNames(), columnWidths));
        if (alignments.length > table.getColumnNames().size()) {
            alignments = Arrays.copyOfRange(alignments, 0, table.getColumnNames().size());
        }
        toReturn.add(createRow(alignments, columnWidths));
        for (List<String> row : getRowsWithData(table)) {
            toReturn.add(createRow(row, columnWidths));
        }
        return toReturn;
    }
}

package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.BaseTable;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.Table;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.MarkdownTablePrinter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CsvProcessor implements CsvProcessorAPI {
    private final Table table;

    public CsvProcessor() {
        this(new BaseTable());
    }

    public CsvProcessor(Table table) {
        this.table = table;
    }

    /**
     * Reads a CSV data from Reader
     *
     * @param reader    the Reader from which the CSV will be read
     * @param delimiter the delimiter used to split the CSV (such as ,.- and so on)
     * @throws CsvDataNotCorrectException if the CSV data is in wrong format
     */
    @Override
    public void readCsv(Reader reader, String delimiter) throws CsvDataNotCorrectException {
        StringBuilder csv = new StringBuilder();
        int charRead;
        while (true) {
            try {
                if ((charRead = reader.read()) < 0) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            csv.append((char) charRead);
        }
        String[] rows = csv.toString().split(System.lineSeparator());
        String[] values = rows[0].split("\\Q" + delimiter + "\\E");
        Set<String> set = new HashSet<>();
        for (String value :values) {
            if (!set.add(value)) {
                throw new CsvDataNotCorrectException("the CSV data is in wrong format");
            }
        }

        for (String row : rows) {
            table.addData(row.split("\\Q" + delimiter + "\\E"));
        }
    }

    /**
     * Writes the content of the table to the provided Writer
     *
     * @param writer     - the Writer to which the table will be written
     * @param alignments
     */
    @Override
    public void writeTable(Writer writer, ColumnAlignment... alignments) {
        MarkdownTablePrinter printer = new MarkdownTablePrinter();
        Collection<String> rows = printer.printTable(table, alignments);
        int index = 0;
        for (String row : printer.printTable(table, alignments)) {
            try {
                writer.write(row);
                writer.flush();
                if (index != rows.size() - 1) {
                    writer.write(System.lineSeparator());
                }
                writer.flush();
                index++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

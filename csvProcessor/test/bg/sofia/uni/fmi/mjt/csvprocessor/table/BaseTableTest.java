package bg.sofia.uni.fmi.mjt.csvprocessor.table;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.MarkdownTablePrinter;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.TablePrinter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BaseTableTest {

    TablePrinter printer = new MarkdownTablePrinter();

    @Test
    void testAlignGivenExample() throws CsvDataNotCorrectException {
        Table table = new BaseTable();
        table.addData(new String[] {"hdr", "testheader", "z"});
        table.addData(new String[] {"testcolumn", "b", "c"});

        Collection<String> print = printer.printTable(table, ColumnAlignment.RIGHT, ColumnAlignment.LEFT, ColumnAlignment.CENTER);
        Iterator<String> it = print.iterator();
        it.next();
        it.next();
        assertEquals("| testcolumn | b          | c   |", it.next());
    }

    @Test
    void testPrintLeftAlign() throws CsvDataNotCorrectException {
        Table table = new BaseTable();
        table.addData(new String[]{"column1", "column2", "column333"});
        table.addData(new String[]{"row11", "row1111", "roww11111111"});
        table.addData(new String[]{"row2222", "row222", "row22"});

        Iterator<String> it = printer.printTable(table,
                ColumnAlignment.LEFT, ColumnAlignment.LEFT, ColumnAlignment.LEFT).iterator();
        it.next();
        it.next();
        assertEquals("| row11   | row1111 | roww11111111 |", it.next());
    }

    @Test
    void testPrintNoRows() throws CsvDataNotCorrectException {
        Table table = new BaseTable();
        table.addData(new String[]{"column1", "column2", "column333"});

        Iterator<String> it = printer.printTable(table,
                ColumnAlignment.LEFT, ColumnAlignment.LEFT, ColumnAlignment.LEFT).iterator();

        assertEquals("| column1 | column2 | column333 |", it.next());
    }

    @Test
    void testPrintNoFormatting() throws CsvDataNotCorrectException {
        Table table = new BaseTable();
        table.addData(new String[]{"column1", "column2", "column333"});

        Iterator<String> it = printer.printTable(table).iterator();

        assertEquals("| column1 | column2 | column333 |", it.next());
    }

    @Test
    void testGetRowsCountWithNoRows() throws CsvDataNotCorrectException {
        Table table = new BaseTable();

        assertEquals(table.getRowsCount(), 0);
    }

    @Test
    void testGetColumnByName() throws CsvDataNotCorrectException {
        Table table = new BaseTable();
        table.addData(new String[]{"column1", "column2", "column333"});
        table.addData(new String[]{"row11", "row1111", "roww11111111"});
        table.addData(new String[]{"row2222", "row222", "row22"});

        assertIterableEquals(table.getColumnData("column1"), List.of("row11", "row2222"));
    }

    @Test
    void testAddDataWithNull() {
        Table table = new BaseTable();
        assertThrows(IllegalArgumentException.class, () -> table.addData(null));
    }

    @Test
    void testAddDataMoreFields() throws CsvDataNotCorrectException {
        Table table = new BaseTable();
        table.addData(new String[]{"column1", "column2", "column333"});

        assertThrows(CsvDataNotCorrectException.class, () -> table.addData(
                new String[]{"row11", "row1111", "roww11111111", "row11111"}
        ));
    }
}

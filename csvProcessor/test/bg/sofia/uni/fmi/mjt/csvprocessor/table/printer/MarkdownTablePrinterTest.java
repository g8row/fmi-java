package bg.sofia.uni.fmi.mjt.csvprocessor.table.printer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MarkdownTablePrinterTest {

    TablePrinter printer = new MarkdownTablePrinter();

    @Test
    void testWithNullTable() {
        assertThrows(IllegalArgumentException.class, () -> printer.printTable(null));
    }
}

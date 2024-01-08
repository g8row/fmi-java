package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.BaseTable;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.Table;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CsvProcessorTest {
    private final Table table = new BaseTable();
    @Test
    public void testIOProcess() throws CsvDataNotCorrectException {
        String csvData = "col1,col2,col3\r\n"
                + "row1,row111,row11\r\n";
        CsvProcessor csvProcessor = new CsvProcessor(table);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));
        csvProcessor.readCsv(new InputStreamReader(inputStream), ",");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        csvProcessor.writeTable(new OutputStreamWriter(outputStream));

        String result = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);

        String expectedCsv = "| col1 | col2   | col3  |" + System.lineSeparator() +
                "| ---- | ------ | ----- |" + System.lineSeparator() +
                "| row1 | row111 | row11 |";

        assertEquals(expectedCsv, result, "Expected output doesnt match");
    }
}
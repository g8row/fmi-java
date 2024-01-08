package bg.sofia.uni.fmi.mjt.csvprocessor;

import bg.sofia.uni.fmi.mjt.csvprocessor.exceptions.CsvDataNotCorrectException;
import bg.sofia.uni.fmi.mjt.csvprocessor.table.printer.ColumnAlignment;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, CsvDataNotCorrectException {
        CsvProcessor csvProcessor = new CsvProcessor();
        csvProcessor.readCsv(new FileReader("C:\\Users\\aliog\\IdeaProjects\\csvProcessor\\src\\bg\\sofia\\uni\\fmi\\mjt\\csvprocessor\\test.csv"), ",");
        csvProcessor.writeTable(
                new FileWriter("C:\\Users\\aliog\\IdeaProjects\\csvProcessor\\src\\bg\\sofia\\uni\\fmi\\mjt\\csvprocessor\\test1.md"),
                ColumnAlignment.CENTER, ColumnAlignment.RIGHT, ColumnAlignment.CENTER, ColumnAlignment.RIGHT, ColumnAlignment.CENTER, ColumnAlignment.RIGHT
                );
    }
}
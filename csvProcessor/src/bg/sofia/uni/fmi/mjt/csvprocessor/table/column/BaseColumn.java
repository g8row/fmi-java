package bg.sofia.uni.fmi.mjt.csvprocessor.table.column;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BaseColumn implements Column {
    Set<String> values;
    public BaseColumn() {
        this(new LinkedHashSet<>());
    }

    public BaseColumn(Set<String> values) {
        this.values = values;
    }

    /**
     * Adds new data string to the column
     *
     * @param data - the string to be added
     * @throws IllegalArgumentException if data is null or blank
     */
    @Override
    public void addData(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("data is null or blank");
        }
        values.add(data);
    }

    /**
     * Returns an unmodifiable collection of all data stored in the column
     */
    @Override
    public Collection<String> getData() {
        return List.copyOf(values);
    }
}

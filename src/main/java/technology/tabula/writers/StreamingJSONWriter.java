package technology.tabula.writers;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;

import technology.tabula.Table;

/**
 * Streams tables as a JSON array, one page at a time. The resulting text is
 * identical to the one produced by {@link JSONWriter#write(Appendable, List)}
 * for the same list of tables, including the {@code []} output for an empty
 * list.
 */
public class StreamingJSONWriter implements StreamingWriter {

    private Gson gson;
    private boolean firstTable;

    @Override
    public void writeStart(Appendable out) throws IOException {
        this.gson = JSONWriter.gson();
        this.firstTable = true;
        out.append('[');
    }

    @Override
    public void writePage(Appendable out, List<Table> tables) throws IOException {
        for (Table table : tables) {
            if (!this.firstTable) {
                out.append(',');
            }
            this.firstTable = false;
            out.append(this.gson.toJson(this.gson.toJsonTree(table, Table.class)));
        }
    }

    @Override
    public void writeEnd(Appendable out) throws IOException {
        out.append(']');
    }

}

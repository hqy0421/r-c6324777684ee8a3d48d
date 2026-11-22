package technology.tabula.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

/**
 * Streams tables to the output as they are produced. The resulting bytes are
 * identical to those produced by {@link CSVWriter#write(Appendable, List)} for
 * the same list of tables.
 */
public class StreamingCSVWriter implements StreamingWriter {

    private final CSVFormat format;
    private CSVPrinter printer;

    public StreamingCSVWriter() {
        this(CSVFormat.EXCEL);
    }

    protected StreamingCSVWriter(CSVFormat format) {
        this.format = format;
    }

    @Override
    public void writeStart(Appendable out) throws IOException {
        this.printer = new CSVPrinter(out, this.format);
    }

    @Override
    public void writePage(Appendable out, List<Table> tables) throws IOException {
        for (Table table : tables) {
            for (List<RectangularTextContainer> row : table.getRows()) {
                List<String> cells = new ArrayList<>(row.size());
                for (RectangularTextContainer<?> cell : row) {
                    cells.add(cell.getText());
                }
                this.printer.printRecord(cells);
            }
        }
        this.printer.flush();
    }

    @Override
    public void writeEnd(Appendable out) throws IOException {
        if (this.printer != null) {
            this.printer.flush();
            this.printer = null;
        }
    }

}

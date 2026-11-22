package technology.tabula.writers;

import org.apache.commons.csv.CSVFormat;

/**
 * Streams tables in TSV format. The resulting bytes are identical to those
 * produced by {@link TSVWriter} for the same list of tables.
 */
public class StreamingTSVWriter extends StreamingCSVWriter {

    public StreamingTSVWriter() {
        super(CSVFormat.TDF);
    }

}

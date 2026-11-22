package technology.tabula.writers;

import java.io.IOException;
import java.util.List;

import technology.tabula.Table;

/**
 * Writes tables incrementally, one page at a time, while producing exactly the
 * same bytes as the corresponding {@link Writer} would produce for the full
 * list of tables.
 */
public interface StreamingWriter {

    /**
     * Starts a new document. Must be called once before any call to
     * {@link #writePage(Appendable, List)}.
     */
    void writeStart(Appendable out) throws IOException;

    /**
     * Writes all tables extracted from a single page, preserving their order.
     * The caller is free to discard the tables once this method returns.
     */
    void writePage(Appendable out, List<Table> tables) throws IOException;

    /**
     * Finishes the document. Must be called once after the last call to
     * {@link #writePage(Appendable, List)}.
     */
    void writeEnd(Appendable out) throws IOException;

}

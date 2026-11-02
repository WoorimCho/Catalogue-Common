package com.example.cataloguecommon;

import java.util.ArrayList;
import java.util.List;

/**
 * A small RFC-4180-ish CSV reader — enough for a controlled import format.
 * Shared by both catalogue services' importers.
 *
 * <p>Supports: comma-separated fields; {@code "..."}-quoted fields that may
 * contain commas; {@code ""} inside a quoted field for a literal quote;
 * trimming of unquoted fields; blank lines skipped.
 *
 * <p>Does <em>not</em> support newlines inside a quoted field — every record is
 * exactly one line.
 */
public final class Csv {

    /** Data rows allowed per file, header excluded (SECURITY.md N4: the 5 MB upload cap alone allows ~1M tiny rows). */
    public static final int MAX_ROWS = 5_000;

    /** Fields allowed on one row. */
    public static final int MAX_FIELDS_PER_ROW = 100;

    private Csv() {
    }

    /**
     * Parse the whole text into rows of fields. The first row is whatever the file's first line is (usually the header).
     *
     * @throws IllegalArgumentException when the file has more than {@link #MAX_ROWS} data rows or a row has more than
     *                                  {@link #MAX_FIELDS_PER_ROW} fields
     */
    public static List<String[]> parse(String text) {
        List<String[]> rows = new ArrayList<>();
        for (String line : text.split("\r\n|\r|\n", -1)) {
            if (line.isBlank()) {
                continue;
            }
            rows.add(parseLine(line));
            if (rows.size() > MAX_ROWS + 1) {
                throw new IllegalArgumentException("the file has more than " + MAX_ROWS + " rows");
            }
        }
        return rows;
    }

    private static void checkFieldCount(int count) {
        if (count > MAX_FIELDS_PER_ROW) {
            throw new IllegalArgumentException("a row has more than " + MAX_FIELDS_PER_ROW + " fields");
        }
    }

    private static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(field.toString().trim());
                checkFieldCount(fields.size());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());
        checkFieldCount(fields.size());
        return fields.toArray(new String[0]);
    }
}

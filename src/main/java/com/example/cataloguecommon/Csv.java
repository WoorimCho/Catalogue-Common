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

    private Csv() {
    }

    /** Parse the whole text into rows of fields. The first row is whatever the file's first line is (usually the header). */
    public static List<String[]> parse(String text) {
        List<String[]> rows = new ArrayList<>();
        for (String line : text.split("\r\n|\r|\n", -1)) {
            if (line.isBlank()) {
                continue;
            }
            rows.add(parseLine(line));
        }
        return rows;
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
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString().trim());
        return fields.toArray(new String[0]);
    }
}

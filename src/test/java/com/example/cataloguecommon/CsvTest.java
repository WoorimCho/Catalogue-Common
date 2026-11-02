package com.example.cataloguecommon;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvTest {

    @Test
    void splitsPlainRowsAndTrims() {
        List<String[]> rows = Csv.parse("name, kcal\n oats , 379 \n");
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("name", "kcal");
        assertThat(rows.get(1)).containsExactly("oats", "379");
    }

    @Test
    void honoursQuotedCommasAndDoubledQuotes() {
        List<String[]> rows = Csv.parse("\"chicken breast, raw\",\"a \"\"good\"\" cut\"");
        assertThat(rows.get(0)).containsExactly("chicken breast, raw", "a \"good\" cut");
    }

    @Test
    void acceptsTheMaximumNumberOfDataRowsPlusAHeader() {
        StringBuilder csv = new StringBuilder("name\n");
        for (int i = 0; i < Csv.MAX_ROWS; i++) {
            csv.append("row").append(i).append('\n');
        }
        assertThat(Csv.parse(csv.toString())).hasSize(Csv.MAX_ROWS + 1);
    }

    @Test
    void rejectsMoreThanTheMaximumNumberOfRows() {
        StringBuilder csv = new StringBuilder("name\n");
        for (int i = 0; i <= Csv.MAX_ROWS; i++) {
            csv.append("row").append(i).append('\n');
        }
        assertThatThrownBy(() -> Csv.parse(csv.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("more than " + Csv.MAX_ROWS + " rows");
    }

    @Test
    void rejectsARowWithTooManyFields() {
        String ok = String.join(",", java.util.Collections.nCopies(Csv.MAX_FIELDS_PER_ROW, "x"));
        assertThat(Csv.parse(ok).get(0)).hasSize(Csv.MAX_FIELDS_PER_ROW);

        String tooMany = ok + ",x";
        assertThatThrownBy(() -> Csv.parse(tooMany))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("more than " + Csv.MAX_FIELDS_PER_ROW + " fields");
    }

    @Test
    void aHugeCommaRunFailsFastInsteadOfBuildingMillionsOfFields() {
        String commas = ",".repeat(2_000_000);
        assertThatThrownBy(() -> Csv.parse(commas)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void skipsBlankLinesAndKeepsEmptyTrailingField() {
        List<String[]> rows = Csv.parse("a,b,\n\n\nc,d,e");
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("a", "b", "");
        assertThat(rows.get(1)).containsExactly("c", "d", "e");
    }
}

package com.example.cataloguecommon;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    void skipsBlankLinesAndKeepsEmptyTrailingField() {
        List<String[]> rows = Csv.parse("a,b,\n\n\nc,d,e");
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("a", "b", "");
        assertThat(rows.get(1)).containsExactly("c", "d", "e");
    }
}

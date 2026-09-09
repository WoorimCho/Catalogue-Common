package com.example.cataloguecommon.tag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagServiceTest {

    @Test
    void normaliseTrimsAndLowercases() {
        assertThat(TagService.normalise("  Nut-Free ")).isEqualTo("nut-free");
        assertThat(TagService.normalise("DIET:Vegan")).isEqualTo("diet:vegan");
        assertThat(TagService.normalise(null)).isNull();
    }

    @Test
    void tagIdentityIsByIdWithStableHash() {
        Tag a = new Tag("diet:vegan");
        Tag b = new Tag("diet:vegan");
        // transient (no id) instances are never equal, but must not blow up in a HashSet
        assertThat(a).isNotEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}

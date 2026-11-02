package com.example.cataloguecommon.tag;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SECURITY.md N4: {@link TagService#resolve} is the one choke point every caller
 * goes through (JSON API, CSV importers, attach-tags), so the tag limits live here.
 */
class TagResolveLimitsTest {

    /** A repository that knows no tags and echoes what it is asked to save. */
    private static TagRepository emptyRepository() {
        return (TagRepository) Proxy.newProxyInstance(TagRepository.class.getClassLoader(),
                new Class<?>[]{TagRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByNameIn" -> List.of();
                    case "saveAll" -> args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static List<String> names(int count) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            out.add("tag-" + i);
        }
        return out;
    }

    private final TagServiceImpl service = new TagServiceImpl(emptyRepository(), null);

    @Test
    void acceptsExactlyTheMaximumNumberOfTags() {
        Set<Tag> resolved = service.resolve(names(TagService.MAX_TAGS_PER_REQUEST));
        assertThat(resolved).hasSize(TagService.MAX_TAGS_PER_REQUEST);
    }

    @Test
    void rejectsOneTagOverTheMaximum() {
        assertThatThrownBy(() -> service.resolve(names(TagService.MAX_TAGS_PER_REQUEST + 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at most " + TagService.MAX_TAGS_PER_REQUEST);
    }

    @Test
    void duplicatesAndBlanksDoNotCountTowardsTheLimit() {
        List<String> noisy = new ArrayList<>(names(TagService.MAX_TAGS_PER_REQUEST));
        noisy.addAll(names(TagService.MAX_TAGS_PER_REQUEST));   // same names again
        noisy.add("   ");
        noisy.add(null);
        noisy.add("TAG-0");                                      // same after normalising
        assertThat(service.resolve(noisy)).hasSize(TagService.MAX_TAGS_PER_REQUEST);
    }

    @Test
    void acceptsANameAtTheMaximumLengthAndRejectsOneLonger() {
        String atLimit = "a".repeat(TagService.MAX_TAG_LENGTH);
        assertThat(service.resolve(List.of(atLimit))).hasSize(1);

        String over = "a".repeat(TagService.MAX_TAG_LENGTH + 1);
        assertThatThrownBy(() -> service.resolve(List.of(over)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at most " + TagService.MAX_TAG_LENGTH);
    }

    @Test
    void lengthIsMeasuredAfterTrimming() {
        String padded = "  " + "a".repeat(TagService.MAX_TAG_LENGTH) + "  ";
        assertThat(service.resolve(List.of(padded))).hasSize(1);
    }
}

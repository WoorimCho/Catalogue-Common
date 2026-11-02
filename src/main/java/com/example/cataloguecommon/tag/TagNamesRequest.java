package com.example.cataloguecommon.tag;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** Body for attaching tags to a catalogue entry: {@code {"tags": ["diet:vegan", "nut-free"]}}. */
public record TagNamesRequest(
        @NotEmpty
        @Size(max = TagService.MAX_TAGS_PER_REQUEST)
        Set<@Size(max = TagService.MAX_TAG_LENGTH) String> tags) {
}

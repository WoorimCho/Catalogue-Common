package com.example.cataloguecommon.tag;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/** Body for attaching tags to a catalogue entry: {@code {"tags": ["diet:vegan", "nut-free"]}}. */
public record TagNamesRequest(@NotEmpty Set<String> tags) {
}

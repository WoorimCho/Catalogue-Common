package com.example.cataloguecommon.tag;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Fold the {@code from} tags into {@code into}: every catalogue entry linked to a
 * {@code from} tag is relinked to {@code into}, then the {@code from} tags are
 * deleted. {@code into} is left untouched if it also appears in {@code from}.
 */
public record MergeRequest(
        @NotEmpty @Size(max = TagService.MAX_TAGS_PER_REQUEST) Set<Long> from,
        @NotNull Long into) {
}

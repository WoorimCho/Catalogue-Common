package com.example.cataloguecommon.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a {@link Tag}. */
public record TagRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 60) String namespace,
        @Size(max = 500) String description) {
}

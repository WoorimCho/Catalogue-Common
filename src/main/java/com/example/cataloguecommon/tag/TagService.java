package com.example.cataloguecommon.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

public interface TagService {

    /**
     * Search tags. {@code nameContains} matches anywhere in the name
     * (case-insensitive substring) — so "vegan" also finds "diet:vegan". When
     * blank, {@code namespace} filters by namespace; when both are blank, every
     * tag is listed. {@code nameContains} wins if both are given.
     */
    Page<TagResponse> list(String nameContains, String namespace, Pageable pageable);

    TagResponse get(long id);

    TagResponse create(TagRequest request);

    TagResponse update(long id, TagRequest request);

    void delete(long id);

    TagResponse merge(MergeRequest request);

    /**
     * Normalise the given names and return managed {@link Tag} entities,
     * creating any that don't exist yet. Call this from within a transaction so
     * the returned entities stay attached.
     */
    Set<Tag> resolve(Collection<String> names);

    /** The single place tag names are canonicalised: trimmed, lower-cased. */
    static String normalise(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }
}

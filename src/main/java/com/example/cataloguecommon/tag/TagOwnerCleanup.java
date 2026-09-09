package com.example.cataloguecommon.tag;

import java.util.Set;

/**
 * Bridges {@link TagServiceImpl} to the owning aggregate — {@code Ingredient} in
 * one service, {@code Recipe} in the other — which stays in that service.
 *
 * <p>There is no DB cascade on the {@code *_tag} join table, so links must be
 * cleared in code before a tag is deleted or merged. Each catalogue service
 * provides a {@code @Component} implementing this against its own repository.
 */
public interface TagOwnerCleanup {

    /**
     * On every owner carrying any of these tag ids, drop those tags and persist.
     * Called before the tags themselves are deleted.
     */
    void detachAll(Set<Long> tagIds);

    /**
     * On every owner carrying any {@code fromIds} tag, drop those and add
     * {@code into}, then persist. Called during a merge, before the {@code from}
     * tags are deleted.
     */
    void retag(Set<Long> fromIds, Tag into);
}

package com.example.cataloguecommon.tag;

public record TagResponse(Long id, String name, String namespace, String description) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getNamespace(), tag.getDescription());
    }
}

package com.example.cataloguecommon.tag;

import com.example.cataloguecommon.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The shared tag lifecycle: search / CRUD / merge / name-resolution. Identical
 * for both catalogue services — the only per-service part (clearing links on the
 * owning entity) is delegated to {@link TagOwnerCleanup}.
 */
@Service
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagOwnerCleanup ownerCleanup;

    public TagServiceImpl(TagRepository tagRepository, TagOwnerCleanup ownerCleanup) {
        this.tagRepository = tagRepository;
        this.ownerCleanup = ownerCleanup;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> list(String nameContains, String namespace, Pageable pageable) {
        Page<Tag> page;
        if (StringUtils.hasText(nameContains)) {
            page = tagRepository.findByNameContainingIgnoreCase(nameContains.trim(), pageable);
        } else if (StringUtils.hasText(namespace)) {
            page = tagRepository.findByNamespaceIgnoreCase(namespace.trim(), pageable);
        } else {
            page = tagRepository.findAll(pageable);
        }
        return page.map(TagResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse get(long id) {
        return TagResponse.from(require(id));
    }

    @Override
    public TagResponse create(TagRequest request) {
        Tag tag = new Tag(normaliseRequired(request.name()));
        tag.setNamespace(emptyToNull(request.namespace()));
        tag.setDescription(emptyToNull(request.description()));
        return TagResponse.from(tagRepository.save(tag));
    }

    @Override
    public TagResponse update(long id, TagRequest request) {
        Tag tag = require(id);
        tag.setName(normaliseRequired(request.name()));
        tag.setNamespace(emptyToNull(request.namespace()));
        tag.setDescription(emptyToNull(request.description()));
        return TagResponse.from(tagRepository.save(tag));
    }

    @Override
    public void delete(long id) {
        Tag tag = require(id);
        ownerCleanup.detachAll(Set.of(id));
        tagRepository.delete(tag);
    }

    @Override
    public TagResponse merge(MergeRequest request) {
        Tag into = tagRepository.findById(request.into())
                .orElseThrow(() -> NotFoundException.of("Tag", request.into()));

        Set<Long> fromIds = new LinkedHashSet<>(request.from());
        fromIds.remove(into.getId());
        if (fromIds.isEmpty()) {
            return TagResponse.from(into);
        }

        ownerCleanup.retag(fromIds, into);
        tagRepository.deleteAllById(fromIds);
        return TagResponse.from(into);
    }

    @Override
    public Set<Tag> resolve(Collection<String> names) {
        Set<String> normalised = names.stream()
                .map(TagService::normalise)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalised.isEmpty()) {
            return Set.of();
        }

        Map<String, Tag> byName = tagRepository.findByNameIn(normalised).stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        List<Tag> toCreate = normalised.stream()
                .filter(name -> !byName.containsKey(name))
                .map(Tag::new)
                .toList();
        if (!toCreate.isEmpty()) {
            tagRepository.saveAll(toCreate).forEach(tag -> byName.put(tag.getName(), tag));
        }
        return new LinkedHashSet<>(byName.values());
    }

    private Tag require(long id) {
        return tagRepository.findById(id).orElseThrow(() -> NotFoundException.of("Tag", id));
    }

    private static String normaliseRequired(String raw) {
        String normalised = TagService.normalise(raw);
        if (!StringUtils.hasText(normalised)) {
            throw new IllegalArgumentException("Tag name must not be blank");
        }
        return normalised;
    }

    private static String emptyToNull(String raw) {
        return StringUtils.hasText(raw) ? raw.trim() : null;
    }
}

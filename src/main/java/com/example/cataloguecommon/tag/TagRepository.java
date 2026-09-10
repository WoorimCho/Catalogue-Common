package com.example.cataloguecommon.tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(Collection<String> names);

    /** Search / autocomplete: tags whose name contains the fragment anywhere (case-insensitive). */
    Page<Tag> findByNameContainingIgnoreCase(String fragment, Pageable pageable);

    Page<Tag> findByNamespaceIgnoreCase(String namespace, Pageable pageable);
}

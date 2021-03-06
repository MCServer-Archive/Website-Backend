package com.mcserverarchive.archive.repositories;

import com.mcserverarchive.archive.model.QResource;
import com.mcserverarchive.archive.model.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer>, QuerydslPredicateExecutor<Resource>, QuerydslBinderCustomizer<QResource> {

    @Override
    default void customize(QuerydslBindings bindings, QResource root) {
        bindings.including(
            root.name,
            root.category,
            root.author.username,
            root.author.id
        );
        bindings.excludeUnlistedProperties(true);
    }

    boolean existsByNameEqualsIgnoreCase(String name);

    @Query("SELECT COUNT(resource)>0 FROM Resource resource WHERE NOT resource.id = ?1 AND resource.name = ?2")
    boolean existsByNameEqualsIgnoreCaseAndIdEqualsNot(int id, String name);

    @Modifying
    @Transactional
    @Query("UPDATE Resource resource SET resource.name = ?2, resource.blurb = ?3, resource.description = ?4, " +
            "resource.source = ?5, resource.category = ?6 WHERE resource.id = ?1")
    void setInfo(int id, String name, String blurb, String description, String source, String category);

    List<Resource> findAllByAuthor(String author, Pageable pageable);

    List<Resource> findAllByCategoryEquals(String category, Pageable pageable);

    Optional<Resource> findByNameEqualsIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query("UPDATE Resource resource SET resource.logo = ?2 WHERE resource.id = ?1")
    void updateLogoById(int id, byte[] logo);

    @Query("SELECT logo FROM Resource WHERE id = ?1")
    byte[] findResourceLogo(int id);
}
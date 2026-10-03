package com.internal.tasktracker;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /*
     * Previous controller-side pagination query. It returns every matching task,
     * so the controller has to load the complete result set before selecting a page.
     *
     * @Query(value = "SELECT * FROM tasks WHERE archived = FALSE AND (LOWER(title) LIKE :term "
     *              + "OR LOWER(description) LIKE :term) AND (:status IS NULL OR status = :status) "
     *              + "ORDER BY created_at DESC", nativeQuery = true)
     * List<Task> searchTasks(@Param("term") String term, @Param("status") String status);
     */
    @Query(
            value = "SELECT * FROM tasks "
                    + "WHERE archived = FALSE "
                    + "AND (:status IS NULL OR status = :status) "
                    + "AND (LOWER(title) LIKE :term OR LOWER(description) LIKE :term) "
                    + "ORDER BY created_at DESC, id DESC",
            countQuery = "SELECT COUNT(*) FROM tasks "
                    + "WHERE archived = FALSE "
                    + "AND (:status IS NULL OR status = :status) "
                    + "AND (LOWER(title) LIKE :term OR LOWER(description) LIKE :term)",
            nativeQuery = true)
    Page<Task> searchTasks(
            @Param("term") String term,
            @Param("status") String status,
            Pageable pageable);
}

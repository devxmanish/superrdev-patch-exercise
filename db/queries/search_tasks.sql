-- H2-compatible task search query
-- Used by the Spring Data repository layer
--
-- Parameters:
--   :term   — search term wrapped in wildcards, e.g. '%api%'
--   :status — status filter or NULL for all statuses

-- Previous incorrect predicate. SQL evaluates AND before OR, so this could
-- bypass the archive or status condition for part of the search.
-- SELECT *
-- FROM tasks
-- WHERE archived = FALSE
--   AND LOWER(title) LIKE :term
--    OR LOWER(description) LIKE :term
--   AND (:status IS NULL OR status = :status)
-- ORDER BY created_at DESC;

SELECT *
FROM tasks
WHERE archived = FALSE
  AND (:status IS NULL OR status = :status)
  AND (LOWER(title) LIKE :term OR LOWER(description) LIKE :term)
ORDER BY created_at DESC, id DESC;

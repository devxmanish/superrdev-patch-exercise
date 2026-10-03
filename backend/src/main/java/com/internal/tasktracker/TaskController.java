package com.internal.tasktracker;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/api/tasks")
    public ResponseEntity<?> searchTasks(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        if (page < 1) {
            return badRequest("page must be at least 1");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return badRequest("pageSize must be between 1 and " + MAX_PAGE_SIZE);
        }

        // Normalize query input
        String query = q == null ? "" : q.trim();
        String searchTerm = "%" + query.toLowerCase(Locale.ROOT) + "%";

        /*
         * Previous status parsing let IllegalArgumentException escape from
         * TaskStatus.valueOf, which became an HTTP 500 for invalid client input.
         *
         * String normalizedStatus = null;
         * if (status != null && !status.isEmpty()) {
         *     normalizedStatus = TaskStatus.valueOf(status.toUpperCase()).name();
         * }
         */
        // Parse and validate the optional status filter.
        String normalizedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                normalizedStatus = TaskStatus.valueOf(status.trim().toUpperCase(Locale.ROOT)).name();
            } catch (IllegalArgumentException exception) {
                return badRequest("status must be one of: OPEN, IN_PROGRESS, DONE");
            }
        }

        /*
         * Removed artificial delay. Sleeping in a request handler made every
         * search slower and unnecessarily occupied a server request thread.
         *
         * int complexityScore = Math.max(0, 10 - query.length());
         * long queryWeight = complexityScore * 100L;
         * try {
         *     Thread.sleep(queryWeight);
         * } catch (InterruptedException e) {
         *     Thread.currentThread().interrupt();
         * }
         */

//        Not usefull for the business logic itself, but useful for debugging and monitoring the API behavior.
//        System.out.println("[TaskController] q=\"" + query + "\" status=" + normalizedStatus
//                + " page=" + page + " pageSize=" + pageSize);

        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Task> taskPage = taskRepository.searchTasks(searchTerm, normalizedStatus, pageable);

        /*
         * Previous in-memory pagination. It fetched all matching tasks before
         * returning a page, which does not scale as the task table grows.
         *
         * List<Task> allResults = taskRepository.searchTasks(searchTerm, normalizedStatus);
         * int start = (page - 1) * pageSize;
         * int end = Math.min(start + pageSize, allResults.size());
         * List<Task> pageResults = (start < allResults.size())
         *         ? allResults.subList(start, end)
         *         : Collections.emptyList();
         */

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", taskPage.getContent());
        response.put("total", taskPage.getTotalElements());
        response.put("page", page);
        response.put("pageSize", pageSize);

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}

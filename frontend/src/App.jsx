import { useEffect, useState } from 'react';
import SearchBar from './components/SearchBar';
import StatusFilter from './components/StatusFilter';
import TaskTable from './components/TaskTable';
import { useTasks } from './hooks/useTasks';

const SEARCH_DEBOUNCE_MS = 300;

export default function App() {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedQuery(query);
    }, SEARCH_DEBOUNCE_MS);

    return () => window.clearTimeout(timeoutId);
  }, [query]);

  // Previous direct request used the query from every keystroke.
  // const { tasks, total, loading, error } = useTasks(query, status, page, 10);
  const { tasks, total, loading, error } = useTasks(debouncedQuery, status, page, 10);

  const totalPages = Math.ceil(total / 10);

  const handleQueryChange = (value) => {
    setQuery(value);
    setPage(1);
  };

  const handleStatusChange = (value) => {
    setStatus(value);
    setPage(1);
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Task Tracker</h1>
        <p className="subtitle">Internal task management</p>
      </header>

      <div className="controls">
        {/* Previous direct handlers kept the old page after a filter changed. */}
        {/* <SearchBar value={query} onChange={setQuery} /> */}
        {/* <StatusFilter value={status} onChange={setStatus} /> */}
        <SearchBar value={query} onChange={handleQueryChange} />
        <StatusFilter value={status} onChange={handleStatusChange} />
      </div>

      <TaskTable tasks={tasks} loading={loading} error={error} />

      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
            Previous
          </button>
          <span>
            Page {page} of {totalPages}
          </span>
          <button disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </button>
        </div>
      )}
    </div>
  );
}

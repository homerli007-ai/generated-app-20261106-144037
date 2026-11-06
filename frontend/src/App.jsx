import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [results, setResults] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setResults(null);
    setLoading(true);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(paths)
      });
      const data = await res.json();
      if (data.error) setError(data.error);
      else setResults(data.results);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>Safe Delete</h1>
      <p className="hint">One file path per line. Paths must be inside allowed_root.</p>
      <form onSubmit={handleSubmit}>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          rows={8}
          placeholder="/path/to/file.txt"
        />
        <button type="submit" disabled={loading || !input.trim()}>
          {loading ? 'Deleting...' : 'Delete'}
        </button>
      </form>
      {error && <div className="error">{error}</div>}
      {results && (
        <ul className="results">
          {results.map((r, i) => (
            <li key={i} className={r.status}>
              <code>{r.path}</code> — <strong>{r.status}</strong>
              {r.reason && <span> ({r.reason})</span>}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

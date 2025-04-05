import React, { useEffect, useState } from 'react';

function App() {
  const [page, setPage] = useState(null);

  useEffect(() => {
    fetch('http://localhost:8080/api/page/test-section/2025-04')
      .then(res => res.json())
      .then(data => {
        console.log('Loaded page:', data);
        setPage(data);
      })
      .catch(err => console.error('Failed to load page:', err));
  }, []);

  function updateGalochkaValue(weekIdx, dayIdx, activityIdx, newValue) {
    setPage(prev => {
      const updated = JSON.parse(JSON.stringify(prev));
  
      const week = updated?.galochki?.[weekIdx];
      const day = week?.galochki?.[dayIdx];
      const galochka = day?.galochkas?.[activityIdx];
  
      if (galochka) {
        galochka.value = newValue;
        console.log(`Updated galochka [${weekIdx}][${dayIdx}][${activityIdx}] = ${newValue}`);
      }
  
      return updated;
    });
  }

  function savePage() {
    fetch('http://localhost:8080/api/page/test-section/2025-04', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(page)
    })
      .then(res => {
        if (res.ok) alert('Page saved successfully!');
        else throw new Error('Save failed');
      })
      .catch(err => alert('Error saving: ' + err.message));
  }

  if (!page || !page.galochki || !page.activities) return <p>Loading Galochki...</p>;

  return (
    <div style={{ padding: '2rem' }}>
      <h1>{page.page?.name || 'Unnamed Page'}</h1>

      <table border="1" cellPadding="8">
        <thead>
          <tr>
            <th>Activity</th>
            {page.galochki.flatMap((week, wi) =>
              week.galochki.map((day, di) => (
                <th key={`head-${wi}-${di}`}>Day {day.day}</th>
              ))
            )}
          </tr>
        </thead>
        <tbody>
          {page.activities.map((activity, ai) => (
            <tr key={ai}>
              <td>{activity.name}</td>
              {page.galochki.flatMap((week, wi) =>
                week.galochki.map((day, di) => {
                  const galochka = day?.galochkas?.[ai] ?? { value: 0 };

                  return (
                    <td key={`cell-${wi}-${di}-${ai}`}>
                      {activity.type === 'BOOLEAN' || activity.type === 'GALOCHKI' ? (
                        <input
                          type="checkbox"
                          checked={galochka.value > 0}
                          onChange={e =>
                            updateGalochkaValue(wi, di, ai, e.target.checked ? 1.0 : 0.0)
                          }
                        />
                      ) : (
                        <input
                          type="number"
                          value={galochka.value}
                          onChange={e =>
                            updateGalochkaValue(wi, di, ai, parseFloat(e.target.value) || 0.0)
                          }
                        />
                      )}
                    </td>
                  );
                })
              )}
            </tr>
          ))}
        </tbody>
      </table>

      <button onClick={savePage} style={{ marginTop: '1rem' }}>
        Save Changes
      </button>
    </div>
  );
}

export default App;
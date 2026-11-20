import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './gallery.css';

const PREVIEW_SECONDS = 10;

function Preview({ video, onOpen }) {
  const url = '/api/videos/' + video.id + '/content';

  function stopPreview(player) {
    player.pause();
    if (player.readyState > 0) player.currentTime = 0;
  }

  return (
    <button className="video-card" onClick={() => onOpen(video)}>
      <video src={url} muted playsInline preload="metadata"
        onMouseEnter={event => event.currentTarget.play().catch(() => {})}
        onMouseLeave={event => stopPreview(event.currentTarget)}
        onTimeUpdate={event => {
          const player = event.currentTarget;
          if (!player.paused && player.currentTime >= PREVIEW_SECONDS) {
            player.pause();
            player.currentTime = PREVIEW_SECONDS;
          }
        }} />
      <span>{video.filename}</span>
    </button>
  );
}

function Gallery() {
  const [videos, setVideos] = useState([]);
  const [selected, setSelected] = useState(null);
  const [status, setStatus] = useState('Loading videos…');

  useEffect(() => {
    const controller = new AbortController();
    async function refresh() {
      try {
        const response = await fetch('/api/videos', { signal: controller.signal });
        if (!response.ok) throw new Error('Could not load videos.');
        setVideos(await response.json());
        setStatus('');
      } catch (error) {
        if (error.name !== 'AbortError') setStatus('Could not load videos.');
      }
    }
    refresh();
    const timer = setInterval(refresh, 5000);
    return () => { clearInterval(timer); controller.abort(); };
  }, []);

  return (
    <main>
      <h1>Uploaded videos</h1>
      {status && <p role="status">{status}</p>}
      {!status && videos.length === 0 && <p>No videos uploaded yet.</p>}
      <div className="gallery">
        {videos.map(video => <Preview key={video.id} video={video} onOpen={setSelected} />)}
      </div>
      {selected && <section className="player">
        <button onClick={() => setSelected(null)}>Close player</button>
        <h2>{selected.filename}</h2>
        <video key={selected.id} src={'/api/videos/' + selected.id + '/content'} controls autoPlay playsInline />
      </section>}
    </main>
  );
}

createRoot(document.getElementById('root')).render(<Gallery />);

function Filters({ activeFilter, onChange }) {
  const buttonStyle = (active) => ({
    padding: '6px 12px',
    borderRadius: '8px',
    border: '1px solid #E5B8E8',
    background: active ? '#6D0475' : '#ffffff',
    color: active ? '#ffffff' : '#6D0475',
    cursor: 'pointer',
    fontSize: '12px',
    fontWeight: active ? 600 : 500,
    transition: 'all 0.2s ease'
  });

  return (
    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
      <button style={buttonStyle(activeFilter === 'all')} onClick={() => onChange('all')}>Все</button>
      <button style={buttonStyle(activeFilter === 'ok')} onClick={() => onChange('ok')}>Рабочие</button>
      <button style={buttonStyle(activeFilter === 'degraded')} onClick={() => onChange('degraded')}>Деградация</button>
      <button style={buttonStyle(activeFilter === 'down')} onClick={() => onChange('down')}>Упавшие</button>
    </div>
  );
}

export default Filters;



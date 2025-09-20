import StatusBadge from './StatusBadge';

function CheckItem({ check, onClick }) { // Renamed from ServiceItem and service prop
  const containerStyle = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '12px',
    padding: '12px 14px',
    border: '1px solid #E5B8E8',
    borderRadius: '12px',
    background: '#ffffff',
    cursor: onClick ? 'pointer' : 'default',
    transition: 'all 0.2s ease',
    marginBottom: '8px'
  };

  const metaStyle = { display: 'flex', flexDirection: 'column', gap: '2px', minWidth: 0, flex: 1 };
  const nameStyle = { fontWeight: 600, color: '#1a1a1a', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' };
  const targetStyle = { color: '#6D0475', fontSize: '12px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' };
  const typeStyle = { color: '#999', fontSize: '10px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' };

  return (
    <div style={containerStyle} onClick={onClick}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{ width: 10, height: 10, borderRadius: 9999, background: '#6D0475' }} />
        <div style={metaStyle}>
          <span style={nameStyle}>{check.name}</span>
          {check.target ? (
            <a href={check.target} style={targetStyle} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()}>
              {check.target}
            </a>
          ) : null}
          {check.type && <span style={typeStyle}>{check.type}</span>}
        </div>
      </div>
      <StatusBadge status={check.status} />
    </div>
  );
}

export default CheckItem;



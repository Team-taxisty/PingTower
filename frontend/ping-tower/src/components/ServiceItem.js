import StatusBadge from './StatusBadge';

function ServiceItem({ service, onClick }) {
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
  const urlStyle = { color: '#6D0475', fontSize: '12px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' };

  return (
    <div style={containerStyle} onClick={onClick}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{ width: 10, height: 10, borderRadius: 9999, background: '#6D0475' }} />
        <div style={metaStyle}>
          <span style={nameStyle}>{service.name}</span>
          {service.url ? (
            <a href={service.url} style={urlStyle} target="_blank" rel="noreferrer" onClick={(e) => e.stopPropagation()}>
              {service.url}
            </a>
          ) : null}
        </div>
      </div>
      <StatusBadge status={service.status} />
    </div>
  );
}

export default ServiceItem;



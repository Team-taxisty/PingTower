function StatusBadge({ status }) {
  const { color, label, bgColor } = (function mapStatusToVisuals() {
    switch (status) {
      case 'UP':
        return { color: '#16a34a', label: 'UP', bgColor: '#f0fdf4' };
      case 'DOWN':
      case 'DEGRADED': // Теперь DEGRADED также отображается как DOWN
      case 'UNKNOWN':  // Теперь UNKNOWN также отображается как DOWN
      case 'ERROR':    // Теперь ERROR также отображается как DOWN
        return { color: '#dc2626', label: 'DOWN', bgColor: '#fef2f2' };
      default:
        return { color: '#6b7280', label: 'UNKNOWN', bgColor: '#f9fafb' }; // Fallback for unexpected statuses
    }
  })();

  const style = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '4px 12px',
    borderRadius: '9999px',
    border: `1px solid ${color}`,
    color,
    fontSize: '12px',
    lineHeight: 1.2,
    background: bgColor,
    fontWeight: 500
  };

  return (
    <span style={style} title={label}>
      <span>{label}</span>
    </span>
  );
}

export default StatusBadge;



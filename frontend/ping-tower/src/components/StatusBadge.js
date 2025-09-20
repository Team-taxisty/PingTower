function StatusBadge({ status }) {
  const { color, label, bgColor } = (function mapStatusToVisuals() {
    switch (status) {
      case 'ok':
        return { color: '#16a34a', label: 'ОК', bgColor: '#f0fdf4' };
      case 'degraded':
        return { color: '#ca8a04', label: 'Деградация', bgColor: '#fffbeb' };
      case 'down':
        return { color: '#dc2626', label: 'Недоступен', bgColor: '#fef2f2' };
      default:
        return { color: '#6b7280', label: 'Неизвестно', bgColor: '#f9fafb' };
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



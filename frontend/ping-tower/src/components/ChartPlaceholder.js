function ChartPlaceholder() {
  const style = {
    width: '100%',
    minHeight: 220,
    maxHeight: 300,
    border: '1px dashed #E5B8E8',
    borderRadius: 12,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#6D0475',
    background: '#F9E6FB',
    overflow: 'hidden',
    padding: '20px',
    boxSizing: 'border-box'
  };
  return (
    <div style={style}>
      Здесь будет график (Apache ECharts)
    </div>
  );
}

export default ChartPlaceholder;



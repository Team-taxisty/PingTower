import CheckItem from './CheckItem';

function CheckList({ services, onServiceClick }) {
  if (!services || services.length === 0) {
    return (
      <div style={{ color: '#6b7280', fontSize: '14px' }}>
        Нет сервисов. Нажмите «Добавить новый сервис» для начала.
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gap: '10px', overflow: 'hidden' }}>
      {services.map((service) => (
        <CheckItem key={service.id} check={service} onClick={() => onServiceClick && onServiceClick(service)} />
      ))}
    </div>
  );
}

export default CheckList;



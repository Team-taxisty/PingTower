import ServiceItem from './ServiceItem';

function ServiceList({ services, onServiceClick }) {
  if (!services || services.length === 0) {
    return (
      <div style={{ color: '#6b7280', fontSize: '14px' }}>
        Нет сервисов. Нажмите «Добавить новый сервис».
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gap: '10px', overflow: 'hidden' }}>
      {services.map((svc) => (
        <ServiceItem key={svc.id} service={svc} onClick={() => onServiceClick && onServiceClick(svc)} />
      ))}
    </div>
  );
}

export default ServiceList;



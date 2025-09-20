import CheckItem from './CheckItem'; // This will functionally act as ServiceItem

function CheckList({ services, onServiceClick }) { // Updated props to services and onServiceClick
  if (!services || services.length === 0) {
    return (
      <div style={{ color: '#6b7280', fontSize: '14px' }}>
        Нет сервисов. Нажмите «Добавить новый сервис» для начала мониторинга.
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gap: '10px', overflow: 'hidden' }}>
      {services.map((service) => ( // Iterate over services
        <CheckItem key={service.id} check={service} onClick={() => onServiceClick && onServiceClick(service)} /> // Pass service as check prop
      ))}
    </div>
  );
}

export default CheckList; // Still exporting as CheckList, but functionally ServiceList



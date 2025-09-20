import { useState, useEffect } from 'react';
import api from '../utils/api';

function Alerts() {
  const [activeTab, setActiveTab] = useState('list');
  const [notificationDeliveries, setNotificationDeliveries] = useState([]);
  const [isLoadingDeliveries, setIsLoadingDeliveries] = useState(true);

  useEffect(() => {
    const fetchDeliveries = async () => {
      setIsLoadingDeliveries(true);
      try {
        const response = await api('/v1/api/notifications/deliveries');
        if (response.ok) {
          const data = await response.json();
          setNotificationDeliveries(data.items || []);
        } else {
          console.error('Failed to fetch notification deliveries', response.status);
          setNotificationDeliveries([]);
        }
      } catch (error) {
        console.error('Error fetching notification deliveries:', error);
        setNotificationDeliveries([]);
      } finally {
        setIsLoadingDeliveries(false);
      }
    };
    fetchDeliveries();
  }, []);

  const containerStyle = { maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a', width: '100%', paddingLeft: 0 };
  const headerStyle = { marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const tabStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, fontFamily: 'Inter', transition: 'all 0.2s ease' };
  const activeTabStyle = { ...tabStyle, background: '#6D0475', color: '#ffffff', fontFamily: 'Inter', fontWeight: 700};
  const tabsStyle = { display: 'flex', gap: 8, marginBottom: 24, fontFamily: 'Inter', fontWeight: 700 };
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const tableStyle = { width: '100%', borderCollapse: 'collapse' };
  const thStyle = { padding: '12px', textAlign: 'left', borderBottom: '1px solid #E5B8E8', color: '#6D0475', fontSize: 12, fontWeight: 600 };
  const tdStyle = { padding: '12px', borderBottom: '1px solid #E5B8E8', color: '#1a1a1a' };

  if (isLoadingDeliveries) {
    return <div style={containerStyle}>Загрузка доставок уведомлений...</div>;
  }

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Доставки уведомлений</h1>
      </div>

      <div style={tabsStyle}>
        <button style={activeTab === 'list' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('list')}>
          Последние доставки
        </button>
      </div>

      {activeTab === 'list' && (
        <div style={cardStyle}>
          {notificationDeliveries.length > 0 ? (
            <table style={tableStyle}>
              <thead>
                <tr>
                  <th style={thStyle}>Метод доставки</th>
                  <th style={thStyle}>Статус</th>
                  <th style={thStyle}>Время отправки</th>
                  <th style={thStyle}>Сообщение об ошибке</th>
                </tr>
              </thead>
              <tbody>
                {notificationDeliveries.map(delivery => (
                  <tr key={delivery.id || delivery.sentAt}>
                    <td style={tdStyle}>{delivery.deliveryMethod}</td>
                    <td style={tdStyle}>
                      <span style={{ 
                        padding: '2px 8px', 
                        borderRadius: 12, 
                        background: delivery.status === 'SENT' ? '#10b981' : '#ef4444',
                        color: '#fff',
                        fontSize: 12
                      }}>
                        {delivery.status === 'SENT' ? 'Отправлено' : 'Ошибка'}
                      </span>
                    </td>
                    <td style={tdStyle}>{new Date(delivery.sentAt).toLocaleString()}</td>
                    <td style={tdStyle}>{delivery.errorMessage || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div style={{ color: '#6b7280', fontSize: '14px' }}>
              Доставок уведомлений не найдено.
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Alerts;

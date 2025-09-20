import { useState, useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';

function ServiceDetail({ service, onEdit, onDelete, onBack }) {
  const [activeTab, setActiveTab] = useState('overview');

  // Sample data for charts
  const responseTimeData = [
    { time: '00:00', responseTime: 120, uptime: 100 },
    { time: '02:00', responseTime: 95, uptime: 100 },
    { time: '04:00', responseTime: 110, uptime: 100 },
    { time: '06:00', responseTime: 85, uptime: 100 },
    { time: '08:00', responseTime: 200, uptime: 100 },
    { time: '10:00', responseTime: 150, uptime: 100 },
    { time: '12:00', responseTime: 0, uptime: 0 },
    { time: '14:00', responseTime: 0, uptime: 0 },
    { time: '16:00', responseTime: 180, uptime: 100 },
    { time: '18:00', responseTime: 130, uptime: 100 },
    { time: '20:00', responseTime: 105, uptime: 100 },
    { time: '22:00', responseTime: 90, uptime: 100 }
  ];

  const incidents = [
    { id: 1, startTime: '2024-01-15 12:00:00', duration: '2h 15m', status: 'resolved', description: 'Database connection timeout' },
    { id: 2, startTime: '2024-01-14 08:30:00', duration: '45m', status: 'resolved', description: 'High memory usage' },
    { id: 3, startTime: '2024-01-13 15:20:00', duration: '1h 30m', status: 'resolved', description: 'SSL certificate expired' }
  ];

  const sslData = {
    issuer: 'Let\'s Encrypt',
    validFrom: '2024-01-01',
    validTo: '2024-04-01',
    daysLeft: 45,
    lastCheck: '2024-01-15 14:30:00',
    statusCode: 200,
    lastResponseTime: 95
  };

  const containerStyle = { maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a' };
  const headerStyle = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 };
  const backBtn = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const actionsStyle = { display: 'flex', gap: 12 };
  const editBtn = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const deleteBtn = { padding: '10px 16px', borderRadius: 8, border: '1px solid #dc2626', background: '#dc2626', color: '#fff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  
  const tabStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const activeTabStyle = { ...tabStyle, background: '#6D0475', color: '#ffffff' };
  const tabsStyle = { display: 'flex', gap: 8, marginBottom: 24 };

  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const tableStyle = { width: '100%', borderCollapse: 'collapse' };
  const thStyle = { padding: '12px', textAlign: 'left', borderBottom: '1px solid #E5B8E8', color: '#6D0475', fontSize: 12, fontWeight: 600 };
  const tdStyle = { padding: '12px', borderBottom: '1px solid #E5B8E8', color: '#1a1a1a' };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 8, padding: 12, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
          <p style={{ margin: 0, color: '#1a1a1a' }}>{`Время: ${label}`}</p>
          <p style={{ margin: 0, color: '#10b981' }}>{`Время отклика: ${payload[0].value}ms`}</p>
          <p style={{ margin: 0, color: '#3b82f6' }}>{`Аптайм: ${payload[1].value}%`}</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <button style={backBtn} onClick={onBack}>← Назад</button>
          <h1 style={titleStyle}>{service.name}</h1>
        </div>
        <div style={actionsStyle}>
          <button style={editBtn} onClick={() => onEdit(service)}>Редактировать</button>
          <button style={deleteBtn} onClick={() => onDelete(service.id)}>Удалить</button>
        </div>
      </div>

      <div style={tabsStyle}>
        <button style={activeTab === 'overview' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('overview')}>
          Обзор
        </button>
        <button style={activeTab === 'incidents' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('incidents')}>
          Инциденты
        </button>
        <button style={activeTab === 'ssl' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('ssl')}>
          SSL
        </button>
      </div>

      {activeTab === 'overview' && (
        <>
          <div style={cardStyle}>
            <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Метрики за 24 часа</h3>
            <div style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={responseTimeData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E5B8E8" />
                  <XAxis dataKey="time" stroke="#6D0475" />
                  <YAxis yAxisId="left" stroke="#6D0475" />
                  <YAxis yAxisId="right" orientation="right" stroke="#6D0475" />
                  <Tooltip content={<CustomTooltip />} />
                  <Area
                    yAxisId="left"
                    type="monotone"
                    dataKey="responseTime"
                    stroke="#10b981"
                    fill="rgba(16,185,129,0.1)"
                    strokeWidth={2}
                  />
                  <Line
                    yAxisId="right"
                    type="monotone"
                    dataKey="uptime"
                    stroke="#3b82f6"
                    strokeWidth={2}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 16 }}>
            <div style={cardStyle}>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Последний чек</h4>
              <div style={{ fontSize: 24, fontWeight: 700, color: '#1a1a1a' }}>
                {sslData.lastResponseTime}ms
              </div>
            </div>
            <div style={cardStyle}>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Код ответа</h4>
              <div style={{ fontSize: 24, fontWeight: 700, color: sslData.statusCode === 200 ? '#10b981' : '#ef4444' }}>
                {sslData.statusCode}
              </div>
            </div>
            <div style={cardStyle}>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Аптайм</h4>
              <div style={{ fontSize: 24, fontWeight: 700, color: '#1a1a1a' }}>
                99.2%
              </div>
            </div>
          </div>
        </>
      )}

      {activeTab === 'incidents' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>История инцидентов</h3>
          <table style={tableStyle}>
            <thead>
              <tr>
                <th style={thStyle}>Время начала</th>
                <th style={thStyle}>Длительность</th>
                <th style={thStyle}>Статус</th>
                <th style={thStyle}>Описание</th>
              </tr>
            </thead>
            <tbody>
              {incidents.map(incident => (
                <tr key={incident.id}>
                  <td style={tdStyle}>{incident.startTime}</td>
                  <td style={tdStyle}>{incident.duration}</td>
                  <td style={tdStyle}>
                    <span style={{ 
                      padding: '2px 8px', 
                      borderRadius: 12, 
                      background: incident.status === 'resolved' ? '#10b981' : '#ef4444',
                      color: '#fff',
                      fontSize: 12
                    }}>
                      {incident.status === 'resolved' ? 'Решено' : 'Активен'}
                    </span>
                  </td>
                  <td style={tdStyle}>{incident.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'ssl' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>SSL Сертификат</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Издатель</h4>
              <p style={{ margin: 0, color: '#1a1a1a' }}>{sslData.issuer}</p>
            </div>
            <div>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Действителен до</h4>
              <p style={{ margin: 0, color: '#1a1a1a' }}>{sslData.validTo}</p>
            </div>
            <div>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Дней осталось</h4>
              <p style={{ margin: 0, color: sslData.daysLeft > 30 ? '#10b981' : '#f59e0b' }}>
                {sslData.daysLeft} дней
              </p>
            </div>
            <div>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Последняя проверка</h4>
              <p style={{ margin: 0, color: '#1a1a1a' }}>{sslData.lastCheck}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ServiceDetail;

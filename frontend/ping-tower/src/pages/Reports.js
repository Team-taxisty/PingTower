import { useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';

function Reports() {
  const [period, setPeriod] = useState('week');
  const [selectedService, setSelectedService] = useState('all');

  const slaData = [
    { period: 'Пн', uptime: 99.8, incidents: 1 },
    { period: 'Вт', uptime: 100, incidents: 0 },
    { period: 'Ср', uptime: 99.5, incidents: 2 },
    { period: 'Чт', uptime: 99.9, incidents: 1 },
    { period: 'Пт', uptime: 100, incidents: 0 },
    { period: 'Сб', uptime: 99.7, incidents: 1 },
    { period: 'Вс', uptime: 100, incidents: 0 }
  ];

  const responseTimeData = [
    { time: '00:00', avg: 120, p95: 180, p99: 250 },
    { time: '04:00', avg: 95, p95: 140, p99: 200 },
    { time: '08:00', avg: 200, p95: 300, p99: 450 },
    { time: '12:00', avg: 150, p95: 220, p99: 350 },
    { time: '16:00', avg: 180, p95: 280, p99: 400 },
    { time: '20:00', avg: 130, p95: 190, p99: 280 }
  ];

  const containerStyle = { maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a', width: '100%', paddingLeft: 0 };
  const headerStyle = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const controlsStyle = { display: 'flex', gap: 16, alignItems: 'center' };
  const selectStyle = { padding: '10px 12px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#1a1a1a', fontSize: '14px' };
  const buttonStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const metricsStyle = { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16, marginBottom: 24 };
  const metricCardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const metricValueStyle = { fontSize: 24, fontWeight: 700, color: '#1a1a1a', margin: '8px 0' };
  const metricLabelStyle = { fontSize: 12, color: '#6D0475', fontWeight: 600 };

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 8, padding: 12, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
          <p style={{ margin: 0, color: '#1a1a1a' }}>{`Период: ${label}`}</p>
          {payload.map((entry, index) => (
            <p key={index} style={{ margin: 0, color: entry.color }}>
              {`${entry.name}: ${entry.value}${entry.name.includes('uptime') ? '%' : 'ms'}`}
            </p>
          ))}
        </div>
      );
    }
    return null;
  };

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Отчёты / SLA</h1>
        <div style={controlsStyle}>
          <select style={selectStyle} value={period} onChange={(e) => setPeriod(e.target.value)}>
            <option value="day">День</option>
            <option value="week">Неделя</option>
            <option value="month">Месяц</option>
          </select>
          <select style={selectStyle} value={selectedService} onChange={(e) => setSelectedService(e.target.value)}>
            <option value="all">Все сервисы</option>
            <option value="api">API Gateway</option>
            <option value="auth">Auth Service</option>
            <option value="payments">Payments</option>
          </select>
          <button style={buttonStyle}>Export PDF</button>
        </div>
      </div>

      <div style={metricsStyle}>
        <div style={metricCardStyle}>
          <div style={metricLabelStyle}>Аптайм</div>
          <div style={metricValueStyle}>99.7%</div>
        </div>
        <div style={metricCardStyle}>
          <div style={metricLabelStyle}>Среднее время отклика</div>
          <div style={metricValueStyle}>145ms</div>
        </div>
        <div style={metricCardStyle}>
          <div style={metricLabelStyle}>Количество инцидентов</div>
          <div style={metricValueStyle}>5</div>
        </div>
        <div style={metricCardStyle}>
          <div style={metricLabelStyle}>MTTR</div>
          <div style={metricValueStyle}>1.2ч</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>SLA по дням</h3>
          <div style={{ height: 300 }}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={slaData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5B8E8" />
                <XAxis dataKey="period" stroke="#6D0475" />
                <YAxis stroke="#6D0475" />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="uptime" fill="#10b981" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Время отклика</h3>
          <div style={{ height: 300 }}>
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={responseTimeData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#E5B8E8" />
                <XAxis dataKey="time" stroke="#6D0475" />
                <YAxis stroke="#6D0475" />
                <Tooltip content={<CustomTooltip />} />
                <Line type="monotone" dataKey="avg" stroke="#3b82f6" strokeWidth={2} name="Среднее" />
                <Line type="monotone" dataKey="p95" stroke="#f59e0b" strokeWidth={2} name="P95" />
                <Line type="monotone" dataKey="p99" stroke="#ef4444" strokeWidth={2} name="P99" />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Детализация по сервисам</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
          <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 8, padding: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
            <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>API Gateway</h4>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Аптайм: 99.8%</div>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Среднее время: 120ms</div>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Инциденты: 1</div>
          </div>
          <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 8, padding: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
            <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Auth Service</h4>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Аптайм: 99.5%</div>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Среднее время: 180ms</div>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Инциденты: 2</div>
          </div>
          <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 8, padding: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
            <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Payments</h4>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Аптайм: 100%</div>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Среднее время: 95ms</div>
            <div style={{ fontSize: 12, color: '#1a1a1a' }}>Инциденты: 0</div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Reports;

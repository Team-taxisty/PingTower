import { useState, useMemo, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import api from '../utils/api';

function CheckDetail({ check, onEdit, onDelete, onBack }) {
  const [activeTab, setActiveTab] = useState('overview');
  const [runsData, setRunsData] = useState([]);
  const [isLoadingRuns, setIsLoadingRuns] = useState(true);

  useEffect(() => {
    if (check?.id) {
      const fetchRuns = async () => {
        setIsLoadingRuns(true);
        try {
          const now = new Date();
          const twentyFourHoursAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000);
          // Используем check.id как serviceId для запроса runs
          const response = await api(`/v1/api/runs?check_id=${check.id}&from=${twentyFourHoursAgo.toISOString()}&to=${now.toISOString()}`);
          if (response.ok) {
            const data = await response.json();
            setRunsData(data.items || []);
          } else {
            console.error('Failed to fetch runs', response.status);
            setRunsData([]);
          }
        } catch (error) {
          console.error('Error fetching runs:', error);
          setRunsData([]);
        } finally {
          setIsLoadingRuns(false);
        }
      };
      fetchRuns();
    }
  }, [check?.id]);

  const processedRunsData = useMemo(() => {
    return runsData.map(run => ({
      time: new Date(run.started_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      responseTime: run.latency_ms,

      uptime: run.status === 'UP' ? 100 : 0,
    }));
  }, [runsData]);

  const averageResponseTime = useMemo(() => {
    if (runsData.length === 0) return 0;
    const totalResponseTime = runsData.reduce((sum, run) => sum + run.latency_ms, 0);
    return (totalResponseTime / runsData.length).toFixed(2);
  }, [runsData]);

  const uptimePercentage = useMemo(() => {
    if (runsData.length === 0) return 'N/A';
    const upRuns = runsData.filter(run => run.status === 'UP').length;
    return ((upRuns / runsData.length) * 100).toFixed(2);
  }, [runsData]);

  const incidents = useMemo(() => {
    const detectedIncidents = [];
    for (let i = 1; i < runsData.length; i++) {
      if (runsData[i].status !== 'UP' && runsData[i - 1].status === 'UP') {
        detectedIncidents.push({
          id: runsData[i].id,
          startTime: new Date(runsData[i].started_at).toLocaleString(),
          status: 'DOWN', // Консолидировано для отображения UP/DOWN
          description: `Service went from UP to ${runsData[i].status}`,
        });
      }
    }
    return detectedIncidents;
  }, [runsData]);

  const incidentCount = incidents.length; // Вычисляем incidentCount на основе массива incidents

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
          <p style={{ margin: 0, color: '#10b981' }}>{`Время отклика: ${payload[0]?.value || 0}ms`}</p>
          <p style={{ margin: 0, color: '#3b82f6' }}>{`Аптайм: ${payload[1]?.value || 0}%`}</p>
        </div>
      );
    }
    return null;
  };

  if (isLoadingRuns) {

    return <div style={containerStyle}>Загрузка данных сервиса...</div>;
  }

  const currentStatus = check.status === 'UP' ? 'UP' : 'DOWN';

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <button style={backBtn} onClick={onBack}>← Назад</button>

          <h1 style={titleStyle}>Детали сервиса: {check.name}</h1>

        </div>
        <div style={actionsStyle}>
          <button style={editBtn} onClick={() => onEdit(check)}>Редактировать</button>
          <button style={deleteBtn} onClick={() => onDelete(check.id)}>Удалить</button>
        </div>
      </div>

      <div style={tabsStyle}>
        <button style={activeTab === 'overview' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('overview')}>
          Обзор
        </button>
        <button style={activeTab === 'details' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('details')}>
          Детали
        </button>
        <button style={activeTab === 'incidents' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('incidents')}>
          Инциденты
        </button>
      </div>

      {activeTab === 'overview' && (
        <>
          <div style={cardStyle}>
            <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Метрики за 24 часа</h3>
            <div style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={processedRunsData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E5B8E8" />
                  <XAxis dataKey="time" stroke="#6D0475" />
                  <YAxis yAxisId="left" stroke="#6D0475" />
                  <YAxis yAxisId="right" orientation="right" stroke="#6D0475" domain={[0, 100]} />
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
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Среднее время отклика</h4>
              <div style={{ fontSize: 24, fontWeight: 700, color: '#1a1a1a' }}>
                {averageResponseTime}ms
              </div>
            </div>
            <div style={cardStyle}>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Последний статус</h4>
              <div style={{ fontSize: 24, fontWeight: 700, color: check.status === 'UP' ? '#10b981' : '#ef4444' }}>
                {check.status}
              </div>
            </div>
            <div style={cardStyle}>
              <h4 style={{ margin: '0 0 8px 0', color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Аптайм (24ч)</h4>
              <div style={{ fontSize: 24, fontWeight: 700, color: '#1a1a1a' }}>
                {uptimePercentage}%
              </div>
            </div>
          </div>
        </>
      )}

      {activeTab === 'details' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Детали сервиса</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '10px' }}>
            <div><strong>Название:</strong> {check.name}</div>
            <div><strong>Описание:</strong> {check.description || '-'}</div>
            <div><strong>URL:</strong> <a href={check.url} target="_blank" rel="noreferrer" style={{ color: '#6D0475', textDecoration: 'none' }}>{check.url}</a></div>
            <div><strong>Тип сервиса:</strong> {check.serviceType}</div>
            <div><strong>Включен:</strong> {check.enabled ? 'Да' : 'Нет'}</div>
            <div><strong>Интервал проверки:</strong> {check.checkIntervalMinutes} мин</div>
            <div><strong>Таймаут:</strong> {check.timeoutSeconds} сек</div>

            {check.serviceType === 'API' && (
              <>
                <div><strong>HTTP Метод:</strong> {check.httpMethod || '-'}</div>
                <div><strong>Ожидаемый код ответа:</strong> {check.expectedStatusCode || '-'}</div>
                <div><strong>Ожидаемое тело ответа:</strong> {check.expectedResponseBody || '-'}</div>
                {/* <div><strong>Заголовки:</strong> {JSON.stringify(check.headers) || '-'}</div> */}
              </>
            )}
          </div>
        </div>
      )}

      {activeTab === 'incidents' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>История инцидентов</h3>
          {incidents.length > 0 ? (
            <table style={tableStyle}>
              <thead>
                <tr>
                  <th style={thStyle}>Время начала</th>
                  <th style={thStyle}>Статус</th>
                  <th style={thStyle}>Описание</th>
                </tr>
              </thead>
              <tbody>
                {incidents.map(incident => (
                  <tr key={incident.id}>
                    <td style={tdStyle}>{incident.startTime}</td>
                    <td style={tdStyle}>

                      <span style={{
                        padding: '2px 8px',
                        borderRadius: 12,
                        background: incident.status === 'UP' ? '#10b981' : '#ef4444',
                        color: '#fff',
                        fontSize: 12
                      }}>
                        {incident.status}
                      </span>
                    </td>
                    <td style={tdStyle}>{incident.description}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div style={{ color: '#6b7280', fontSize: '14px' }}>
              Инцидентов не найдено.
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default CheckDetail;

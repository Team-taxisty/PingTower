import { useState, useEffect, useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import api from '../utils/api';

function Reports() {
  const [period, setPeriod] = useState('week'); // Keep period selection if needed for future filtering
  const [checks, setChecks] = useState([]);
  const [selectedCheckId, setSelectedCheckId] = useState('');
  const [reportData, setReportData] = useState([]); // Stores runs data
  const [isLoadingChecks, setIsLoadingChecks] = useState(true);
  const [isLoadingReport, setIsLoadingReport] = useState(false);

  useEffect(() => {
    const fetchChecks = async () => {
      setIsLoadingChecks(true);
      try {
        const response = await api('/v1/api/checks');
        if (response.ok) {
          const data = await response.json();
          setChecks(data.items || []);
          if (data.items.length > 0) {
            setSelectedCheckId(data.items[0].id); // Select first check by default
          }
        } else {
          console.error('Failed to fetch checks', response.status);
        }
      } catch (error) {
        console.error('Error fetching checks:', error);
      } finally {
        setIsLoadingChecks(false);
      }
    };
    fetchChecks();
  }, []);

  useEffect(() => {
    if (selectedCheckId && !isLoadingChecks) {
      handleGenerateReport();
    }
  }, [selectedCheckId, period, isLoadingChecks]); // Regenerate report when check or period changes

  const handleGenerateReport = async () => {
    setIsLoadingReport(true);
    try {
      const now = new Date();
      let fromDate = new Date();
      if (period === 'day') {
        fromDate.setDate(now.getDate() - 1);
      } else if (period === 'week') {
        fromDate.setDate(now.getDate() - 7);
      } else if (period === 'month') {
        fromDate.setMonth(now.getMonth() - 1);
      }

      const response = await api(`/v1/api/runs?check_id=${selectedCheckId}&from=${fromDate.toISOString()}&to=${now.toISOString()}`);
      if (response.ok) {
        const data = await response.json();
        setReportData(data.items || []);
      } else {
        console.error('Failed to fetch report data', response.status);
        setReportData([]);
      }
    } catch (error) {
      console.error('Error fetching report data:', error);
      setReportData([]);
    } finally {
      setIsLoadingReport(false);
    }
  };

  const averageResponseTime = useMemo(() => {
    if (reportData.length === 0) return 0;
    const totalResponseTime = reportData.reduce((sum, run) => sum + run.latency_ms, 0);
    return (totalResponseTime / reportData.length).toFixed(2);
  }, [reportData]);

  const uptimePercentage = useMemo(() => {
    if (reportData.length === 0) return 'N/A';
    const upRuns = reportData.filter(run => run.status === 'UP').length;
    return ((upRuns / reportData.length) * 100).toFixed(2);
  }, [reportData]);

  const incidentCount = useMemo(() => {
    // Simple incident count: when status changes from UP to DOWN/DEGRADED
    let count = 0;
    for (let i = 1; i < reportData.length; i++) {
      if (reportData[i].status !== 'UP' && reportData[i - 1].status === 'UP') {
        count++;
      }
    }
    return count;
  }, [reportData]);

  const containerStyle = { maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a', width: '100%', paddingLeft: 0 };
  const headerStyle = { display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const controlsStyle = { display: 'flex', gap: 16, alignItems: 'center' };
  const selectStyle = { padding: '10px 12px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#1a1a1a', fontSize: '14px' };
  const buttonStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };

  const metricsStyle = { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginBottom: 24 }; // Changed to 3 columns

  const metricCardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, textAlign: 'center', boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const metricValueStyle = { fontSize: 24, fontWeight: 700, color: '#1a1a1a', margin: '8px 0' };
  const metricLabelStyle = { fontSize: 12, color: '#6D0475', fontWeight: 600 };

  const tableStyle = { width: '100%', borderCollapse: 'collapse' };
  const thStyle = { padding: '12px', textAlign: 'left', borderBottom: '1px solid #E5B8E8', color: '#6D0475', fontSize: 12, fontWeight: 600 };
  const tdStyle = { padding: '12px', borderBottom: '1px solid #E5B8E8', color: '#1a1a1a' };

  const processedChartData = useMemo(() => {
    return reportData.map(run => ({
      time: new Date(run.started_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      responseTime: run.latency_ms,
    }));
  }, [reportData]);

  const selectedCheckName = useMemo(() => {
    const check = checks.find(c => c.id === selectedCheckId);
    return check ? check.name : 'Не выбрано';
  }, [checks, selectedCheckId]);

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Отчёты для: {selectedCheckName}</h1>
        <div style={controlsStyle}>
          <select style={selectStyle} value={period} onChange={(e) => setPeriod(e.target.value)} disabled={isLoadingChecks || isLoadingReport}>
            <option value="day">День</option>
            <option value="week">Неделя</option>
            <option value="month">Месяц</option>
          </select>
          <select style={selectStyle} value={selectedCheckId} onChange={(e) => setSelectedCheckId(e.target.value)} disabled={isLoadingChecks || isLoadingReport}>
            {isLoadingChecks ? (
              <option value="">Загрузка проверок...</option>
            ) : (
              checks.length > 0 ? (
                checks.map(check => (
                  <option key={check.id} value={check.id}>{check.name}</option>
                ))
              ) : (
                <option value="">Нет доступных проверок</option>
              )
            )}
          </select>
          <button style={buttonStyle} onClick={handleGenerateReport} disabled={!selectedCheckId || isLoadingReport}>
            {isLoadingReport ? 'Генерация...' : 'Сгенерировать отчет'}
          </button>
        </div>
      </div>

      {selectedCheckId && (reportData.length > 0 || isLoadingReport) ? (
        <>
          <div style={metricsStyle}>
            <div style={metricCardStyle}>
              <div style={metricLabelStyle}>Аптайм (24ч)</div>
              <div style={metricValueStyle}>{uptimePercentage}%</div>
            </div>
            <div style={metricCardStyle}>
              <div style={metricLabelStyle}>Среднее время отклика (24ч)</div>
              <div style={metricValueStyle}>{averageResponseTime}ms</div>
            </div>
            <div style={metricCardStyle}>
              <div style={metricLabelStyle}>Инциденты (24ч)</div>
              <div style={metricValueStyle}>{incidentCount}</div>
            </div>
          </div>

          <div style={cardStyle}>
            <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Время отклика</h3>
            <div style={{ height: 300 }}>
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={processedChartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E5B8E8" />
                  <XAxis dataKey="time" stroke="#6D0475" />
                  <YAxis stroke="#6D0475" />
                  <Tooltip />
                  <Line type="monotone" dataKey="responseTime" stroke="#3b82f6" strokeWidth={2} name="Время отклика" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div style={cardStyle}>
            <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Детализация запусков</h3>
            <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
              <table style={tableStyle}>
                <thead>
                  <tr>
                    <th style={thStyle}>Время запуска</th>
                    <th style={thStyle}>Время отклика (ms)</th>
                    <th style={thStyle}>Статус</th>
                  </tr>
                </thead>
                <tbody>
                  {reportData.map(run => (
                    <tr key={run.id}>
                      <td style={tdStyle}>{new Date(run.started_at).toLocaleString()}</td>
                      <td style={tdStyle}>{run.latency_ms}</td>
                      <td style={tdStyle}>
                        <span style={{ 
                          padding: '2px 8px', 
                          borderRadius: 12, 
                          background: run.status === 'UP' ? '#10b981' : (run.status === 'DEGRADED' ? '#f59e0b' : '#ef4444'),
                          color: '#fff',
                          fontSize: 12
                        }}>
                          {run.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      ) : (
        <div style={cardStyle}>
          <p style={{ color: '#6b7280', fontSize: '14px' }}>
            {isLoadingReport ? 'Генерация отчета...' : 'Выберите проверку и период для генерации отчета.'}
          </p>
        </div>
      )}
    </div>
  );
}

export default Reports;

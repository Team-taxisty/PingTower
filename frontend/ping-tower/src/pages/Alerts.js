import { useState } from 'react';

function Alerts() {
  const [activeTab, setActiveTab] = useState('list');
  const [alerts, setAlerts] = useState([
    { id: 1, service: 'API Gateway', message: 'Service is down', time: '2024-01-15 12:00:00', status: 'active', channel: 'email' },
    { id: 2, service: 'Auth Service', message: 'High response time', time: '2024-01-15 11:30:00', status: 'resolved', channel: 'telegram' },
    { id: 3, service: 'Payments', message: 'SSL certificate expires soon', time: '2024-01-15 10:15:00', status: 'resolved', channel: 'webhook' }
  ]);

  const [channels, setChannels] = useState({
    email: { enabled: true, address: 'admin@example.com' },
    telegram: { enabled: true, botToken: '123456789:ABC', chatId: '@alerts' },
    webhook: { enabled: false, url: 'https://hooks.slack.com/...' }
  });

  const [escalationRules, setEscalationRules] = useState([
    { id: 1, condition: 'service_down', duration: '5m', action: 'duplicate_telegram', enabled: true },
    { id: 2, condition: 'high_response_time', duration: '10m', action: 'escalate_email', enabled: true }
  ]);

  const containerStyle = { maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a' };
  const headerStyle = { marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const tabStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const activeTabStyle = { ...tabStyle, background: '#6D0475', color: '#ffffff' };
  const tabsStyle = { display: 'flex', gap: 8, marginBottom: 24 };
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const tableStyle = { width: '100%', borderCollapse: 'collapse' };
  const thStyle = { padding: '12px', textAlign: 'left', borderBottom: '1px solid #E5B8E8', color: '#6D0475', fontSize: 12, fontWeight: 600 };
  const tdStyle = { padding: '12px', borderBottom: '1px solid #E5B8E8', color: '#1a1a1a' };
  const inputStyle = { width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#1a1a1a', fontSize: '14px' };
  const buttonStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Уведомления</h1>
      </div>

      <div style={tabsStyle}>
        <button style={activeTab === 'list' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('list')}>
          Последние алерты
        </button>
        <button style={activeTab === 'channels' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('channels')}>
          Каналы
        </button>
        <button style={activeTab === 'rules' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('rules')}>
          Правила эскалации
        </button>
      </div>

      {activeTab === 'list' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>История алертов</h3>
          <table style={tableStyle}>
            <thead>
              <tr>
                <th style={thStyle}>Сервис</th>
                <th style={thStyle}>Сообщение</th>
                <th style={thStyle}>Время</th>
                <th style={thStyle}>Статус</th>
                <th style={thStyle}>Канал</th>
              </tr>
            </thead>
            <tbody>
              {alerts.map(alert => (
                <tr key={alert.id}>
                  <td style={tdStyle}>{alert.service}</td>
                  <td style={tdStyle}>{alert.message}</td>
                  <td style={tdStyle}>{alert.time}</td>
                  <td style={tdStyle}>
                    <span style={{ 
                      padding: '2px 8px', 
                      borderRadius: 12, 
                      background: alert.status === 'active' ? '#ef4444' : '#10b981',
                      color: '#fff',
                      fontSize: 12
                    }}>
                      {alert.status === 'active' ? 'Активен' : 'Решён'}
                    </span>
                  </td>
                  <td style={tdStyle}>{alert.channel}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'channels' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Настройки каналов</h3>
          
          <div style={{ marginBottom: 24 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Email</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <input type="checkbox" checked={channels.email.enabled} onChange={(e) => setChannels(prev => ({ ...prev, email: { ...prev.email, enabled: e.target.checked }}))} />
              <span>Включить email уведомления</span>
            </div>
            <input 
              style={inputStyle} 
              placeholder="admin@example.com" 
              value={channels.email.address}
              onChange={(e) => setChannels(prev => ({ ...prev, email: { ...prev.email, address: e.target.value }}))}
            />
          </div>

          <div style={{ marginBottom: 24 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Telegram</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <input type="checkbox" checked={channels.telegram.enabled} onChange={(e) => setChannels(prev => ({ ...prev, telegram: { ...prev.telegram, enabled: e.target.checked }}))} />
              <span>Включить Telegram уведомления</span>
            </div>
            <input 
              style={inputStyle} 
              placeholder="Bot Token" 
              value={channels.telegram.botToken}
              onChange={(e) => setChannels(prev => ({ ...prev, telegram: { ...prev.telegram, botToken: e.target.value }}))}
            />
            <input 
              style={{ ...inputStyle, marginTop: 8 }} 
              placeholder="Chat ID" 
              value={channels.telegram.chatId}
              onChange={(e) => setChannels(prev => ({ ...prev, telegram: { ...prev.telegram, chatId: e.target.value }}))}
            />
          </div>

          <div style={{ marginBottom: 24 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Webhook</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <input type="checkbox" checked={channels.webhook.enabled} onChange={(e) => setChannels(prev => ({ ...prev, webhook: { ...prev.webhook, enabled: e.target.checked }}))} />
              <span>Включить Webhook уведомления</span>
            </div>
            <input 
              style={inputStyle} 
              placeholder="https://hooks.slack.com/..." 
              value={channels.webhook.url}
              onChange={(e) => setChannels(prev => ({ ...prev, webhook: { ...prev.webhook, url: e.target.value }}))}
            />
          </div>

          <button style={buttonStyle}>Сохранить настройки</button>
        </div>
      )}

      {activeTab === 'rules' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Правила эскалации</h3>
          
          {escalationRules.map(rule => (
            <div key={rule.id} style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '12px 0', borderBottom: '1px solid #E5B8E8' }}>
              <input type="checkbox" checked={rule.enabled} onChange={(e) => setEscalationRules(prev => prev.map(r => r.id === rule.id ? { ...r, enabled: e.target.checked } : r))} />
              <span style={{ minWidth: 120, color: '#1a1a1a' }}>
                {rule.condition === 'service_down' ? 'Сервис недоступен' : 'Высокое время отклика'}
              </span>
              <span style={{ minWidth: 60, color: '#6D0475' }}>{rule.duration}</span>
              <span style={{ minWidth: 120, color: '#6D0475' }}>
                {rule.action === 'duplicate_telegram' ? 'Дублировать в Telegram' : 'Эскалировать по Email'}
              </span>
            </div>
          ))}

          <button style={{ ...buttonStyle, marginTop: 16 }}>Добавить правило</button>
        </div>
      )}
    </div>
  );
}

export default Alerts;

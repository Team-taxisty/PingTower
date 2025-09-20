import { useState } from 'react';

function Settings() {
  const [activeTab, setActiveTab] = useState('profile');
  const [profile, setProfile] = useState({
    name: 'Администратор',
    email: 'admin@example.com',
    phone: '+7 (999) 123-45-67'
  });
  const [preferences, setPreferences] = useState({
    timezone: 'Europe/Moscow',
    theme: 'dark',
    language: 'ru',
    notifications: {
      email: true,
      telegram: true,
      webhook: false
    }
  });

  const containerStyle = { maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a' };
  const headerStyle = { marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const tabStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const activeTabStyle = { ...tabStyle, background: '#6D0475', color: '#ffffff' };
  const tabsStyle = { display: 'flex', gap: 8, marginBottom: 24 };
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const inputStyle = { width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#1a1a1a', marginBottom: 12, fontSize: '14px' };
  const buttonStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const checkboxStyle = { marginRight: 8 };

  const timezones = [
    'Europe/Moscow',
    'Europe/London',
    'America/New_York',
    'America/Los_Angeles',
    'Asia/Tokyo',
    'Asia/Shanghai',
    'Australia/Sydney'
  ];

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Настройки</h1>
      </div>

      <div style={tabsStyle}>
        <button style={activeTab === 'profile' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('profile')}>
          Профиль
        </button>
        <button style={activeTab === 'preferences' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('preferences')}>
          Предпочтения
        </button>
        <button style={activeTab === 'security' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('security')}>
          Безопасность
        </button>
      </div>

      {activeTab === 'profile' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Личные данные</h3>
          
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Имя</label>
            <input 
              style={inputStyle} 
              value={profile.name}
              onChange={(e) => setProfile(prev => ({ ...prev, name: e.target.value }))}
            />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Email</label>
            <input 
              style={inputStyle} 
              type="email"
              value={profile.email}
              onChange={(e) => setProfile(prev => ({ ...prev, email: e.target.value }))}
            />
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Телефон</label>
            <input 
              style={inputStyle} 
              value={profile.phone}
              onChange={(e) => setProfile(prev => ({ ...prev, phone: e.target.value }))}
            />
          </div>

          <button style={buttonStyle}>Сохранить изменения</button>
        </div>
      )}

      {activeTab === 'preferences' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Настройки приложения</h3>
          
          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Часовой пояс</label>
            <select 
              style={inputStyle} 
              value={preferences.timezone}
              onChange={(e) => setPreferences(prev => ({ ...prev, timezone: e.target.value }))}
            >
              {timezones.map(tz => (
                <option key={tz} value={tz}>{tz}</option>
              ))}
            </select>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Тема</label>
            <div style={{ display: 'flex', gap: 16 }}>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="theme" 
                  value="dark" 
                  checked={preferences.theme === 'dark'}
                  onChange={(e) => setPreferences(prev => ({ ...prev, theme: e.target.value }))}
                  style={checkboxStyle}
                />
                Тёмная
              </label>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input 
                  type="radio" 
                  name="theme" 
                  value="light" 
                  checked={preferences.theme === 'light'}
                  onChange={(e) => setPreferences(prev => ({ ...prev, theme: e.target.value }))}
                  style={checkboxStyle}
                />
                Светлая
              </label>
            </div>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Язык</label>
            <select 
              style={inputStyle} 
              value={preferences.language}
              onChange={(e) => setPreferences(prev => ({ ...prev, language: e.target.value }))}
            >
              <option value="ru">Русский</option>
              <option value="en">English</option>
            </select>
          </div>

          <div style={{ marginBottom: 16 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Уведомления</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={preferences.notifications.email}
                  onChange={(e) => setPreferences(prev => ({ 
                    ...prev, 
                    notifications: { ...prev.notifications, email: e.target.checked }
                  }))}
                  style={checkboxStyle}
                />
                Email уведомления
              </label>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={preferences.notifications.telegram}
                  onChange={(e) => setPreferences(prev => ({ 
                    ...prev, 
                    notifications: { ...prev.notifications, telegram: e.target.checked }
                  }))}
                  style={checkboxStyle}
                />
                Telegram уведомления
              </label>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input 
                  type="checkbox" 
                  checked={preferences.notifications.webhook}
                  onChange={(e) => setPreferences(prev => ({ 
                    ...prev, 
                    notifications: { ...prev.notifications, webhook: e.target.checked }
                  }))}
                  style={checkboxStyle}
                />
                Webhook уведомления
              </label>
            </div>
          </div>

          <button style={buttonStyle}>Сохранить настройки</button>
        </div>
      )}

      {activeTab === 'security' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Безопасность</h3>
          
          <div style={{ marginBottom: 16 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Смена пароля</h4>
            <input 
              style={inputStyle} 
              type="password"
              placeholder="Текущий пароль"
            />
            <input 
              style={inputStyle} 
              type="password"
              placeholder="Новый пароль"
            />
            <input 
              style={inputStyle} 
              type="password"
              placeholder="Подтвердите новый пароль"
            />
            <button style={buttonStyle}>Изменить пароль</button>
          </div>

          <div style={{ marginBottom: 16 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>Двухфакторная аутентификация</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
              <input type="checkbox" />
              <span>Включить 2FA</span>
            </div>
            <button style={buttonStyle}>Настроить 2FA</button>
          </div>

          <div style={{ marginBottom: 16 }}>
            <h4 style={{ margin: '0 0 12px 0', color: '#6D0475', fontSize: 14, fontWeight: 600 }}>API ключи</h4>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 8, padding: 16, marginBottom: 12, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600 }}>Текущий API ключ</div>
              <div style={{ fontFamily: 'monospace', fontSize: 12, color: '#1a1a1a', wordBreak: 'break-all' }}>
                pk_live_51H...xyz
              </div>
            </div>
            <button style={buttonStyle}>Сгенерировать новый ключ</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default Settings;

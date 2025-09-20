import { useState } from 'react';

function Settings() {
  const [activeTab, setActiveTab] = useState('profile');
  // Добавляем новые состояния для отслеживания наведения кнопок
  const [isProfileSaveHovered, setIsProfileSaveHovered] = useState(false);
  const [isPreferencesSaveHovered, setIsPreferencesSaveHovered] = useState(false);
  const [isChangePasswordHovered, setIsChangePasswordHovered] = useState(false);
  const [isConfigure2FAHovered, setIsConfigure2FAHovered] = useState(false);
  const [isGenerateAPIKeyHovered, setIsGenerateAPIKeyHovered] = useState(false);

  const [profile, setProfile] = useState({
    name: 'Admin',
    email: 'admin@example.com',
    phone: '+7'
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

  // Изменяем containerStyle: удаляем maxWidth и устанавливаем width: '100%'
  const containerStyle = { width: '100%', margin: '0 auto', padding: 24, paddingLeft: 0, color: '#1a1a1a' };
  const headerStyle = { marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const tabStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const activeTabStyle = { ...tabStyle, background: '#6D0475', color: '#ffffff' };
  const tabsStyle = { display: 'flex', gap: 8, marginBottom: 24 };
  // Изменяем cardStyle: убеждаемся, что width: '100%' установлен
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)', width: '96%' };
  // Обновляем inputStyle для корректной обработки ширины с padding и border
  const inputStyle = { width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#1a1a1a', marginBottom: 12, fontSize: '14px', boxSizing: 'border-box' };
  // Создаем selectStyle на основе inputStyle
  const selectStyle = { ...inputStyle, cursor: 'pointer' };
  const buttonStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const checkboxStyle = { marginRight: 8 };

  // Новые стили для кнопок с эффектом наведения
  const saveProfileButtonStyle = {
    ...buttonStyle,
    background: isProfileSaveHovered ? '#8a0593' : '#6D0475',
  };

  const savePreferencesButtonStyle = {
    ...buttonStyle,
    background: isPreferencesSaveHovered ? '#8a0593' : '#6D0475',
  };

  const changePasswordButtonStyle = {
    ...buttonStyle,
    background: isChangePasswordHovered ? '#8a0593' : '#6D0475',
  };

  const configure2FAButtonStyle = {
    ...buttonStyle,
    background: isConfigure2FAHovered ? '#8a0593' : '#6D0475',
  };

  const generateAPIKeyButtonStyle = {
    ...buttonStyle,
    background: isGenerateAPIKeyHovered ? '#8a0593' : '#6D0475',
  };


  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Настройки</h1>
      </div>

      {/* <div style={tabsStyle}>
        <button style={activeTab === 'profile' ? activeTabStyle : tabStyle} onClick={() => setActiveTab('profile')}>
          Профиль
        </button>
      </div> */}

      {activeTab === 'profile' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Личные данные</h3>
          
          <div style={{ marginBottom: 16,  width: '100%'}}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600}}>Имя</label>
            <input 
              style={inputStyle} 
              value={profile.name}
              onChange={(e) => setProfile(prev => ({ ...prev, name: e.target.value }))}
            />
          </div>

          <div style={{ marginBottom: 16,  width: '100%' }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Email</label>
            <input 
              style={inputStyle} 
              type="email"
              value={profile.email}
              onChange={(e) => setProfile(prev => ({ ...prev, email: e.target.value }))}
            />
          </div>

          <div style={{ marginBottom: 16, width: '100%' }}>
            <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Телефон</label>
            <input 
              style={inputStyle} 
              value={profile.phone}
              onChange={(e) => setProfile(prev => ({ ...prev, phone: e.target.value }))}
            />
          </div>

          <button 
            style={saveProfileButtonStyle}
            onMouseEnter={() => setIsProfileSaveHovered(true)}
            onMouseLeave={() => setIsProfileSaveHovered(false)}
          >
            Сохранить изменения
          </button>
        </div>
      )}
    </div>
  );
}

export default Settings;

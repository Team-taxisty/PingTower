import { useState, useEffect } from 'react';
import api from '../utils/api'; // Import the api utility

function Settings() {
  const [activeTab, setActiveTab] = useState('profile');
  const [isProfileSaveHovered, setIsProfileSaveHovered] = useState(false);
  const [profile, setProfile] = useState({
    name: '',
    email: '',
  });
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  // State for notification channels
  const [notificationChannels, setNotificationChannels] = useState([]);
  const [newChannel, setNewChannel] = useState({
    name: '',
    type: 'EMAIL', // Default type
    configuration: '',
    isDefault: false,
  });
  const [isLoadingChannels, setIsLoadingChannels] = useState(false);
  const [isAddingChannel, setIsAddingChannel] = useState(false);
  const [isAddChannelButtonHovered, setIsAddChannelButtonHovered] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      setIsLoading(true);
      try {
        const response = await api('/v1/api/auth/profile');
        if (response.ok) {
          const data = await response.json();
          setProfile({ name: data.username, email: data.email });
        } else {
          console.error('Failed to fetch profile', response.status);
          setMessage({ type: 'error', text: 'Не удалось загрузить данные профиля.' });
        }
      } catch (error) {
        console.error('Error fetching profile:', error);
        setMessage({ type: 'error', text: 'Произошла ошибка при загрузке профиля.' });
      } finally {
        setIsLoading(false);
      }
    };

    const fetchNotificationChannels = async () => {
      setIsLoadingChannels(true);
      try {
        const response = await api('/v1/api/notifications/channels');
        if (response.ok) {
          const data = await response.json();
          setNotificationChannels(data.items || []);
        } else {
          console.error('Failed to fetch notification channels', response.status);
        }
      } catch (error) {
        console.error('Error fetching notification channels:', error);
      } finally {
        setIsLoadingChannels(false);
      }
    };

    if (activeTab === 'profile') {
      fetchProfile();
    } else if (activeTab === 'notifications') {
      fetchNotificationChannels();
    }
  }, [activeTab]);

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setMessage(null);
    setIsLoading(true);
    try {
      const response = await api('/v1/api/auth/profile', {
        method: 'PATCH', // Assuming PATCH for profile update
        body: JSON.stringify({ username: profile.name, email: profile.email }),
      });

      if (response.ok) {
        setMessage({ type: 'success', text: 'Профиль успешно обновлен!' });
        localStorage.setItem('username', profile.name);
        localStorage.setItem('email', profile.email);
      } else {
        const errorData = await response.json();
        setMessage({ type: 'error', text: errorData.message || 'Ошибка при обновлении профиля.' });
      }
    } catch (error) {
      console.error('API Error:', error);
      setMessage({ type: 'error', text: 'Произошла ошибка при сохранении профиля.' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateChannel = async (e) => {
    e.preventDefault();
    setMessage(null);
    setIsAddingChannel(true);
    try {
      const response = await api('/v1/api/notifications/channels', {
        method: 'POST',
        body: JSON.stringify(newChannel),
      });

      if (response.ok) {
        const createdChannel = await response.json();
        setNotificationChannels(prev => [...prev, createdChannel]);
        setNewChannel({ name: '', type: 'EMAIL', configuration: '', isDefault: false }); // Reset form
        setMessage({ type: 'success', text: 'Канал уведомлений успешно добавлен!' });
      } else {
        const errorData = await response.json();
        setMessage({ type: 'error', text: errorData.message || 'Ошибка при добавлении канала уведомлений.' });
      }
    } catch (error) {
      console.error('API Error:', error);
      setMessage({ type: 'error', text: 'Произошла ошибка при добавлении канала уведомлений.' });
    } finally {
      setIsAddingChannel(false);
    }
  };

  const containerStyle = { maxWidth: 1120, margin: '0 auto', padding: 24, color: '#1a1a1a' };
  const headerStyle = { marginBottom: 24 };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const tabStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#6D0475', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };
  const activeTabStyle = { ...tabStyle, background: '#6D0475', color: '#ffffff' };
  const tabsStyle = { display: 'flex', gap: 8, marginBottom: 24 };
  const cardStyle = { background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, marginBottom: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' };
  const inputStyle = { width: '100%', padding: '10px 12px', borderRadius: 8, border: '1px solid #E5B8E8', background: '#ffffff', color: '#1a1a1a', marginBottom: 12, fontSize: '14px', boxSizing: 'border-box' };
  const selectStyle = { ...inputStyle, cursor: 'pointer' }; // New style for select
  const buttonStyle = { padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease' };

  const saveProfileButtonStyle = {
    ...buttonStyle,
    background: isProfileSaveHovered ? '#8a0593' : '#6D0475',
    cursor: isLoading ? 'not-allowed' : 'pointer',
    opacity: isLoading ? 0.7 : 1,
  };

  const addChannelButtonStyle = {
    ...buttonStyle,
    background: isAddChannelButtonHovered ? '#8a0593' : '#6D0475',
    cursor: isAddingChannel ? 'not-allowed' : 'pointer',
    opacity: isAddingChannel ? 0.7 : 1,
  };

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1 style={titleStyle}>Настройки</h1>
      </div>

      <div style={tabsStyle}>
        <button 
          style={activeTab === 'profile' ? activeTabStyle : tabStyle} 
          onClick={() => setActiveTab('profile')}>
          Профиль
        </button>
        <button 
          style={activeTab === 'notifications' ? activeTabStyle : tabStyle} 
          onClick={() => setActiveTab('notifications')}>
          Уведомления
        </button>
      </div>

      {activeTab === 'profile' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Личные данные</h3>
          
          <form onSubmit={handleProfileSave}>
            <div style={{ marginBottom: 16,  width: '100%'}}>
              <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600}}>Имя пользователя</label>
              <input 
                style={inputStyle} 
                value={profile.name}
                onChange={(e) => setProfile(prev => ({ ...prev, name: e.target.value }))}
                required 
                disabled={isLoading}
              />
            </div>

            <div style={{ marginBottom: 16,  width: '100%' }}>
              <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600 }}>Email</label>
              <input 
                style={inputStyle} 
                type="email"
                value={profile.email}
                onChange={(e) => setProfile(prev => ({ ...prev, email: e.target.value }))}
                required 
                disabled={isLoading}
              />
            </div>

            {message && (
              <p style={{ color: message.type === 'success' ? 'green' : 'red', textAlign: 'center', marginBottom: '15px' }}>
                {message.text}
              </p>
            )}

            <button 
              type="submit"
              style={saveProfileButtonStyle}
              onMouseEnter={() => setIsProfileSaveHovered(true)}
              onMouseLeave={() => setIsProfileSaveHovered(false)}
              disabled={isLoading}
            >
              {isLoading ? 'Сохранение...' : 'Сохранить изменения'}
            </button>
          </form>
        </div>
      )}

      {activeTab === 'notifications' && (
        <div style={cardStyle}>
          <h3 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Каналы уведомлений</h3>
          
          {isLoadingChannels ? (
            <p>Загрузка каналов уведомлений...</p>
          ) : (
            notificationChannels.length > 0 ? (
              <div style={{ display: 'grid', gap: '10px', marginBottom: '20px' }}>
                {notificationChannels.map(channel => (
                  <div key={channel.id} style={{ padding: '12px 14px', border: '1px solid #E5B8E8', borderRadius: '12px', background: '#ffffff', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, color: '#1a1a1a' }}>{channel.name} ({channel.type})</div>
                      <div style={{ fontSize: '12px', color: '#6D0475' }}>{channel.configuration}</div>
                    </div>
                    {/* Add actions like edit/delete later if API supports */}
                  </div>
                ))}
              </div>
            ) : (
              <p>Каналы уведомлений не найдены.</p>
            )
          )}

          <h4 style={{ margin: '0 0 16px 0', color: '#6D0475', fontSize: 16, fontWeight: 600 }}>Добавить новый канал</h4>
          <form onSubmit={handleCreateChannel}>
            <div style={{ marginBottom: 16, width: '100%' }}>
              <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600}}>Название</label>
              <input 
                style={inputStyle} 
                value={newChannel.name}
                onChange={(e) => setNewChannel(prev => ({ ...prev, name: e.target.value }))}
                required 
                disabled={isAddingChannel}
              />
            </div>
            <div style={{ marginBottom: 16, width: '100%' }}>
              <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600}}>Тип</label>
              <select 
                style={selectStyle} 
                value={newChannel.type}
                onChange={(e) => setNewChannel(prev => ({ ...prev, type: e.target.value }))}
                required 
                disabled={isAddingChannel}
              >
                <option value="EMAIL">Email</option>
                <option value="TELEGRAM">Telegram</option>
                <option value="WEBHOOK">Webhook</option>
              </select>
            </div>
            <div style={{ marginBottom: 16, width: '100%' }}>
              <label style={{ display: 'block', marginBottom: 4, color: '#6D0475', fontSize: 12, fontWeight: 600}}>Конфигурация (например, email адрес или URL вебхука)</label>
              <input 
                style={inputStyle} 
                value={newChannel.configuration}
                onChange={(e) => setNewChannel(prev => ({ ...prev, configuration: e.target.value }))}
                required 
                disabled={isAddingChannel}
              />
            </div>
            <div style={{ marginBottom: 16, width: '100%', display: 'flex', alignItems: 'center' }}>
              <input 
                type="checkbox"
                checked={newChannel.isDefault}
                onChange={(e) => setNewChannel(prev => ({ ...prev, isDefault: e.target.checked }))}
                disabled={isAddingChannel}
                style={{ marginRight: '8px' }}
              />
              <label style={{ color: '#6D0475', fontSize: 14, fontWeight: 500 }}>Сделать по умолчанию</label>
            </div>

            {message && activeTab === 'notifications' && (
              <p style={{ color: message.type === 'success' ? 'green' : 'red', textAlign: 'center', marginBottom: '15px' }}>
                {message.text}
              </p>
            )}

            <button 
              type="submit"
              style={addChannelButtonStyle}
              onMouseEnter={() => setIsAddChannelButtonHovered(true)}
              onMouseLeave={() => setIsAddChannelButtonHovered(false)}
              disabled={isAddingChannel}
            >
              {isAddingChannel ? 'Добавление...' : 'Добавить канал'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default Settings;

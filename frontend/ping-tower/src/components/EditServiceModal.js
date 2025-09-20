import { useState } from 'react';

function EditServiceModal({ service, open, onClose, onSubmit }) {
  const [formData, setFormData] = useState({
    name: service?.name || '',
    url: service?.url || '',
    checkInterval: '60s',
    checkType: 'http',
    timeout: '30s'
  });

  if (!open || !service) return null;

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({ ...service, ...formData });
    onClose();
  }

  const overlay = {
    position: 'fixed', inset: 0, background: 'rgba(109, 4, 117, 0.3)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    padding: '20px'
  };
  const modal = {
    width: '100%', maxWidth: 500, background: '#ffffff', borderRadius: '12px',
    border: '1px solid #E5B8E8', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px',
    boxShadow: '0 4px 12px rgba(109, 4, 117, 0.2)'
  };
  const label = { fontSize: 12, color: '#6D0475', marginBottom: 4, display: 'block', fontWeight: 600 };
  const input = {
    width: '100%', borderRadius: '8px', border: '1px solid #E5B8E8', padding: '10px 12px',
    background: '#ffffff', color: '#1a1a1a', marginBottom: 12, fontSize: '14px'
  };
  const select = { ...input, cursor: 'pointer' };
  const actions = { display: 'flex', gap: '12px', justifyContent: 'flex-end' };
  const btn = {
    padding: '10px 16px', borderRadius: '8px', border: '1px solid #E5B8E8', cursor: 'pointer',
    background: '#ffffff', color: '#6D0475', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
  };
  const saveBtn = { ...btn, background: '#6D0475', color: '#ffffff', border: '1px solid #6D0475' };

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontWeight: 600, fontSize: 16, color: '#6D0475' }}>Редактировать мониторинг</div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          <div>
            <label style={label} htmlFor="name">Название</label>
            <input 
              style={input} 
              id="name" 
              name="name" 
              type="text" 
              value={formData.name}
              onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
              required 
            />
          </div>
          <div>
            <label style={label} htmlFor="url">URL</label>
            <input 
              style={input} 
              id="url" 
              name="url" 
              type="url" 
              value={formData.url}
              onChange={(e) => setFormData(prev => ({ ...prev, url: e.target.value }))}
            />
          </div>
          <div>
            <label style={label} htmlFor="checkInterval">Интервал проверки</label>
            <select 
              style={select} 
              id="checkInterval" 
              value={formData.checkInterval}
              onChange={(e) => setFormData(prev => ({ ...prev, checkInterval: e.target.value }))}
            >
              <option value="30s">30 секунд</option>
              <option value="60s">1 минута</option>
              <option value="300s">5 минут</option>
              <option value="600s">10 минут</option>
            </select>
          </div>
          <div>
            <label style={label} htmlFor="checkType">Тип проверки</label>
            <select 
              style={select} 
              id="checkType" 
              value={formData.checkType}
              onChange={(e) => setFormData(prev => ({ ...prev, checkType: e.target.value }))}
            >
              <option value="http">HTTP</option>
              <option value="https">HTTPS</option>
              <option value="ping">Ping</option>
              <option value="tcp">TCP</option>
            </select>
          </div>
          <div>
            <label style={label} htmlFor="timeout">Таймаут</label>
            <select 
              style={select} 
              id="timeout" 
              value={formData.timeout}
              onChange={(e) => setFormData(prev => ({ ...prev, timeout: e.target.value }))}
            >
              <option value="10s">10 секунд</option>
              <option value="30s">30 секунд</option>
              <option value="60s">1 минута</option>
            </select>
          </div>
          <div style={actions}>
            <button type="button" style={btn} onClick={onClose}>Отмена</button>
            <button type="submit" style={saveBtn}>Сохранить</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditServiceModal;

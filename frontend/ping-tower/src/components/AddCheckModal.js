import React, { useState } from 'react';

function AddCheckModal({ open, onClose, onSubmit }) {
  const [isCancelButtonHovered, setIsCancelButtonHovered] = useState(false);
  const [isAddButtonHovered, setIsAddButtonHovered] = useState(false);
  const [serviceType, setServiceType] = useState('PING'); // Default to PING

  if (!open) return null;

  function handleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    const basePayload = {
      name: String(formData.get('name') || '').trim(),
      description: String(formData.get('description') || '').trim(),
      url: String(formData.get('url') || '').trim(),
      serviceType: serviceType,
      enabled: true, // Всегда true при создании
      checkIntervalMinutes: parseInt(formData.get('checkIntervalMinutes') || '5', 10),
      timeoutSeconds: parseInt(formData.get('timeoutSeconds') || '30', 10),
    };

    let specificPayload = {};

    if (serviceType === 'API') {
      specificPayload = {
        httpMethod: String(formData.get('httpMethod') || 'GET').trim(),
        // headers: JSON.parse(formData.get('headers') || '{}'), // Если нужно, можно добавить ввод для заголовков
        expectedStatusCode: parseInt(formData.get('expectedStatusCode') || '200', 10),
        expectedResponseBody: String(formData.get('expectedResponseBody') || '').trim(),
      };
    }

    onSubmit({ ...basePayload, ...specificPayload });
  }

  const overlay = {
    position: 'fixed', inset: 0, background: 'rgba(109, 4, 117, 0.3)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    padding: '20px'
  };
  const modal = {
    width: '100%', maxWidth: 440, background: '#ffffff', borderRadius: '12px',
    border: '1px solid #E5B8E8', padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px',
    boxShadow: '0 4px 12px rgba(109, 4, 117, 0.2)'
  };
  const label = { fontSize: 12, color: '#6D0475', fontWeight: 600 };
  const input = {
    width: '100%', borderRadius: '8px', border: '1px solid #E5B8E8', padding: '10px 12px',
    fontSize: '14px', color: '#1a1a1a', background: '#ffffff', boxSizing: 'border-box', marginBottom: '12px'
  };
  const selectStyle = {
    width: '100%', borderRadius: '8px', border: '1px solid #E5B8E8', padding: '10px 12px',
    fontSize: '14px', color: '#1a1a1a', background: '#ffffff', boxSizing: 'border-box', marginBottom: '12px'
  };
  const actions = { display: 'flex', gap: '12px', justifyContent: 'flex-end' };
  const btn = {
    padding: '10px 16px', borderRadius: '8px', border: '1px solid #E5B8E8', cursor: 'pointer',
    fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
  };

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontWeight: 600, fontSize: 16, color: '#6D0475' }}>Новый сервис мониторинга</div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div style={{ flexDirection: 'column', gap: 6, display: 'flex' }}>
            <label style={label} htmlFor="name">Название</label>
            <input style={input} id="name" name="name" type="text" required />
          </div>
          <div style={{ flexDirection: 'column', gap: 6, display: 'flex' }}>
            <label style={label} htmlFor="description">Описание</label>
            <input style={input} id="description" name="description" type="text" />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="url">URL</label>
            <input style={input} id="url" name="url" type="text" required />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="serviceType">Тип сервиса</label>
            <select style={selectStyle} id="serviceType" name="serviceType" required value={serviceType} onChange={(e) => setServiceType(e.target.value)}>
              <option value="PING">PING</option>
              <option value="API">API</option>
            </select>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="checkIntervalMinutes">Интервал проверки (минуты)</label>
            <input style={input} id="checkIntervalMinutes" name="checkIntervalMinutes" type="number" defaultValue="5" min="1" required />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="timeoutSeconds">Таймаут (секунды)</label>
            <input style={input} id="timeoutSeconds" name="timeoutSeconds" type="number" defaultValue="30" min="1" required />
          </div>

          {serviceType === 'API' && (
            <>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                <label style={label} htmlFor="httpMethod">HTTP Метод</label>
                <select style={selectStyle} id="httpMethod" name="httpMethod" defaultValue="GET">
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                </select>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                <label style={label} htmlFor="expectedStatusCode">Ожидаемый код ответа</label>
                <input style={input} id="expectedStatusCode" name="expectedStatusCode" type="number" defaultValue="200" />
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                <label style={label} htmlFor="expectedResponseBody">Ожидаемое тело ответа (JSON)</label>
                <input style={input} id="expectedResponseBody" name="expectedResponseBody" type="text" placeholder="{'status': 'ok'}" />
              </div>
              {/* Headers можно добавить позже, если потребуется UI для них */}
            </>
          )}

          <div style={actions}>
            <button
              type="button"
              style={isCancelButtonHovered ? { ...btn, background: '#fef2f2', color: '#991b1b' } : { ...btn, background: '#ffffff', color: '#6D0475' }}
              onMouseEnter={() => setIsCancelButtonHovered(true)}
              onMouseLeave={() => setIsCancelButtonHovered(false)}
              onClick={onClose}
            >
              Отмена
            </button>
            <button
              type="submit"
              style={isAddButtonHovered ? { ...btn, background: '#8a0593', color: '#ffffff', border: '1px solid #8a0593' } : { ...btn, background: '#6D0475', color: '#ffffff', border: '1px solid #6D0475' }}
              onMouseEnter={() => setIsAddButtonHovered(true)}
              onMouseLeave={() => setIsAddButtonHovered(false)}
            >
              Добавить
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddCheckModal;
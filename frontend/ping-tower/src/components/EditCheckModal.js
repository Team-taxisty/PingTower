import { useState, useEffect } from 'react';

function EditCheckModal({ check, open, onClose, onSubmit }) {
  const [formData, setFormData] = useState({
    name: check?.name || '',
    description: check?.description || '',
    url: check?.url || '',
    serviceType: check?.serviceType || 'PING',
    enabled: check?.enabled ?? true,
    checkIntervalMinutes: check?.checkIntervalMinutes || 5,
    timeoutSeconds: check?.timeoutSeconds || 30,
    httpMethod: check?.httpMethod || 'GET',
    expectedStatusCode: check?.expectedStatusCode || 200,
    expectedResponseBody: check?.expectedResponseBody || '',
    // headers: check?.headers || {}, // Если нужно, можно добавить ввод для заголовков
  });

  useEffect(() => {
    if (check) {
      setFormData({
        name: check.name || '',
        description: check.description || '',
        url: check.url || '',
        serviceType: check.serviceType || 'PING',
        enabled: check.enabled ?? true,
        checkIntervalMinutes: check.checkIntervalMinutes || 5,
        timeoutSeconds: check.timeoutSeconds || 30,
        httpMethod: check.httpMethod || 'GET',
        expectedStatusCode: check.expectedStatusCode || 200,
        expectedResponseBody: check.expectedResponseBody || '',
        // headers: check.headers || {},
      });
    }
  }, [check]);

  const [isCancelButtonHovered, setIsCancelButtonHovered] = useState(false);
  const [isSaveButtonHovered, setIsSaveButtonHovered] = useState(false);

  if (!open || !check) return null;

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit({ ...check, ...formData });
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
    background: '#ffffff', color: '#1a1a1a', marginBottom: 12, fontSize: '14px', boxSizing: 'border-box'
  };
  const select = { ...input, cursor: 'pointer', boxSizing: 'border-box' };
  const actions = { display: 'flex', gap: '12px', justifyContent: 'flex-end' };
  const btn = {
    padding: '10px 16px', borderRadius: '8px', border: '1px solid #E5B8E8', cursor: 'pointer',
    background: '#ffffff', color: '#6D0475', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
  };
  const saveBtn = { ...btn, background: '#6D0475', color: '#ffffff', border: '1px solid #6D0475' };

  const cancelButtonStyles = {
    ...btn,
    background: isCancelButtonHovered ? '#f0f0f0' : '#ffffff',
    color: isCancelButtonHovered ? '#4a034f' : '#6D0475',
    border: '1px solid #E5B8E8',
  };

  const saveButtonStyles = {
    ...saveBtn,
    background: isSaveButtonHovered ? '#8a0593' : '#6D0475',
  };

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontWeight: 600, fontSize: 16, color: '#6D0475' }}>Редактировать сервис</div>
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
            <label style={label} htmlFor="description">Описание</label>
            <input
              style={input}
              id="description"
              name="description"
              type="text"
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
            />
          </div>
          <div>
            <label style={label} htmlFor="url">URL</label>
            <input
              style={input}
              id="url"
              name="url"
              type="text"
              value={formData.url}
              onChange={(e) => setFormData(prev => ({ ...prev, url: e.target.value }))}
              required
            />
          </div>
          <div>
            <label style={label} htmlFor="serviceType">Тип сервиса</label>
            <select
              style={select}
              id="serviceType"
              name="serviceType"
              value={formData.serviceType}
              onChange={(e) => setFormData(prev => ({ ...prev, serviceType: e.target.value }))}
              required
            >
              <option value="PING">PING</option>
              <option value="API">API</option>
            </select>
          </div>
          <div>
            <label style={label} htmlFor="enabled">Включен</label>
            <input
              type="checkbox"
              id="enabled"
              name="enabled"
              checked={formData.enabled}
              onChange={(e) => setFormData(prev => ({ ...prev, enabled: e.target.checked }))}
              style={{ marginLeft: '10px' }}
            />
          </div>
          <div>
            <label style={label} htmlFor="checkIntervalMinutes">Интервал проверки (минуты)</label>
            <input
              style={input}
              id="checkIntervalMinutes"
              name="checkIntervalMinutes"
              type="number"
              value={formData.checkIntervalMinutes}
              onChange={(e) => setFormData(prev => ({ ...prev, checkIntervalMinutes: parseInt(e.target.value, 10) }))}
              min="1"
              required
            />
          </div>
          <div>
            <label style={label} htmlFor="timeoutSeconds">Таймаут (секунды)</label>
            <input
              style={input}
              id="timeoutSeconds"
              name="timeoutSeconds"
              type="number"
              value={formData.timeoutSeconds}
              onChange={(e) => setFormData(prev => ({ ...prev, timeoutSeconds: parseInt(e.target.value, 10) }))}
              min="1"
              required
            />
          </div>

          {formData.serviceType === 'API' && (
            <>
              <div>
                <label style={label} htmlFor="httpMethod">HTTP Метод</label>
                <select
                  style={select}
                  id="httpMethod"
                  name="httpMethod"
                  value={formData.httpMethod}
                  onChange={(e) => setFormData(prev => ({ ...prev, httpMethod: e.target.value }))}
                >
                  <option value="GET">GET</option>
                  <option value="POST">POST</option>
                  <option value="PUT">PUT</option>
                  <option value="DELETE">DELETE</option>
                </select>
              </div>
              <div>
                <label style={label} htmlFor="expectedStatusCode">Ожидаемый код ответа</label>
                <input
                  style={input}
                  id="expectedStatusCode"
                  name="expectedStatusCode"
                  type="number"
                  value={formData.expectedStatusCode}
                  onChange={(e) => setFormData(prev => ({ ...prev, expectedStatusCode: parseInt(e.target.value, 10) }))}
                />
              </div>
              <div>
                <label style={label} htmlFor="expectedResponseBody">Ожидаемое тело ответа (JSON)</label>
                <input
                  style={input}
                  id="expectedResponseBody"
                  name="expectedResponseBody"
                  type="text"
                  value={formData.expectedResponseBody}
                  onChange={(e) => setFormData(prev => ({ ...prev, expectedResponseBody: e.target.value }))}
                  placeholder="{'status': 'ok'}"
                />
              </div>
              {/* Headers можно добавить позже, если потребуется UI для них */}
            </>
          )}

          <div style={actions}>
            <button
              type="button"
              style={cancelButtonStyles}
              onClick={onClose}
              onMouseEnter={() => setIsCancelButtonHovered(true)}
              onMouseLeave={() => setIsCancelButtonHovered(false)}
            >
              Отмена
            </button>
            <button
              type="submit"
              style={saveButtonStyles}
              onMouseEnter={() => setIsSaveButtonHovered(true)}
              onMouseLeave={() => setIsSaveButtonHovered(false)}
            >
              Сохранить
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EditCheckModal;

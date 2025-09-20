import { useState, useEffect } from 'react'; // Import useEffect for initial state synchronization

function EditCheckModal({ check, open, onClose, onSubmit }) { // Renamed from EditServiceModal and service prop
  const [formData, setFormData] = useState({
    name: check?.name || '',
    type: check?.type || 'HTTP', // Default to HTTP
    target: check?.target || '',
    interval_sec: check?.interval_sec || 60, // Default to 60 seconds
  });

  useEffect(() => {
    if (check) {
      setFormData({
        name: check.name || '',
        type: check.type || 'HTTP',
        target: check.target || '',
        interval_sec: check.interval_sec || 60,
      });
    }
  }, [check]);

  // Добавляем новые состояния для отслеживания наведения кнопок
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
  // Обновляем inputStyle для корректной обработки ширины с padding и border
  const input = {
    width: '100%', borderRadius: '8px', border: '1px solid #E5B8E8', padding: '10px 12px',
    background: '#ffffff', color: '#1a1a1a', marginBottom: 12, fontSize: '14px', boxSizing: 'border-box'
  };
  // Создаем selectStyle на основе inputStyle
  const select = { ...input, cursor: 'pointer', boxSizing: 'border-box' };
  const actions = { display: 'flex', gap: '12px', justifyContent: 'flex-end' };
  const btn = {
    padding: '10px 16px', borderRadius: '8px', border: '1px solid #E5B8E8', cursor: 'pointer',
    background: '#ffffff', color: '#6D0475', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
  };
  const saveBtn = { ...btn, background: '#6D0475', color: '#ffffff', border: '1px solid #6D0475' };

  // Новые стили для кнопок с эффектом наведения
  const cancelButtonStyles = {
    ...btn,
    background: isCancelButtonHovered ? '#f0f0f0' : '#ffffff', // Светлее на наведении
    color: isCancelButtonHovered ? '#4a034f' : '#6D0475',     // Темнее текст
    border: '1px solid #E5B8E8',
  };

  const saveButtonStyles = {
    ...saveBtn,
    background: isSaveButtonHovered ? '#8a0593' : '#6D0475', // Темнее на наведении
  };

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontWeight: 600, fontSize: 16, color: '#6D0475' }}>Редактировать проверку</div>
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
            <label style={label} htmlFor="type">Тип проверки</label>
            <select 
              style={select} 
              id="type" 
              name="type" 
              value={formData.type}
              onChange={(e) => setFormData(prev => ({ ...prev, type: e.target.value }))}
            >
              <option value="HTTP">HTTP</option>
              <option value="HTTPS">HTTPS</option>
              <option value="API_JSON">API_JSON</option>
              <option value="API_XML">API_XML</option>
            </select>
          </div>
          <div>
            <label style={label} htmlFor="target">Цель (URL)</label>
            <input 
              style={input} 
              id="target" 
              name="target" 
              type="text" 
              value={formData.target}
              onChange={(e) => setFormData(prev => ({ ...prev, target: e.target.value }))}
            />
          </div>
          <div>
            <label style={label} htmlFor="interval_sec">Интервал проверки</label>
            <select 
              style={select} 
              id="interval_sec" 
              name="interval_sec" 
              value={formData.interval_sec}
              onChange={(e) => setFormData(prev => ({ ...prev, interval_sec: parseInt(e.target.value) }))}
            >
              <option value={15}>15 секунд</option>
              <option value={30}>30 секунд</option>
              <option value={60}>1 минута</option>
              <option value={300}>5 минут</option>
              <option value={600}>10 минут</option>
            </select>
          </div>
          <div style={actions}>
            {/* Кнопка "Отмена" */}
            <button
              type="button"
              style={cancelButtonStyles} // Используем новый стиль
              onClick={onClose}
              onMouseEnter={() => setIsCancelButtonHovered(true)} // Обрабатываем наведение
              onMouseLeave={() => setIsCancelButtonHovered(false)} // Обрабатываем уход курсора
            >
              Отмена
            </button>
            {/* Кнопка "Сохранить" */}
            <button
              type="submit"
              style={saveButtonStyles} // Используем новый стиль
              onMouseEnter={() => setIsSaveButtonHovered(true)} // Обрабатываем наведение
              onMouseLeave={() => setIsSaveButtonHovered(false)} // Обрабатываем уход курсора
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

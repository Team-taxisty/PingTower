import React, { useState } from 'react'; // Import useState for hover effects

function AddCheckModal({ open, onClose, onSubmit }) { // Renamed from AddServiceModal
  const [isCancelButtonHovered, setIsCancelButtonHovered] = useState(false);
  const [isAddButtonHovered, setIsAddButtonHovered] = useState(false);

  if (!open) return null;

  function handleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const payload = {
      name: String(formData.get('name') || '').trim(),
      type: String(formData.get('type') || '').trim(),
      target: String(formData.get('target') || '').trim(),
    };
    onSubmit(payload);
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
    fontSize: '14px', color: '#1a1a1a', background: '#ffffff', boxSizing: 'border-box'
  };
  const selectStyle = {
    width: '100%', borderRadius: '8px', border: '1px solid #E5B8E8', padding: '10px 12px',
    fontSize: '14px', color: '#1a1a1a', background: '#ffffff', boxSizing: 'border-box'
  };
  const actions = { display: 'flex', gap: '12px', justifyContent: 'flex-end' };
  const btn = {
    padding: '10px 16px', borderRadius: '8px', border: '1px solid #E5B8E8', cursor: 'pointer',
    fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
  };

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontWeight: 600, fontSize: 16, color: '#6D0475' }}>Новая проверка</div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div style={{ flexDirection: 'column', gap: 6, display: 'flex' }}>
            <label style={label} htmlFor="name">Название</label>
            <input style={input} id="name" name="name" type="text" required />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="type">Тип</label>
            <select style={selectStyle} id="type" name="type" required defaultValue="HTTP">
              <option value="HTTP">HTTP</option>
              <option value="HTTPS">HTTPS</option>
              <option value="API_JSON">API_JSON</option>
              <option value="API_XML">API_XML</option>
            </select>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="target">Цель (URL)</label>
            <input style={input} id="target" name="target" type="text" required />
          </div>
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



function AddServiceModal({ open, onClose, onSubmit }) {
  if (!open) return null;

  function handleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const payload = {
      name: String(formData.get('name') || '').trim(),
      url: String(formData.get('url') || '').trim(),
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
    width: '97%', borderRadius: '8px', border: '1px solid #E5B8E8', padding: '10px 12px', paddingRight: '0',
    fontSize: '14px', color: '#1a1a1a', background: '#ffffff'
  };
  const actions = { display: 'flex', gap: '12px', justifyContent: 'flex-end' };
  const btn = {
    padding: '10px 16px', borderRadius: '8px', border: '1px solid #E5B8E8', cursor: 'pointer',
    fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
  };

  return (
    <div style={overlay} onClick={onClose}>
      <div style={modal} onClick={(e) => e.stopPropagation()}>
        <div style={{ fontWeight: 600, fontSize: 16, color: '#6D0475' }}>Новый сервис</div>
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <div style={{ flexDirection: 'column', gap: 6, display: 'flex' }}>
            <label style={label} htmlFor="name">Название</label>
            <input style={input} id="name" name="name" type="text" required />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            <label style={label} htmlFor="url">URL</label>
            <input style={input} id="url" name="url" type="url" required />
          </div>
          <div style={actions}>
            <button type="button" style={{ ...btn, background: '#ffffff', color: '#6D0475' }} onClick={onClose}>Отмена</button>
            <button type="submit" style={{ ...btn, background: '#6D0475', color: '#ffffff', border: '1px solid #6D0475' }}>Добавить</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddServiceModal;



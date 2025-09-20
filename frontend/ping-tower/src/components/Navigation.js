function Navigation({ currentPage, onNavigate }) {
  const navStyle = {
    background: '#ffffff',
    border: '1px solid #E5B8E8',
    borderRadius: 12,
    padding: 16,
    marginBottom: 20,
    display: 'flex',
    alignItems: 'center',
    gap: 16,
    flexWrap: 'wrap',
    boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)'
  };

  const logoStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
    marginRight: 'auto'
  };

  const logoPlaceholderStyle = {
    width: 32,
    height: 32,
    borderRadius: 8,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#ffffff',
    fontSize: 16,
    fontWeight: 700
  };

  const navItemStyle = (isActive) => ({
    padding: '8px 16px',
    borderRadius: 8,
    border: '1px solid #E5B8E8',
    background: isActive ? '#6D0475' : 'transparent',
    color: isActive ? '#ffffff' : '#6D0475',
    cursor: 'pointer',
    textDecoration: 'none',
    fontSize: 14,
    fontWeight: isActive ? 600 : 500,
    transition: 'all 0.2s ease'
  });

  const navItems = [
    { id: 'dashboard', label: 'Dashboard' },
    { id: 'alerts', label: 'Уведомления' },
    { id: 'reports', label: 'Отчёты' },
    { id: 'settings', label: 'Настройки' }
  ];

  return (
    <nav style={navStyle}>
      <div style={logoStyle}>
        <div style={logoPlaceholderStyle}>
          <img
            src="/images/logoHeader.png"
            alt="PingTower logo"
            style={{width: 400, height: 48, borderRadius: 10, paddingLeft: 80}}
          />
        </div>
      </div>
      {navItems.map(item => (
        <button
          key={item.id}
          style={navItemStyle(currentPage === item.id)}
          onClick={() => onNavigate(item.id)}
        >
          {item.label}
        </button>
      ))}
    </nav>
  );
}

export default Navigation;

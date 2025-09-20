import { useMemo, useState } from 'react';
import Navigation from './components/Navigation';
import Filters from './components/Filters';
import ServiceList from './components/ServiceList';
import AddServiceModal from './components/AddServiceModal';
import EditServiceModal from './components/EditServiceModal';
import AvailabilityLineChart from './components/charts/AvailabilityLineChart';
import StatusPieChart from './components/charts/StatusPieChart';
import ServiceDetail from './pages/ServiceDetail';
import Alerts from './pages/Alerts';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import Auth from './pages/Auth';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentPage, setCurrentPage] = useState('dashboard');
  const [selectedService, setSelectedService] = useState(null);
  const [services, setServices] = useState(() => [
    { id: '1', name: 'API Gateway', url: 'https://api.example.com/health', status: 'ok' },
    { id: '2', name: 'Auth Service', url: 'https://auth.example.com/health', status: 'degraded' },
    { id: '3', name: 'Payments', url: 'https://payments.example.com/health', status: 'down' },
  ]);
  const [filter, setFilter] = useState('all');
  const availabilityPoints = [
    { t: '00:00', v: 100 }, { t: '02:00', v: 99.8 }, { t: '04:00', v: 99.9 },
    { t: '06:00', v: 99.7 }, { t: '08:00', v: 99.9 }, { t: '10:00', v: 100 },
    { t: '12:00', v: 99.6 }, { t: '14:00', v: 99.9 }, { t: '16:00', v: 100 },
    { t: '18:00', v: 99.8 }, { t: '20:00', v: 99.9 }, { t: '22:00', v: 100 }
  ];
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingService, setEditingService] = useState(null);

  const filteredServices = useMemo(() => {
    if (filter === 'all') return services;
    return services.filter((s) => s.status === filter);
  }, [services, filter]);

  function handleAddService(payload) {
    const newService = {
      id: String(Date.now()),
      name: payload.name,
      url: payload.url || '',
      status: 'ok'
    };
    setServices((prev) => [newService, ...prev]);
    setIsModalOpen(false);
  }

  function handleEditService(service) {
    setEditingService(service);
    setIsEditModalOpen(true);
  }

  function handleUpdateService(updatedService) {
    setServices((prev) => prev.map(s => s.id === updatedService.id ? updatedService : s));
    setIsEditModalOpen(false);
    setEditingService(null);
  }

  function handleDeleteService(serviceId) {
    if (window.confirm('Удалить сервис?')) {
      setServices((prev) => prev.filter(s => s.id !== serviceId));
      if (selectedService && selectedService.id === serviceId) {
        setSelectedService(null);
        setCurrentPage('dashboard');
      }
    }
  }

  function handleServiceClick(service) {
    setSelectedService(service);
    setCurrentPage('service-detail');
  }

  function handleBackToDashboard() {
    setSelectedService(null);
    setCurrentPage('dashboard');
  }

  function handleAuthSuccess() {
    setIsLoggedIn(true);
    setCurrentPage('dashboard');
  }

  const pageStyle = { maxWidth: 1120, margin: '0 auto', padding: 24, display: 'flex', flexDirection: 'column', gap: 20, color: '#1a1a1a' };
  const headerStyle = { display: 'flex', alignItems: 'center', justifyContent: 'space-between' };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const addBtn = { 
    padding: '10px 14px', 
    borderRadius: 10, 
    border: '1px solid #6D0475', 
    cursor: 'pointer', 
    background: '#6D0475', 
    color: '#ffffff',
    fontSize: 14,
    fontWeight: 500,
    transition: 'all 0.2s ease'
  };

  function renderPage() {
    switch (currentPage) {
      case 'dashboard':
        return (
          <>
            <div style={headerStyle}>
              <button style={addBtn} onClick={() => setIsModalOpen(true)}>Добавить новый сервис</button>
            </div>

            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)', marginBottom: 20, overflow: 'hidden' }}>
              <div style={{ marginBottom: 16, color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Статусы</div>
              <div style={{ height: 250, overflow: 'hidden' }}>
                <StatusPieChart data={{
                  ok: services.filter(s => s.status === 'ok').length,
                  degraded: services.filter(s => s.status === 'degraded').length,
                  down: services.filter(s => s.status === 'down').length,
                }} />
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h3 style={{ margin: 0, color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Сервисы</h3>
              <Filters activeFilter={filter} onChange={setFilter} />
            </div>

            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <ServiceList services={filteredServices} onServiceClick={handleServiceClick} />
            </div>
          </>
        );
      case 'service-detail':
        return selectedService ? (
          <ServiceDetail 
            service={selectedService} 
            onEdit={handleEditService}
            onDelete={handleDeleteService}
            onBack={handleBackToDashboard}
          />
        ) : null;
      case 'alerts':
        return <Alerts />;
      case 'reports':
        return <Reports />;
      case 'settings':
        return <Settings />;
      default:
        return null;
    }
  }

  if (!isLoggedIn) {
    return <Auth onAuthSuccess={handleAuthSuccess} />;
  }

  return (
    <div style={pageStyle}>
      <Navigation currentPage={currentPage} onNavigate={setCurrentPage} />
      {renderPage()}

      <AddServiceModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleAddService}
      />

      <EditServiceModal
        service={editingService}
        open={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false);
          setEditingService(null);
        }}
        onSubmit={handleUpdateService}
      />
    </div>
  );
}

export default App;
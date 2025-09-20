import React, { useState, useEffect, useMemo, useCallback } from 'react';
import Navigation from './components/Navigation';
import CheckList from './components/CheckList'; // Functionally ServiceList
import AddCheckModal from './components/AddCheckModal'; // Functionally AddServiceModal
import EditCheckModal from './components/EditCheckModal'; // Functionally EditServiceModal
import CheckDetail from './pages/CheckDetail'; // Functionally ServiceDetail
import StatusPieChart from './components/charts/StatusPieChart';
import Auth from './pages/Auth';
import Reports from './pages/Reports';
import Alerts from './pages/Alerts';
import Settings from './pages/Settings';
import api from './utils/api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('jwtToken'));
  const [currentPage, setCurrentPage] = useState(isLoggedIn ? 'dashboard' : 'auth');
  const [services, setServices] = useState([]);
  const [selectedService, setSelectedService] = useState(null); // Renamed from selectedCheck
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchServices = useCallback(async () => {
    if (!isLoggedIn) return;

    setIsLoading(true);
    setError(null);
    try {
      const response = await api('/v1/api/services'); // Updated endpoint
      if (response.ok) {
        const data = await response.json();
        setServices(data.items || []); // Assuming data.items contains the services array
      } else {
        console.error('Failed to fetch services:', response.status);
        setError('Не удалось загрузить сервисы.');
      }
    } catch (err) {
      console.error('Error fetching services:', err);
      setError('Произошла ошибка при загрузке сервисов.');
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn]);

  useEffect(() => {
    if (isLoggedIn) {
      fetchServices();
    }
  }, [isLoggedIn, fetchServices]);


  const handleAuthSuccess = () => {
    setIsLoggedIn(true);
    setCurrentPage('dashboard');
  };

  const handleLogout = () => {
    localStorage.clear();
    setIsLoggedIn(false);
    setCurrentPage('auth');
    setServices([]);
    setSelectedService(null);
  };

  const handleAddService = async (newServiceData) => { // Renamed from handleAddCheck
    try {
      const response = await api('/v1/api/services', { // Updated endpoint
        method: 'POST',
        body: JSON.stringify({ ...newServiceData, enabled: true }), // Add enabled: true by default
      });
      if (response.ok) {
        fetchServices();
        setIsAddModalOpen(false);
      } else {
        const errorData = await response.json();
        console.error('Failed to add service:', errorData);
        setError(errorData.message || 'Ошибка при добавлении сервиса.');
      }
    } catch (err) {
      console.error('Error adding service:', err);
      setError('Произошла ошибка при добавлении сервиса.');
    }
  };

  const handleEditService = (service) => { // Renamed from handleEditCheck
    setSelectedService(service);
    setIsEditModalOpen(true);
  };

  const handleUpdateService = async (updatedServiceData) => { // Renamed from handleUpdateCheck
    try {
      const response = await api(`/v1/api/services/${updatedServiceData.id}`, { // Updated endpoint
        method: 'PUT',
        body: JSON.stringify(updatedServiceData),
      });
      if (response.ok) {
        fetchServices();
        setIsEditModalOpen(false);
        setSelectedService(null);
      } else {
        const errorData = await response.json();
        console.error('Failed to update service:', errorData);
        setError(errorData.message || 'Ошибка при обновлении сервиса.');
      }
    } catch (err) {
      console.error('Error updating service:', err);
      setError('Произошла ошибка при обновлении сервиса.');
    }
  };

  const handleDeleteService = async (serviceId) => { // Renamed from handleDeleteCheck
    if (window.confirm('Вы уверены, что хотите удалить этот сервис?')) {
      try {
        const response = await api(`/v1/api/services/${serviceId}`, { // Updated endpoint
          method: 'DELETE',
        });
        if (response.ok) {
          fetchServices();
          setSelectedService(null);
          setCurrentPage('dashboard');
        } else {
          const errorData = await response.json();
          console.error('Failed to delete service:', errorData);
          setError(errorData.message || 'Ошибка при удалении сервиса.');
        }
      } catch (err) {
        console.error('Error deleting service:', err);
        setError('Произошла ошибка при удалении сервиса.');
      }
    }
  };

  const handleServiceClick = (service) => { // Renamed from handleCheckClick
    setSelectedService(service);
    setCurrentPage('serviceDetail');
  };

  const dashboardContent = (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: 24, color: '#1a1a1a' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h1 style={{ margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' }}>Мои сервисы</h1>
        <button
          onClick={() => setIsAddModalOpen(true)}
          style={{
            padding: '10px 16px', borderRadius: 8, border: '1px solid #6D0475', background: '#6D0475', color: '#ffffff', cursor: 'pointer', fontSize: '14px', fontWeight: 500, transition: 'all 0.2s ease'
          }}
        >
          Добавить новый сервис
        </button>
      </div>
      {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}
      {isLoading ? (
        <div>Загрузка сервисов...</div>
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '24px' }}>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Всего сервисов</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#1a1a1a' }}>{services.length}</div>
            </div>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Активные</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#10b981' }}>{services.filter(s => s.enabled).length}</div>
            </div>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Неактивные</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#ef4444' }}>{services.filter(s => !s.enabled).length}</div>
            </div>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Статус</div>
              <StatusPieChart data={{
                ok: services.filter(s => s.status === 'UP').length,
                down: services.filter(s =>
                  s.status === 'DOWN' ||
                  s.status === 'DEGRADED' || // Still consolidate degraded, unknown, error as DOWN for pie chart
                  s.status === 'UNKNOWN' ||
                  s.status === 'ERROR'
                ).length,
              }} />
            </div>
          </div>
          <CheckList services={services} onServiceClick={handleServiceClick} /> {/* Renamed prop */}
        </>
      )}
    </div>
  );

  let content;
  if (!isLoggedIn) {
    content = <Auth onAuthSuccess={handleAuthSuccess} />;
  } else {
    switch (currentPage) {
      case 'dashboard':
        content = dashboardContent;
        break;
      case 'reports':
        content = <Reports />;
        break;
      case 'alerts':
        content = <Alerts />;
        break;
      case 'settings':
        content = <Settings />;
        break;
      case 'serviceDetail': // Renamed from checkDetail
        content = (
          <CheckDetail
            check={selectedService} // Renamed prop
            onEdit={handleEditService} // Renamed
            onDelete={handleDeleteService} // Renamed
            onBack={() => {
              setSelectedService(null);
              setCurrentPage('dashboard');
            }}
          />
        );
        break;
      default:
        content = dashboardContent;
    }
  }

  return (
    <div style={{ fontFamily: 'Inter, sans-serif', background: '#f8f9fa', minHeight: '100vh' }}>
      {isLoggedIn && <Navigation currentPage={currentPage} onNavigate={setCurrentPage} onLogout={handleLogout} />}
      {content}
      <AddCheckModal // Functionally AddServiceModal
        open={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSubmit={handleAddService} // Renamed
      />
      {selectedService && (
        <EditCheckModal // Functionally EditServiceModal
          open={isEditModalOpen}
          onClose={() => {
            setIsEditModalOpen(false);
            setSelectedService(null);
          }}
          check={selectedService} // Renamed prop
          onSubmit={handleUpdateService} // Renamed
        />
      )}
    </div>
  );
}

export default App;
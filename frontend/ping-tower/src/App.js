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
  const [selectedService, setSelectedService] = useState(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchServicesAndStatuses = useCallback(async () => {
    if (!isLoggedIn) return;

    setIsLoading(true);
    setError(null);
    try {
      const [servicesResponse, dashboardResponse] = await Promise.all([
        api('/v1/api/services'), // Получаем список всех сервисов
        api('/v1/api/monitoring/dashboard') // Получаем статусы для дашборда
      ]);

      let fetchedServices = [];
      if (servicesResponse.ok) {
        const servicesData = await servicesResponse.json();
        fetchedServices = servicesData.content || [];
      } else {
        console.error('Failed to fetch services:', servicesResponse.status);
        setError('Не удалось загрузить сервисы.');
      }

      if (dashboardResponse.ok) {
        const dashboardData = await dashboardResponse.json();
        const servicesWithStatus = fetchedServices.map(service => {
          const dashboardService = dashboardData.find(ds => ds.serviceId === service.id);
          return {
            ...service,
            status: dashboardService ? dashboardService.status : 'UNKNOWN', // Добавляем статус
            // Возможно, также добавить responseCode, responseTimeMs, lastChecked
            responseCode: dashboardService ? dashboardService.responseCode : null,
            responseTimeMs: dashboardService ? dashboardService.responseTimeMs : null,
            lastChecked: dashboardService ? dashboardService.lastChecked : null,
          };
        });
        setServices(servicesWithStatus);
      } else {
        const errorText = await dashboardResponse.text();
        console.error('Failed to fetch dashboard data:', dashboardResponse.status, errorText);
        // Если дашборд не загрузился, отображаем сервисы без статуса или с UNKNOWN
        setServices(fetchedServices.map(service => ({ ...service, status: 'UNKNOWN' })));
        // Удаляем setError для статусов сервисов
      }

    } catch (err) {
      console.error('Error fetching services and statuses:', err);
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn]);

  useEffect(() => {
    if (isLoggedIn) {
      fetchServicesAndStatuses();
    }
  }, [isLoggedIn, fetchServicesAndStatuses]);

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

  const handleAddService = async (newServiceData) => {
    try {
      const response = await api('/v1/api/services', {
        method: 'POST',
        body: JSON.stringify({ ...newServiceData, enabled: true }),
      });
      if (response.ok) {
        fetchServicesAndStatuses(); // Обновляем оба списка после добавления
        setIsAddModalOpen(false);
      } else {
        const errorData = await response.json();
        console.error('Failed to add service:', errorData);
        const errorMessage = errorData.message || 'Ошибка при добавлении сервиса.';
        setError(errorMessage);
      }
    } catch (err) {
      console.error('Error adding service:', err);
      const errorMessage = 'Произошла ошибка при добавлении сервиса: ' + err.message;
      setError(errorMessage);
    }
  };

  const handleEditService = (service) => {
    setSelectedService(service);
    setIsEditModalOpen(true);
  };

  const handleUpdateService = async (updatedServiceData) => {
    try {
      const response = await api(`/v1/api/services/${updatedServiceData.id}`, {
        method: 'PUT',
        body: JSON.stringify(updatedServiceData),
      });
      if (response.ok) {
        fetchServicesAndStatuses(); // Обновляем оба списка после обновления
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

  const handleDeleteService = async (serviceId) => {
    if (window.confirm('Вы уверены, что хотите удалить этот сервис?')) {
      try {
        const response = await api(`/v1/api/services/${serviceId}`, {
          method: 'DELETE',
        });
        if (response.ok) {
          fetchServicesAndStatuses(); // Обновляем оба списка после удаления
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

  const handleServiceClick = (service) => {
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
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 2fr', gap: '16px', marginBottom: '24px' }}>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Работают</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#10b981' }}>{services.filter(s => s.status === 'UP').length}</div>
            </div>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Не работают</div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#ef4444' }}>{services.filter(s => s.status === 'DOWN' || s.status === 'DEGRADED' || s.status === 'UNKNOWN' || s.status === 'ERROR').length}</div>
            </div>
            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <div style={{ fontSize: 12, color: '#6D0475', fontWeight: 600, marginBottom: 8 }}>Статус</div>
              {(() => {
                const okCount = services.filter(s => s.status === 'UP').length;
                const downCount = services.filter(s =>
                  s.status === 'DOWN' ||
                  s.status === 'DEGRADED' ||
                  s.status === 'UNKNOWN' ||
                  s.status === 'ERROR'
                ).length;
                console.log('Dashboard chart data:', { ok: okCount, down: downCount });
                return (
                  <StatusPieChart data={{
                    ok: okCount,
                    down: downCount,
                  }} />
                );
              })()}
            </div>
          </div>
          <CheckList services={services} onServiceClick={handleServiceClick} />
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
      case 'serviceDetail':
        content = (
          <CheckDetail
            check={selectedService}
            onEdit={handleEditService}
            onDelete={handleDeleteService}
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
      {error && <div style={{ color: 'red', marginBottom: '16px', padding: '10px', border: '1px solid red', borderRadius: '8px', textAlign: 'center' }}>{error}</div>}
      <AddCheckModal
        open={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSubmit={handleAddService}
      />
      {selectedService && (
        <EditCheckModal
          open={isEditModalOpen}
          onClose={() => {
            setIsEditModalOpen(false);
            setSelectedService(null);
          }}
          check={selectedService}
          onSubmit={handleUpdateService}
        />
      )}
    </div>
  );
}

export default App;
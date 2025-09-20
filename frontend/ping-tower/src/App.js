import { useMemo, useState, useEffect } from 'react';
import Navigation from './components/Navigation';
import Filters from './components/Filters';
import CheckList from './components/CheckList'; // This will functionally act as ServiceList
import AddCheckModal from './components/AddCheckModal'; // This will functionally act as AddServiceModal
import EditCheckModal from './components/EditCheckModal'; // This will functionally act as EditServiceModal
import AvailabilityLineChart from './components/charts/AvailabilityLineChart';
import StatusPieChart from './components/charts/StatusPieChart';
import CheckDetail from './pages/CheckDetail'; // This will functionally act as ServiceDetail
import Alerts from './pages/Alerts';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import Auth from './pages/Auth';
import api from './utils/api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(localStorage.getItem('jwtToken') ? true : false);
  const [currentPage, setCurrentPage] = useState('dashboard');
  const [selectedService, setSelectedService] = useState(null); // Renamed from selectedCheck
  const [services, setServices] = useState([]); // Renamed from checks, now holds service data from /monitoring/dashboard

  useEffect(() => {
    if (isLoggedIn) {
      const fetchDashboardData = async () => {
        try {
          const response = await api('/monitoring/dashboard');
          if (response.ok) {
            const data = await response.json();
            // Map new API response to existing 'checks' (now 'services') structure
            const mappedServices = data.map(service => ({
              id: service.serviceId, // Use serviceId as id
              name: service.serviceName,
              target: service.serviceUrl, // Map serviceUrl to target
              status: service.status, // UP, DOWN, DEGRADED, UNKNOWN, ERROR
              responseTimeMs: service.responseTimeMs,
              lastChecked: service.lastChecked,
              enabled: service.enabled,
              // Defaulting type for now, will refine in Add/Edit modals
              type: service.serviceType || 'HTTP',
              interval_sec: service.checkIntervalMinutes * 60 || 60, // Assuming a default
            }));
            setServices(mappedServices || []);
          } else {
            console.error('Failed to fetch dashboard data', response.status);
          }
        } catch (error) {
          console.error('Error fetching dashboard data:', error);
        }
      };
      fetchDashboardData();
    }
  }, [isLoggedIn]);

  const [filter, setFilter] = useState('all');
  const availabilityPoints = [
    { t: '00:00', v: 100 }, { t: '02:00', v: 99.8 }, { t: '04:00', v: 99.9 },
    { t: '06:00', v: 99.7 }, { t: '08:00', v: 99.9 }, { t: '10:00', v: 100 },
    { t: '12:00', v: 99.6 }, { t: '14:00', v: 99.9 }, { t: '16:00', v: 100 },
    { t: '18:00', v: 99.8 }, { t: '20:00', v: 99.9 }, { t: '22:00', v: 100 }
  ];
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingService, setEditingService] = useState(null); // Renamed from editingCheck

  const filteredServices = useMemo(() => {
    if (filter === 'all') return services;
    // Lowercase comparison for status
    return services.filter((s) => s.status.toLowerCase() === filter);
  }, [services, filter]);

  async function handleAddService(payload) { // Renamed from handleAddCheck
    try {
      const response = await api('/v1/api/services', { // Assuming /v1/api/services is the correct POST endpoint
        method: 'POST',
        headers: { 'Idempotency-Key': `service-${Date.now()}` },
        body: JSON.stringify({
          name: payload.name,
          url: payload.target, // Map target to url
          serviceType: payload.type, // Map type to serviceType
          checkIntervalMinutes: payload.interval_sec / 60, // Convert seconds to minutes
          // Add other required fields with default values or from payload if available
          timeoutSeconds: 30, // Default value
          httpMethod: 'GET', // Default value
          expectedStatusCode: 200, // Default value
        }),
      });

      if (response.ok) {
        const newService = await response.json();
        setServices((prev) => [newService, ...prev]);
        setIsModalOpen(false);
      } else {
        console.error('Failed to add service', response.status);
      }
    } catch (error) {
      console.error('Error adding service:', error);
    }
  }

  function handleEditService(service) { // Renamed from handleEditCheck
    setEditingService(service);
    setIsEditModalOpen(true);
  }

  async function handleUpdateService(updatedService) { // Renamed from handleUpdateCheck
    try {
      const response = await api(`/v1/api/services/${updatedService.id}`, { // Assuming PUT for full update, or PATCH
        method: 'PUT',
        body: JSON.stringify({
          name: updatedService.name,
          url: updatedService.target,
          serviceType: updatedService.type,
          checkIntervalMinutes: updatedService.interval_sec / 60,
          // Include other fields from the new API spec, possibly from existing updatedService object
          timeoutSeconds: updatedService.timeoutSeconds || 30,
          httpMethod: updatedService.httpMethod || 'GET',
          expectedStatusCode: updatedService.expectedStatusCode || 200,
          enabled: updatedService.enabled, // Assuming enabled can be updated
        }),
      });

      if (response.ok) {
        setServices((prev) => prev.map(s => s.id === updatedService.id ? updatedService : s));
        setIsEditModalOpen(false);
        setEditingService(null);
      } else {
        console.error('Failed to update service', response.status);
      }
    } catch (error) {
      console.error('Error updating service:', error);
    }
  }

  async function handleDeleteService(serviceId) { // Renamed from handleDeleteCheck
    if (window.confirm('Удалить сервис?')) {
      try {
        const response = await api(`/v1/api/services/${serviceId}`, {
          method: 'DELETE',
        });

        if (response.ok) {
          setServices((prev) => prev.filter(s => s.id !== serviceId));
          if (selectedService && selectedService.id === serviceId) {
            setSelectedService(null);
            setCurrentPage('dashboard');
          }
        } else {
          console.error('Failed to delete service', response.status);
        }
      } catch (error) {
        console.error('Error deleting service:', error);
      }
    }
  }

  function handleServiceClick(service) { // Renamed from handleCheckClick
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

  function handleLogout() {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    setIsLoggedIn(false);
    setCurrentPage('dashboard'); // Redirect to dashboard or auth page after logout
  }

  const pageStyle = { maxWidth: 1120, margin: '0 auto', padding: 24, display: 'flex', flexDirection: 'column', gap: 20, color: '#1a1a1a' };
  const headerStyle = { display: 'flex', alignItems: 'center', justifyContent: 'space-between' };
  const titleStyle = { margin: 0, fontSize: 24, fontWeight: 700, color: '#6D0475' };
  const addBtn = {
    padding: '10px 14px',
    borderRadius: 10,
    border: '1px solid #6D0475',
    cursor: 'pointer',
    background: '#6D0475', // Darker purple on hover
    color: '#ffffff',
    fontSize: 14,
    fontWeight: 500,
    fontFamily: 'Inter',
    transition: 'all 0.2s ease',
  };

  function renderPage() {
    switch (currentPage) {
      case 'dashboard':
        return (
          <>
            <div style={headerStyle}>
              <button
                style={addBtn}
                onClick={() => setIsModalOpen(true)}
              >
                Добавить новый сервис
              </button>
            </div>

            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)', marginBottom: 20, overflow: 'hidden' }}>
              <div style={{ marginBottom: 16, color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Статусы</div>
              <div style={{ height: 250, overflow: 'hidden' }}>
                <StatusPieChart data={{
                  ok: services.filter(s => s.status === 'UP').length,
                  down: services.filter(s =>
                    s.status === 'DOWN' ||
                    s.status === 'DEGRADED' ||
                    s.status === 'UNKNOWN' ||
                    s.status === 'ERROR'
                  ).length,
                }} />
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h3 style={{ margin: 0, color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Сервисы</h3>
              <Filters activeFilter={filter} onChange={setFilter} />
            </div>

            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <CheckList checks={filteredServices} onCheckClick={handleServiceClick} />
            </div>
          </>
        );
      case 'service-detail':
        return selectedService ? (
          <CheckDetail 
            check={selectedService} 
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
      <Navigation currentPage={currentPage} onNavigate={setCurrentPage} onLogout={handleLogout} isLoggedIn={isLoggedIn} />
      {renderPage()}

      <AddCheckModal
        open={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleAddService}
      />

      <EditCheckModal
        check={editingService}
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
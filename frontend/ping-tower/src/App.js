import { useMemo, useState, useEffect } from 'react';
import Navigation from './components/Navigation';
import Filters from './components/Filters';
import CheckList from './components/CheckList'; // Updated import
import AddCheckModal from './components/AddCheckModal'; // Updated import
import EditCheckModal from './components/EditCheckModal'; // Updated import
import AvailabilityLineChart from './components/charts/AvailabilityLineChart';
import StatusPieChart from './components/charts/StatusPieChart';
import CheckDetail from './pages/CheckDetail'; // Corrected import from ServiceDetail
import Alerts from './pages/Alerts';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import Auth from './pages/Auth';
import api from './utils/api';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(localStorage.getItem('jwtToken') ? true : false);
  const [currentPage, setCurrentPage] = useState('dashboard');
  const [selectedCheck, setSelectedCheck] = useState(null);
  const [checks, setChecks] = useState([]); // Renamed from services

  useEffect(() => {
    if (isLoggedIn) {
      const fetchChecks = async () => {
        try {
          const response = await api('/v1/api/checks');
          if (response.ok) {
            const data = await response.json();
            setChecks(data.items || []);
          } else {
            console.error('Failed to fetch checks', response.status);
          }
        } catch (error) {
          console.error('Error fetching checks:', error);
        }
      };
      fetchChecks();
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
  const [editingCheck, setEditingCheck] = useState(null); // Renamed from editingService

  const filteredChecks = useMemo(() => {
    if (filter === 'all') return checks;
    return checks.filter((s) => s.status === filter);
  }, [checks, filter]);

  async function handleAddCheck(payload) { // Renamed from handleAddService
    try {
      const response = await api('/v1/api/checks', {
        method: 'POST',
        headers: { 'Idempotency-Key': `check-${Date.now()}` }, // Required by OpenAPI spec
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        const newCheck = await response.json();
        setChecks((prev) => [newCheck, ...prev]);
        setIsModalOpen(false);
      } else {
        console.error('Failed to add check', response.status);
      }
    } catch (error) {
      console.error('Error adding check:', error);
    }
  }

  function handleEditCheck(check) { // Renamed from handleEditService
    setEditingCheck(check);
    setIsEditModalOpen(true);
  }

  async function handleUpdateCheck(updatedCheck) { // Renamed from handleUpdateService
    try {
      const response = await api(`/v1/api/checks/${updatedCheck.id}`, { // Assuming PUT/PATCH endpoint exists
        method: 'PUT', // Or PATCH depending on API implementation for update
        body: JSON.stringify(updatedCheck),
      });

      if (response.ok) {
        setChecks((prev) => prev.map(c => c.id === updatedCheck.id ? updatedCheck : c));
        setIsEditModalOpen(false);
        setEditingCheck(null);
      } else {
        console.error('Failed to update check', response.status);
      }
    } catch (error) {
      console.error('Error updating check:', error);
    }
  }

  async function handleDeleteCheck(checkId) { // Renamed from handleDeleteService
    if (window.confirm('Удалить проверку?')) {
      try {
        const response = await api(`/v1/api/checks/${checkId}`, {
          method: 'DELETE',
        });

        if (response.ok) {
          setChecks((prev) => prev.filter(c => c.id !== checkId));
          if (selectedCheck && selectedCheck.id === checkId) {
            setSelectedCheck(null);
            setCurrentPage('dashboard');
          }
        } else {
          console.error('Failed to delete check', response.status);
        }
      } catch (error) {
        console.error('Error deleting check:', error);
      }
    }
  }

  function handleCheckClick(check) { // Renamed from handleServiceClick
    setSelectedCheck(check);
    setCurrentPage('service-detail'); // Still service-detail for now, will update later
  }

  function handleBackToDashboard() {
    setSelectedCheck(null);
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
                Добавить новую проверку
              </button>
            </div>

            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 20, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)', marginBottom: 20, overflow: 'hidden' }}>
              <div style={{ marginBottom: 16, color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Статусы</div>
              <div style={{ height: 250, overflow: 'hidden' }}>
                <StatusPieChart data={{
                  ok: checks.filter(c => c.status === 'ok').length,
                  down: checks.filter(c => c.status === 'down').length,
                }} />
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <h3 style={{ margin: 0, color: '#6D0475', fontSize: 18, fontWeight: 600 }}>Проверки</h3>
              <Filters activeFilter={filter} onChange={setFilter} />
            </div>

            <div style={{ background: '#ffffff', border: '1px solid #E5B8E8', borderRadius: 12, padding: 16, boxShadow: '0 2px 4px rgba(109, 4, 117, 0.1)' }}>
              <CheckList checks={filteredChecks} onCheckClick={handleCheckClick} />
            </div>
          </>
        );
      case 'service-detail':
        return selectedCheck ? (
          <CheckDetail 
            check={selectedCheck} 
            onEdit={handleEditCheck}
            onDelete={handleDeleteCheck}
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
        onSubmit={handleAddCheck}
      />

      <EditCheckModal
        check={editingCheck}
        open={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false);
          setEditingCheck(null);
        }}
        onSubmit={handleUpdateCheck}
      />
    </div>
  );
}

export default App;
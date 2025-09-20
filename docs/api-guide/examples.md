# Примеры использования API

## Дашборды и мониторинг

### 1. Главный дашборд

```javascript
// Получение данных для главного дашборда
const fetchDashboardData = async () => {
  try {
    // Получаем статус всех сервисов
    const dashboardResponse = await api('/api/v1/monitoring/dashboard');
    const services = await dashboardResponse.json();
    
    // Получаем общую статистику
    const healthResponse = await api('/api/v1/monitoring/health');
    const health = await healthResponse.json();
    
    return {
      services,
      health
    };
  } catch (error) {
    console.error('Error fetching dashboard data:', error);
  }
};
```

### 2. Детальная страница сервиса

```javascript
// Получение данных для детальной страницы сервиса
const fetchServiceDetails = async (serviceId) => {
  try {
    // Получаем метрики сервиса
    const metricsResponse = await api(`/api/v1/monitoring/services/${serviceId}/metrics`);
    const metrics = await metricsResponse.json();
    
    // Получаем последние результаты
    const resultsResponse = await api(`/api/v1/monitoring/services/${serviceId}/results?size=100`);
    const results = await resultsResponse.json();
    
    return {
      metrics,
      results: results.content
    };
  } catch (error) {
    console.error('Error fetching service details:', error);
  }
};
```

### 3. График доступности

```javascript
// Получение данных для графика доступности
const fetchAvailabilityData = async (serviceId, hours = 24) => {
  try {
    const since = new Date(Date.now() - hours * 60 * 60 * 1000).toISOString();
    const response = await api(`/api/v1/monitoring/services/${serviceId}/results?since=${since}&size=1000`);
    const data = await response.json();
    
    // Обработка данных для графика
    const chartData = data.content.map(result => ({
      time: new Date(result.checkTime).toLocaleTimeString(),
      uptime: result.successful ? 100 : 0,
      responseTime: result.responseTimeMs
    }));
    
    return chartData;
  } catch (error) {
    console.error('Error fetching availability data:', error);
  }
};
```

### 4. Мониторинг алертов

```javascript
// Получение активных алертов
const fetchActiveAlerts = async () => {
  try {
    const response = await api('/api/v1/alerts?resolved=false&size=50');
    const data = await response.json();
    return data.content;
  } catch (error) {
    console.error('Error fetching alerts:', error);
  }
};
```

## React компоненты

### Компонент дашборда

```jsx
import React, { useState, useEffect } from 'react';

const Dashboard = () => {
  const [services, setServices] = useState([]);
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Параллельные запросы для улучшения производительности
      const [dashboardResponse, healthResponse] = await Promise.all([
        api('/api/v1/monitoring/dashboard'),
        api('/api/v1/monitoring/health')
      ]);

      const servicesData = await dashboardResponse.json();
      const healthData = await healthResponse.json();

      setServices(servicesData);
      setHealth(healthData);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div>Загрузка дашборда...</div>;
  }

  return (
    <div className="dashboard">
      <div className="health-summary">
        <h2>Общее состояние системы</h2>
        <div className="stats">
          <div className="stat">
            <span className="label">Всего сервисов:</span>
            <span className="value">{health?.totalServices}</span>
          </div>
          <div className="stat">
            <span className="label">Активных:</span>
            <span className="value">{health?.activeServices}</span>
          </div>
          <div className="stat">
            <span className="label">Успешность:</span>
            <span className="value">{health?.successRate?.toFixed(1)}%</span>
          </div>
        </div>
      </div>

      <div className="services-grid">
        <h2>Сервисы</h2>
        {services.map(service => (
          <div key={service.serviceId} className={`service-card ${service.status.toLowerCase()}`}>
            <h3>{service.serviceName}</h3>
            <p className="url">{service.serviceUrl}</p>
            <div className="status">
              <span className={`status-badge ${service.status.toLowerCase()}`}>
                {service.status}
              </span>
              {service.responseTimeMs && (
                <span className="response-time">{service.responseTimeMs}ms</span>
              )}
            </div>
            <p className="last-checked">
              Последняя проверка: {new Date(service.lastChecked).toLocaleString()}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Dashboard;
```

### Компонент детальной страницы сервиса

```jsx
import React, { useState, useEffect } from 'react';

const ServiceDetail = ({ serviceId }) => {
  const [metrics, setMetrics] = useState(null);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (serviceId) {
      fetchServiceDetails(serviceId);
    }
  }, [serviceId]);

  const fetchServiceDetails = async (id) => {
    try {
      setLoading(true);
      
      const [metricsResponse, resultsResponse] = await Promise.all([
        api(`/api/v1/monitoring/services/${id}/metrics`),
        api(`/api/v1/monitoring/services/${id}/results?size=100`)
      ]);

      const metricsData = await metricsResponse.json();
      const resultsData = await resultsResponse.json();

      setMetrics(metricsData);
      setResults(resultsData.content);
    } catch (error) {
      console.error('Error fetching service details:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div>Загрузка деталей сервиса...</div>;
  }

  if (!metrics) {
    return <div>Сервис не найден</div>;
  }

  return (
    <div className="service-detail">
      <div className="service-header">
        <h1>{metrics.serviceName}</h1>
        <p className="service-url">{metrics.serviceUrl}</p>
      </div>

      <div className="metrics-grid">
        <div className="metric-card">
          <h3>Uptime</h3>
          <div className="metric-value">{metrics.uptimePercentage?.toFixed(2)}%</div>
        </div>
        
        <div className="metric-card">
          <h3>Среднее время ответа</h3>
          <div className="metric-value">{metrics.averageResponseTimeMs?.toFixed(0)}ms</div>
        </div>
        
        <div className="metric-card">
          <h3>Всего проверок</h3>
          <div className="metric-value">{metrics.totalChecks}</div>
        </div>
        
        <div className="metric-card">
          <h3>Неудачных проверок</h3>
          <div className="metric-value">{metrics.failedChecks}</div>
        </div>
      </div>

      <div className="recent-results">
        <h2>Последние результаты</h2>
        <div className="results-table">
          <table>
            <thead>
              <tr>
                <th>Время</th>
                <th>Статус</th>
                <th>Код ответа</th>
                <th>Время ответа</th>
                <th>Ошибка</th>
              </tr>
            </thead>
            <tbody>
              {results.map(result => (
                <tr key={result.id} className={result.successful ? 'success' : 'failure'}>
                  <td>{new Date(result.checkTime).toLocaleString()}</td>
                  <td>{result.successful ? 'UP' : 'DOWN'}</td>
                  <td>{result.responseCode}</td>
                  <td>{result.responseTimeMs}ms</td>
                  <td>{result.errorMessage || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default ServiceDetail;
```

### Компонент графика доступности

```jsx
import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const AvailabilityChart = ({ serviceId, hours = 24 }) => {
  const [chartData, setChartData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (serviceId) {
      fetchAvailabilityData(serviceId, hours);
    }
  }, [serviceId, hours]);

  const fetchAvailabilityData = async (id, hoursCount) => {
    try {
      setLoading(true);
      const since = new Date(Date.now() - hoursCount * 60 * 60 * 1000).toISOString();
      const response = await api(`/api/v1/monitoring/services/${id}/results?since=${since}&size=1000`);
      const data = await response.json();
      
      // Обработка данных для графика
      const processedData = data.content.map(result => ({
        time: new Date(result.checkTime).toLocaleTimeString(),
        uptime: result.successful ? 100 : 0,
        responseTime: result.responseTimeMs
      }));
      
      setChartData(processedData);
    } catch (error) {
      console.error('Error fetching availability data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div>Загрузка графика...</div>;
  }

  return (
    <div className="availability-chart">
      <h3>Доступность за последние {hours} часов</h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="time" />
          <YAxis domain={[0, 100]} />
          <Tooltip />
          <Line 
            type="monotone" 
            dataKey="uptime" 
            stroke="#8884d8" 
            strokeWidth={2}
            dot={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default AvailabilityChart;
```

## Утилиты для работы с API

### Универсальная функция API запросов

```javascript
const BASE_URL = 'http://localhost:8080';

const api = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };

  const response = await fetch(`${BASE_URL}${url}`, { ...options, headers });

  if (response.status === 401) {
    // Перенаправление на страницу входа
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    window.location.href = '/';
    return;
  }

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response;
};
```

### Кэширование данных

```javascript
class DataCache {
  constructor(ttl = 30000) { // 30 секунд по умолчанию
    this.cache = new Map();
    this.ttl = ttl;
  }

  set(key, data) {
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    });
  }

  get(key) {
    const item = this.cache.get(key);
    if (!item) return null;

    if (Date.now() - item.timestamp > this.ttl) {
      this.cache.delete(key);
      return null;
    }

    return item.data;
  }

  clear() {
    this.cache.clear();
  }
}

const cache = new DataCache();

const fetchWithCache = async (url, cacheKey) => {
  // Проверяем кэш
  const cachedData = cache.get(cacheKey);
  if (cachedData) {
    return cachedData;
  }

  // Загружаем данные
  const response = await api(url);
  const data = await response.json();
  
  // Сохраняем в кэш
  cache.set(cacheKey, data);
  
  return data;
};
```

### Обработка ошибок

```javascript
const handleApiError = (error, context = '') => {
  console.error(`API Error ${context}:`, error);
  
  if (error.message.includes('401')) {
    // Неавторизованный доступ
    localStorage.removeItem('jwtToken');
    window.location.href = '/login';
  } else if (error.message.includes('403')) {
    // Недостаточно прав
    alert('У вас недостаточно прав для выполнения этого действия');
  } else if (error.message.includes('404')) {
    // Ресурс не найден
    alert('Запрашиваемый ресурс не найден');
  } else if (error.message.includes('500')) {
    // Внутренняя ошибка сервера
    alert('Произошла внутренняя ошибка сервера. Попробуйте позже.');
  } else {
    // Общая ошибка
    alert('Произошла ошибка при загрузке данных');
  }
};
```

## Интеграция с внешними библиотеками

### Chart.js для графиков

```javascript
import Chart from 'chart.js/auto';

const createAvailabilityChart = (canvasId, data) => {
  const ctx = document.getElementById(canvasId).getContext('2d');
  
  new Chart(ctx, {
    type: 'line',
    data: {
      labels: data.map(item => item.time),
      datasets: [{
        label: 'Доступность (%)',
        data: data.map(item => item.uptime),
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        tension: 0.1
      }]
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
          max: 100
        }
      }
    }
  });
};
```

### React Query для управления состоянием

```javascript
import { useQuery } from 'react-query';

const useDashboardData = () => {
  return useQuery('dashboard', async () => {
    const [dashboardResponse, healthResponse] = await Promise.all([
      api('/api/v1/monitoring/dashboard'),
      api('/api/v1/monitoring/health')
    ]);

    return {
      services: await dashboardResponse.json(),
      health: await healthResponse.json()
    };
  }, {
    refetchInterval: 30000, // Обновление каждые 30 секунд
    staleTime: 15000 // Данные считаются свежими 15 секунд
  });
};

const Dashboard = () => {
  const { data, isLoading, error } = useDashboardData();

  if (isLoading) return <div>Загрузка...</div>;
  if (error) return <div>Ошибка загрузки данных</div>;

  return (
    <div>
      {/* Рендер дашборда */}
    </div>
  );
};
```

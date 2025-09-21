# Рекомендации по производительности

## Обзор

Данный раздел содержит рекомендации по оптимизации производительности при работе с PingTower API.

## 1. Пагинация

Всегда используйте пагинацию для больших наборов данных:

```http
GET /api/v1/monitoring/results?page=0&size=50
```

### Рекомендуемые размеры страниц

- **Результаты проверок**: 20-100 записей
- **Алерты**: 20-50 записей
- **Сервисы**: 20-50 записей
- **История запусков**: 100-1000 записей

### Реализация пагинации на фронтенде

```javascript
const usePaginatedData = (url, pageSize = 20) => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchData = async (page = 0) => {
    setLoading(true);
    try {
      const response = await api(`${url}?page=${page}&size=${pageSize}`);
      const result = await response.json();
      
      setData(result.content);
      setTotalPages(result.totalPages);
      setCurrentPage(page);
    } catch (error) {
      console.error('Error fetching paginated data:', error);
    } finally {
      setLoading(false);
    }
  };

  const nextPage = () => {
    if (currentPage < totalPages - 1) {
      fetchData(currentPage + 1);
    }
  };

  const prevPage = () => {
    if (currentPage > 0) {
      fetchData(currentPage - 1);
    }
  };

  return {
    data,
    loading,
    currentPage,
    totalPages,
    nextPage,
    prevPage,
    fetchData
  };
};
```

## 2. Фильтрация по времени

Ограничивайте временные диапазоны для улучшения производительности:

```http
GET /api/v1/monitoring/services/1/results?since=2024-01-15T00:00:00&until=2024-01-15T23:59:59
```

### Рекомендуемые временные диапазоны

- **Дашборд**: последние 24 часа
- **Детальная страница**: последние 7 дней
- **Отчеты**: максимум 30 дней
- **Аналитика**: максимум 90 дней

### Утилиты для работы с временем

```javascript
const getTimeRange = (hours) => {
  const now = new Date();
  const since = new Date(now.getTime() - hours * 60 * 60 * 1000);
  return {
    since: since.toISOString(),
    until: now.toISOString()
  };
};

const getDateRange = (days) => {
  const now = new Date();
  const since = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
  return {
    since: since.toISOString(),
    until: now.toISOString()
  };
};

// Использование
const { since, until } = getTimeRange(24); // Последние 24 часа
const url = `/api/v1/monitoring/results?since=${since}&until=${until}`;
```

## 3. Кэширование

Кэшируйте данные на frontend для уменьшения количества запросов:

### Простое кэширование

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

  // Очистка устаревших записей
  cleanup() {
    const now = Date.now();
    for (const [key, item] of this.cache.entries()) {
      if (now - item.timestamp > this.ttl) {
        this.cache.delete(key);
      }
    }
  }
}

const cache = new DataCache();

const fetchWithCache = async (url, cacheKey, ttl = 30000) => {
  // Проверяем кэш
  const cachedData = cache.get(cacheKey);
  if (cachedData) {
    return cachedData;
  }

  // Загружаем данные
  const response = await api(url);
  const data = await response.json();
  
  // Сохраняем в кэш
  cache.set(cacheKey, data, ttl);
  
  return data;
};
```

### Кэширование с React

```jsx
import React, { useState, useEffect, useMemo } from 'react';

const useCachedData = (fetchFunction, cacheKey, ttl = 30000) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const result = await fetchWithCache(fetchFunction, cacheKey, ttl);
        setData(result);
      } catch (err) {
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [fetchFunction, cacheKey, ttl]);

  return { data, loading, error };
};

// Использование
const Dashboard = () => {
  const { data, loading, error } = useCachedData(
    () => api('/api/v1/monitoring/dashboard'),
    'dashboard',
    30000 // 30 секунд кэш
  );

  if (loading) return <div>Загрузка...</div>;
  if (error) return <div>Ошибка: {error.message}</div>;

  return <div>{/* Рендер дашборда */}</div>;
};
```

## 4. Параллельные запросы

Используйте параллельные запросы для улучшения производительности:

```javascript
const fetchDashboardData = async () => {
  try {
    // Параллельные запросы
    const [dashboardResponse, healthResponse, alertsResponse] = await Promise.all([
      api('/api/v1/monitoring/dashboard'),
      api('/api/v1/monitoring/health'),
      api('/api/v1/alerts?resolved=false&size=10')
    ]);

    const [services, health, alerts] = await Promise.all([
      dashboardResponse.json(),
      healthResponse.json(),
      alertsResponse.json()
    ]);

    return { services, health, alerts };
  } catch (error) {
    console.error('Error fetching dashboard data:', error);
    throw error;
  }
};
```

### React Query для параллельных запросов

```javascript
import { useQueries } from 'react-query';

const useDashboardData = () => {
  const queries = useQueries([
    {
      queryKey: ['dashboard'],
      queryFn: () => api('/api/v1/monitoring/dashboard').then(res => res.json())
    },
    {
      queryKey: ['health'],
      queryFn: () => api('/api/v1/monitoring/health').then(res => res.json())
    },
    {
      queryKey: ['alerts'],
      queryFn: () => api('/api/v1/alerts?resolved=false&size=10').then(res => res.json())
    }
  ]);

  return {
    services: queries[0].data,
    health: queries[1].data,
    alerts: queries[2].data,
    loading: queries.some(query => query.isLoading),
    error: queries.find(query => query.error)?.error
  };
};
```

## 5. Lazy Loading

Загружайте данные по требованию:

```jsx
import React, { useState, useEffect } from 'react';

const ServiceList = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const loadMore = async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    try {
      const response = await api(`/api/v1/services?page=${page}&size=20`);
      const data = await response.json();
      
      if (page === 0) {
        setServices(data.content);
      } else {
        setServices(prev => [...prev, ...data.content]);
      }
      
      setPage(prev => prev + 1);
      setHasMore(data.content.length === 20);
    } catch (error) {
      console.error('Error loading services:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMore();
  }, []);

  return (
    <div>
      {services.map(service => (
        <ServiceItem key={service.id} service={service} />
      ))}
      
      {hasMore && (
        <button onClick={loadMore} disabled={loading}>
          {loading ? 'Загрузка...' : 'Загрузить еще'}
        </button>
      )}
    </div>
  );
};
```

## 6. Debouncing для поиска

Используйте debouncing для поисковых запросов:

```javascript
import { useDebouncedCallback } from 'use-debounce';

const SearchableServiceList = () => {
  const [services, setServices] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(false);

  const debouncedSearch = useDebouncedCallback(async (term) => {
    if (!term.trim()) {
      setServices([]);
      return;
    }

    setLoading(true);
    try {
      const response = await api(`/api/v1/services?search=${encodeURIComponent(term)}`);
      const data = await response.json();
      setServices(data.content);
    } catch (error) {
      console.error('Search error:', error);
    } finally {
      setLoading(false);
    }
  }, 300); // 300ms задержка

  useEffect(() => {
    debouncedSearch(searchTerm);
  }, [searchTerm, debouncedSearch]);

  return (
    <div>
      <input
        type="text"
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        placeholder="Поиск сервисов..."
      />
      
      {loading && <div>Поиск...</div>}
      
      {services.map(service => (
        <ServiceItem key={service.id} service={service} />
      ))}
    </div>
  );
};
```

## 7. WebSocket для real-time обновлений

Для real-time обновлений рекомендуется использовать WebSocket соединения:

```javascript
class WebSocketManager {
  constructor() {
    this.ws = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 1000;
  }

  connect() {
    const token = localStorage.getItem('jwtToken');
    const wsUrl = `ws://localhost:8080/ws?token=${token}`;
    
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
    };

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      this.handleMessage(data);
    };

    this.ws.onclose = () => {
      console.log('WebSocket disconnected');
      this.attemptReconnect();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  handleMessage(data) {
    // Обработка различных типов сообщений
    switch (data.type) {
      case 'SERVICE_STATUS_UPDATE':
        this.updateServiceStatus(data.payload);
        break;
      case 'NEW_ALERT':
        this.showNewAlert(data.payload);
        break;
      case 'CHECK_RESULT':
        this.updateCheckResult(data.payload);
        break;
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        console.log(`Reconnecting... attempt ${this.reconnectAttempts}`);
        this.connect();
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
    }
  }
}

// Использование в React
const useWebSocket = () => {
  const [wsManager] = useState(() => new WebSocketManager());

  useEffect(() => {
    wsManager.connect();
    return () => wsManager.disconnect();
  }, [wsManager]);

  return wsManager;
};
```

## 8. Оптимизация запросов

### Batch запросы

```javascript
const batchRequests = async (requests) => {
  const batchSize = 5; // Максимум 5 запросов одновременно
  const results = [];

  for (let i = 0; i < requests.length; i += batchSize) {
    const batch = requests.slice(i, i + batchSize);
    const batchResults = await Promise.all(batch.map(request => request()));
    results.push(...batchResults);
  }

  return results;
};

// Использование
const fetchMultipleServiceMetrics = async (serviceIds) => {
  const requests = serviceIds.map(id => 
    () => api(`/api/v1/monitoring/services/${id}/metrics`).then(res => res.json())
  );
  
  return await batchRequests(requests);
};
```

### Предзагрузка данных

```javascript
const preloadData = async (urls) => {
  const promises = urls.map(url => 
    fetch(url).then(response => response.json()).catch(() => null)
  );
  
  return Promise.all(promises);
};

// Предзагрузка критически важных данных
const preloadCriticalData = async () => {
  const criticalUrls = [
    '/api/v1/monitoring/health',
    '/api/v1/monitoring/dashboard'
  ];
  
  await preloadData(criticalUrls);
};
```

## 9. Мониторинг производительности

### Измерение времени запросов

```javascript
const measureApiPerformance = async (url, options = {}) => {
  const startTime = performance.now();
  
  try {
    const response = await api(url, options);
    const endTime = performance.now();
    const duration = endTime - startTime;
    
    // Логирование медленных запросов
    if (duration > 1000) {
      console.warn(`Slow API request: ${url} took ${duration.toFixed(2)}ms`);
    }
    
    return response;
  } catch (error) {
    const endTime = performance.now();
    const duration = endTime - startTime;
    console.error(`API request failed: ${url} took ${duration.toFixed(2)}ms`, error);
    throw error;
  }
};
```

### Метрики производительности

```javascript
class PerformanceMonitor {
  constructor() {
    this.metrics = {
      requestCount: 0,
      totalTime: 0,
      slowRequests: 0,
      errorCount: 0
    };
  }

  recordRequest(duration, success) {
    this.metrics.requestCount++;
    this.metrics.totalTime += duration;
    
    if (duration > 1000) {
      this.metrics.slowRequests++;
    }
    
    if (!success) {
      this.metrics.errorCount++;
    }
  }

  getAverageResponseTime() {
    return this.metrics.requestCount > 0 
      ? this.metrics.totalTime / this.metrics.requestCount 
      : 0;
  }

  getSlowRequestPercentage() {
    return this.metrics.requestCount > 0 
      ? (this.metrics.slowRequests / this.metrics.requestCount) * 100 
      : 0;
  }

  getErrorRate() {
    return this.metrics.requestCount > 0 
      ? (this.metrics.errorCount / this.metrics.requestCount) * 100 
      : 0;
  }
}

const performanceMonitor = new PerformanceMonitor();
```

## 10. Рекомендации по оптимизации

### Настройки браузера

1. **Включите HTTP/2** для мультиплексирования запросов
2. **Используйте Service Workers** для кэширования
3. **Настройте сжатие** (gzip/brotli) на сервере

### Настройки приложения

1. **Ограничьте количество одновременных запросов**
2. **Используйте connection pooling**
3. **Настройте таймауты** для запросов

### Мониторинг

1. **Отслеживайте время ответа API**
2. **Мониторьте количество запросов**
3. **Анализируйте ошибки и их частоту**
4. **Используйте инструменты разработчика** для профилирования

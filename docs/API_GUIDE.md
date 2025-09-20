# PingTower API Guide - Мониторинг и Дашборды

## Обзор

PingTower предоставляет REST API для получения данных мониторинга, метрик и аналитики для построения дашбордов на frontend. API поддерживает как реальные данные из PostgreSQL, так и аналитические данные из ClickHouse.

## Аутентификация

Все API запросы требуют JWT токен в заголовке Authorization:

```http
Authorization: Bearer <your-jwt-token>
```

## Базовый URL

```
http://localhost:8080
```

## Эндпоинты аутентификации и управления пользователями

### 1. Регистрация пользователя

**POST** `/api/auth/register`

Создает нового пользователя в системе.

**Тело запроса:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**Валидация:**
- `username` - обязательное поле, 3-50 символов
- `email` - обязательное поле, валидный email
- `password` - обязательное поле, минимум 6 символов

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

**Ошибки:**
- `400 Bad Request` - неверные данные
- `409 Conflict` - пользователь с таким username или email уже существует

### 2. Вход в систему

**POST** `/api/auth/login`

Аутентифицирует пользователя и возвращает JWT токен.

**Тело запроса:**
```json
{
  "username": "john_doe",
  "password": "securepassword123"
}
```

**Валидация:**
- `username` - обязательное поле
- `password` - обязательное поле

**Ответ:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["USER"]
}
```

**Ошибки:**
- `400 Bad Request` - неверные данные
- `401 Unauthorized` - неверные учетные данные

### 3. Получение профиля пользователя

**GET** `/api/auth/profile`

Возвращает информацию о текущем пользователе.

**Заголовки:**
```http
Authorization: Bearer <your-jwt-token>
```

**Ответ:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "isActive": true
}
```

**Ошибки:**
- `401 Unauthorized` - токен отсутствует или недействителен

## Основные эндпоинты для дашбордов

### 1. Общий дашборд системы

**GET** `/monitoring/dashboard`

Возвращает статус всех активных сервисов для главного дашборда.

**Параметры:** Нет

**Ответ:**
```json
[
  {
    "serviceId": 1,
    "serviceName": "My API",
    "serviceUrl": "https://api.example.com/health",
    "status": "UP",
    "responseCode": 200,
    "responseTimeMs": 150,
    "lastChecked": "2024-01-15T10:30:00",
    "enabled": true
  },
  {
    "serviceId": 2,
    "serviceName": "Database Service",
    "serviceUrl": "https://db.example.com/ping",
    "status": "DOWN",
    "responseCode": 500,
    "responseTimeMs": 5000,
    "lastChecked": "2024-01-15T10:29:45",
    "enabled": true
  }
]
```

**Статусы сервисов:**
- `UP` - Сервис работает нормально
- `DOWN` - Сервис недоступен
- `DEGRADED` - Сервис работает с проблемами
- `UNKNOWN` - Статус неизвестен (нет данных)
- `ERROR` - Ошибка при проверке

### 2. Здоровье системы

**GET** `/monitoring/health`

Возвращает общую статистику системы мониторинга.

**Параметры:** Нет

**Ответ:**
```json
{
  "totalServices": 15,
  "activeServices": 12,
  "recentChecks": 1440,
  "recentFailures": 23,
  "successRate": 98.4,
  "timestamp": "2024-01-15T10:30:00"
}
```

### 3. Результаты проверок

**GET** `/monitoring/results`

Возвращает последние результаты проверок с фильтрацией.

**Параметры:**
- `page` (int, optional) - Номер страницы (по умолчанию 0)
- `size` (int, optional) - Размер страницы (по умолчанию 20)
- `serviceId` (long, optional) - ID конкретного сервиса
- `successful` (boolean, optional) - Фильтр по успешности
- `since` (string, optional) - Дата начала в ISO формате

**Пример запроса:**
```http
GET /api/v1/monitoring/results?page=0&size=50&successful=false&since=2024-01-15T00:00:00
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 12345,
      "serviceId": 1,
      "serviceName": "My API",
      "checkedAt": "2024-01-15T10:30:00",
      "successful": false,
      "responseCode": 500,
      "responseTimeMs": 5000,
      "errorMessage": "Connection timeout"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 4. Результаты конкретного сервиса

**GET** `/monitoring/services/{serviceId}/results`

Возвращает результаты проверок для конкретного сервиса.

**Параметры:**
- `serviceId` (path) - ID сервиса
- `page` (int, optional) - Номер страницы
- `size` (int, optional) - Размер страницы
- `since` (string, optional) - Дата начала
- `until` (string, optional) - Дата окончания

**Пример запроса:**
```http
GET /api/v1/monitoring/services/1/results?since=2024-01-15T00:00:00&until=2024-01-15T23:59:59&size=100
```

**Ответ:** Аналогичен предыдущему эндпоинту

### 5. Метрики сервиса

**GET** `/monitoring/services/{serviceId}/metrics`

Возвращает агрегированные метрики сервиса за период.

**Параметры:**
- `serviceId` (path) - ID сервиса
- `since` (string, optional) - Дата начала (по умолчанию 30 дней назад)

**Пример запроса:**
```http
GET /api/v1/monitoring/services/1/metrics?since=2024-01-01T00:00:00
```

**Ответ:**
```json
{
  "serviceId": 1,
  "serviceName": "My API",
  "serviceUrl": "https://api.example.com/health",
  "totalChecks": 1440,
  "successfulChecks": 1417,
  "failedChecks": 23,
  "averageResponseTime": 245.5,
  "minResponseTime": 89,
  "maxResponseTime": 2100,
  "uptimePercentage": 98.4,
  "periodStart": "2024-01-01T00:00:00",
  "periodEnd": "2024-01-15T10:30:00"
}
```

## Дополнительные эндпоинты

### 6. Список сервисов

**GET** `/services`

Возвращает список всех сервисов пользователя.

**Параметры:**
- `page` (int, optional) - Номер страницы
- `size` (int, optional) - Размер страницы
- `enabled` (boolean, optional) - Фильтр по активности
- `search` (string, optional) - Поиск по имени

**Пример запроса:**
```http
GET /api/services?page=0&size=20&enabled=true&search=api
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "My API",
      "description": "Production API service",
      "url": "https://api.example.com/health",
      "serviceType": "HTTP",
      "enabled": true,
      "checkIntervalMinutes": 5,
      "timeoutSeconds": 30,
      "httpMethod": "GET",
      "headers": {},
      "requestBody": null,
      "queryParams": {},
      "expectedStatusCode": 200,
      "expectedResponseBody": null,
      "isAlive": true,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 7. Алерты

**GET** `/alerts`

Возвращает список алертов.

**Параметры:**
- `page` (int, optional) - Номер страницы
- `size` (int, optional) - Размер страницы
- `serviceId` (long, optional) - ID сервиса
- `resolved` (boolean, optional) - Фильтр по статусу
- `severity` (string, optional) - Фильтр по серьезности
- `since` (string, optional) - Дата начала

**Пример запроса:**
```http
GET /api/v1/alerts?page=0&size=20&resolved=false&severity=HIGH
```

**Ответ:**
```json
{
  "content": [
    {
      "id": 1,
      "serviceId": 1,
      "message": "Service 'My API' is experiencing failures. 3 failures in the last 30 minutes.",
      "severity": "HIGH",
      "resolved": false,
      "triggeredAt": "2024-01-15T10:25:00",
      "resolvedAt": null,
      "metadata": {
        "serviceName": "My API",
        "serviceUrl": "https://api.example.com/health",
        "failureCount": "3",
        "lastResponseCode": "500"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

## Примеры использования

### Аутентификация и управление пользователями

#### 1. Регистрация нового пользователя

```javascript
// Регистрация пользователя
const registerUser = async (userData) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: userData.username,
        email: userData.email,
        password: userData.password
      })
    });

    if (response.ok) {
      const data = await response.json();
      // Сохраняем токен и данные пользователя
      localStorage.setItem('jwtToken', data.token);
      localStorage.setItem('userId', data.id);
      localStorage.setItem('username', data.username);
      localStorage.setItem('email', data.email);
      return data;
    } else {
      const error = await response.json();
      throw new Error(error.message || 'Ошибка регистрации');
    }
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
};
```

#### 2. Вход в систему

```javascript
// Вход пользователя
const loginUser = async (credentials) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: credentials.username,
        password: credentials.password
      })
    });

    if (response.ok) {
      const data = await response.json();
      // Сохраняем токен и данные пользователя
      localStorage.setItem('jwtToken', data.token);
      localStorage.setItem('userId', data.id);
      localStorage.setItem('username', data.username);
      localStorage.setItem('email', data.email);
      return data;
    } else {
      const error = await response.json();
      throw new Error(error.message || 'Ошибка входа');
    }
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};
```

#### 3. Получение профиля пользователя

```javascript
// Получение профиля текущего пользователя
const getUserProfile = async () => {
  try {
    const token = localStorage.getItem('jwtToken');
    const response = await fetch('http://localhost:8080/api/auth/profile', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });

    if (response.ok) {
      return await response.json();
    } else if (response.status === 401) {
      // Токен недействителен, перенаправляем на страницу входа
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('email');
      window.location.href = '/login';
    } else {
      throw new Error('Ошибка получения профиля');
    }
  } catch (error) {
    console.error('Profile fetch error:', error);
    throw error;
  }
};
```

#### 4. Универсальная функция для API запросов

```javascript
// Универсальная функция для API запросов с автоматической обработкой токенов
const api = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };

  const response = await fetch(`http://localhost:8080${url}`, { 
    ...options, 
    headers 
  });

  if (response.status === 401) {
    // Перенаправление на страницу входа при неавторизованном доступе
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    window.location.href = '/login';
    return;
  }

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response;
};
```

#### 5. React компонент для аутентификации

```javascript
import React, { useState } from 'react';

function Auth({ onAuthSuccess }) {
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const validateForm = () => {
    const newErrors = {};
    
    if (!isLogin) {
      if (!username.trim()) {
        newErrors.username = 'Имя пользователя обязательно';
      } else if (username.trim().length < 3) {
        newErrors.username = 'Имя пользователя должно быть не менее 3 символов';
      } else if (username.trim().length > 50) {
        newErrors.username = 'Имя пользователя должно быть не более 50 символов';
      }
    }
    
    if (!email.trim()) {
      newErrors.email = 'Email обязателен';
    } else if (!/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(email)) {
      newErrors.email = 'Неверный формат Email';
    }
    
    if (!password.trim()) {
      newErrors.password = 'Пароль обязателен';
    } else if (password.length < 6) {
      newErrors.password = 'Пароль должен быть не менее 6 символов';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null);
    
    if (validateForm()) {
      setIsLoading(true);
      try {
        const url = isLogin ? '/api/auth/login' : '/api/auth/register';
        const body = isLogin 
          ? { username: email, password } 
          : { username, email, password };

        const response = await fetch(`http://localhost:8080${url}`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(body),
        });

        const data = await response.json();

        if (response.ok) {
          setMessage({ 
            type: 'success', 
            text: isLogin ? 'Вход выполнен успешно!' : 'Регистрация прошла успешно!' 
          });
          
          // Сохраняем JWT токен и данные пользователя
          localStorage.setItem('jwtToken', data.token);
          localStorage.setItem('userId', data.id);
          localStorage.setItem('username', data.username);
          localStorage.setItem('email', data.email);
          
          onAuthSuccess();
        } else {
          setMessage({ 
            type: 'error', 
            text: data.message || 'Произошла ошибка авторизации.' 
          });
        }
      } catch (error) {
        console.error('API Error:', error);
        setMessage({ 
          type: 'error', 
          text: 'Произошла ошибка. Пожалуйста, попробуйте снова.' 
        });
      } finally {
        setIsLoading(false);
      }
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '30px' }}>
      <h2 style={{ textAlign: 'center', color: '#6D0475' }}>
        {isLogin ? 'Вход' : 'Регистрация'}
      </h2>
      
      <form onSubmit={handleSubmit}>
        {!isLogin && (
          <div style={{ marginBottom: '20px' }}>
            <label>Имя пользователя</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              style={{ width: '100%', padding: '12px', marginTop: '5px' }}
            />
            {errors.username && <p style={{ color: 'red' }}>{errors.username}</p>}
          </div>
        )}
        
        <div style={{ marginBottom: '20px' }}>
          <label>Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ width: '100%', padding: '12px', marginTop: '5px' }}
          />
          {errors.email && <p style={{ color: 'red' }}>{errors.email}</p>}
        </div>
        
        <div style={{ marginBottom: '20px' }}>
          <label>Пароль</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ width: '100%', padding: '12px', marginTop: '5px' }}
          />
          {errors.password && <p style={{ color: 'red' }}>{errors.password}</p>}
        </div>
        
        {message && (
          <p style={{ 
            color: message.type === 'success' ? 'green' : 'red', 
            textAlign: 'center' 
          }}>
            {message.text}
          </p>
        )}
        
        <button 
          type="submit" 
          disabled={isLoading}
          style={{ 
            width: '100%', 
            padding: '14px', 
            background: '#6D0475', 
            color: 'white', 
            border: 'none',
            borderRadius: '8px'
          }}
        >
          {isLogin ? (isLoading ? 'Вход...' : 'Войти') : (isLoading ? 'Регистрация...' : 'Зарегистрироваться')}
        </button>
      </form>
      
      <button 
        onClick={() => setIsLogin(!isLogin)} 
        style={{ 
          background: 'none', 
          border: 'none', 
          color: '#6D0475', 
          cursor: 'pointer',
          marginTop: '20px',
          width: '100%'
        }}
      >
        {isLogin ? 'Нет аккаунта? Зарегистрируйтесь' : 'Уже есть аккаунт? Войдите'}
      </button>
    </div>
  );
}

export default Auth;
```

### Дашборды и мониторинг

#### 1. Главный дашборд

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
      time: new Date(result.checkedAt).toLocaleTimeString(),
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

## Обработка ошибок

### Стандартные HTTP коды

- `200 OK` - Успешный запрос
- `400 Bad Request` - Неверные параметры
- `401 Unauthorized` - Требуется аутентификация
- `403 Forbidden` - Недостаточно прав
- `404 Not Found` - Ресурс не найден
- `500 Internal Server Error` - Внутренняя ошибка сервера

### Обработка ошибок в JavaScript

```javascript
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
    window.location.href = '/';
    return;
  }

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response;
};
```

## Рекомендации по производительности

### 1. Пагинация
Всегда используйте пагинацию для больших наборов данных:
```http
GET /api/v1/monitoring/results?page=0&size=50
```

### 2. Фильтрация по времени
Ограничивайте временные диапазоны для улучшения производительности:
```http
GET /api/v1/monitoring/services/1/results?since=2024-01-15T00:00:00&until=2024-01-15T23:59:59
```

### 3. Кэширование
Кэшируйте данные на frontend для уменьшения количества запросов:
```javascript
const [cachedData, setCachedData] = useState(null);
const [lastFetch, setLastFetch] = useState(null);

const fetchData = async () => {
  const now = Date.now();
  if (cachedData && lastFetch && (now - lastFetch) < 30000) { // 30 секунд кэш
    return cachedData;
  }
  
  const data = await fetchFromAPI();
  setCachedData(data);
  setLastFetch(now);
  return data;
};
```

### 4. WebSocket для real-time обновлений
Для real-time обновлений рекомендуется использовать WebSocket соединения (планируется в будущих версиях).

## Форматы дат

Все даты в API используют формат ISO 8601:
```
2024-01-15T10:30:00
2024-01-15T10:30:00.123Z
```

## Безопасность и лучшие практики

### 1. Управление JWT токенами

```javascript
// Безопасное хранение токенов
const tokenManager = {
  // Сохранение токена
  setToken: (token) => {
    localStorage.setItem('jwtToken', token);
  },
  
  // Получение токена
  getToken: () => {
    return localStorage.getItem('jwtToken');
  },
  
  // Проверка валидности токена
  isTokenValid: () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) return false;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch (error) {
      return false;
    }
  },
  
  // Удаление токена
  removeToken: () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
  }
};
```

### 2. Автоматическое обновление токенов

```javascript
// Middleware для автоматического обновления токенов
const apiWithTokenRefresh = async (url, options = {}) => {
  const token = tokenManager.getToken();
  
  if (!token || !tokenManager.isTokenValid()) {
    // Перенаправляем на страницу входа
    window.location.href = '/login';
    return;
  }
  
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    }
  });
  
  if (response.status === 401) {
    // Токен истек, удаляем его и перенаправляем
    tokenManager.removeToken();
    window.location.href = '/login';
    return;
  }
  
  return response;
};
```

### 3. Валидация на frontend

```javascript
// Валидация форм на клиенте
const validators = {
  username: (value) => {
    if (!value || value.trim().length < 3) {
      return 'Имя пользователя должно содержать минимум 3 символа';
    }
    if (value.trim().length > 50) {
      return 'Имя пользователя должно содержать максимум 50 символов';
    }
    if (!/^[a-zA-Z0-9_]+$/.test(value)) {
      return 'Имя пользователя может содержать только буквы, цифры и подчеркивания';
    }
    return null;
  },
  
  email: (value) => {
    if (!value) {
      return 'Email обязателен';
    }
    if (!/^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$/.test(value)) {
      return 'Неверный формат email';
    }
    return null;
  },
  
  password: (value) => {
    if (!value) {
      return 'Пароль обязателен';
    }
    if (value.length < 6) {
      return 'Пароль должен содержать минимум 6 символов';
    }
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
      return 'Пароль должен содержать заглавные и строчные буквы, а также цифры';
    }
    return null;
  }
};
```

### 4. Обработка ошибок

```javascript
// Централизованная обработка ошибок
const errorHandler = {
  handleApiError: (error, response) => {
    if (response) {
      switch (response.status) {
        case 400:
          return 'Неверные данные запроса';
        case 401:
          return 'Необходима авторизация';
        case 403:
          return 'Недостаточно прав доступа';
        case 404:
          return 'Ресурс не найден';
        case 409:
          return 'Конфликт данных (например, пользователь уже существует)';
        case 500:
          return 'Внутренняя ошибка сервера';
        default:
          return 'Произошла неизвестная ошибка';
      }
    }
    return 'Ошибка сети или сервера';
  }
};
```

## Лимиты и ограничения

- Максимальный размер страницы: 500 элементов
- Максимальный период для метрик: 1 год
- Максимальное количество результатов за запрос: 1000
- Rate limiting: 1000 запросов в час на пользователя
- JWT токены действительны 24 часа
- Максимальная длина пароля: 100 символов
- Максимальная длина username: 50 символов

## Поддержка

Для получения поддержки или сообщения об ошибках обращайтесь к команде разработки PingTower.
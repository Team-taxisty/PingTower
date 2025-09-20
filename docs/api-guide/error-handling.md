# Обработка ошибок

## Стандартные HTTP коды

- `200 OK` - Успешный запрос
- `201 Created` - Ресурс создан
- `204 No Content` - Успешное удаление
- `400 Bad Request` - Неверные параметры
- `401 Unauthorized` - Требуется аутентификация
- `403 Forbidden` - Недостаточно прав
- `404 Not Found` - Ресурс не найден
- `409 Conflict` - Конфликт (например, пользователь уже существует)
- `422 Unprocessable Entity` - Ошибка валидации
- `500 Internal Server Error` - Внутренняя ошибка сервера

## Обработка ошибок в JavaScript

### Базовая обработка ошибок

```javascript
const api = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };

  try {
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
      const errorData = await response.json().catch(() => ({}));
      throw new ApiError(response.status, errorData.message || 'Произошла ошибка');
    }

    return response;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError(0, 'Ошибка сети или сервера');
  }
};
```

### Класс для ошибок API

```javascript
class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

const handleApiError = (error, context = '') => {
  console.error(`API Error ${context}:`, error);
  
  switch (error.status) {
    case 400:
      alert('Неверные параметры запроса');
      break;
    case 401:
      // Обработка уже выполнена в api функции
      break;
    case 403:
      alert('У вас недостаточно прав для выполнения этого действия');
      break;
    case 404:
      alert('Запрашиваемый ресурс не найден');
      break;
    case 409:
      alert('Конфликт: ресурс уже существует');
      break;
    case 422:
      alert('Ошибка валидации данных');
      break;
    case 500:
      alert('Произошла внутренняя ошибка сервера. Попробуйте позже.');
      break;
    default:
      alert('Произошла ошибка при загрузке данных');
  }
};
```

### Обработка ошибок в React компонентах

```jsx
import React, { useState, useEffect } from 'react';

const ServiceList = () => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchServices();
  }, []);

  const fetchServices = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await api('/api/v1/services');
      const data = await response.json();
      setServices(data.content);
    } catch (err) {
      setError(err);
      handleApiError(err, 'при загрузке списка сервисов');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Загрузка сервисов...</div>;
  }

  if (error) {
    return (
      <div className="error">
        <p>Ошибка загрузки данных</p>
        <button onClick={fetchServices}>Повторить попытку</button>
      </div>
    );
  }

  return (
    <div className="services">
      {services.map(service => (
        <div key={service.id} className="service-item">
          {service.name}
        </div>
      ))}
    </div>
  );
};
```

### Глобальная обработка ошибок

```javascript
// Глобальный обработчик ошибок для fetch
const originalFetch = window.fetch;
window.fetch = async (...args) => {
  try {
    const response = await originalFetch(...args);
    
    // Логирование всех запросов
    console.log(`API Request: ${args[0]} - Status: ${response.status}`);
    
    return response;
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
};

// Глобальный обработчик необработанных ошибок
window.addEventListener('unhandledrejection', (event) => {
  console.error('Unhandled promise rejection:', event.reason);
  
  if (event.reason instanceof ApiError) {
    handleApiError(event.reason);
  }
});
```

## Валидация данных

### Валидация на клиенте

```javascript
const validateServiceData = (serviceData) => {
  const errors = {};

  // Валидация имени
  if (!serviceData.name || serviceData.name.trim().length < 3) {
    errors.name = 'Имя сервиса должно содержать минимум 3 символа';
  }

  // Валидация URL
  try {
    new URL(serviceData.url);
  } catch {
    errors.url = 'Введите корректный URL';
  }

  // Валидация интервала проверки
  if (!serviceData.checkInterval || serviceData.checkInterval < 30) {
    errors.checkInterval = 'Интервал проверки должен быть не менее 30 секунд';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};

const createService = async (serviceData) => {
  // Валидация перед отправкой
  const validation = validateServiceData(serviceData);
  if (!validation.isValid) {
    throw new ApiError(400, 'Ошибка валидации', validation.errors);
  }

  try {
    const response = await api('/api/v1/services', {
      method: 'POST',
      body: JSON.stringify(serviceData)
    });
    return await response.json();
  } catch (error) {
    handleApiError(error, 'при создании сервиса');
    throw error;
  }
};
```

### Обработка ошибок валидации

```jsx
const ServiceForm = () => {
  const [formData, setFormData] = useState({
    name: '',
    url: '',
    checkInterval: 60
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setErrors({});

    try {
      await createService(formData);
      alert('Сервис успешно создан');
      // Сброс формы или перенаправление
    } catch (error) {
      if (error.status === 400 && error.validationErrors) {
        setErrors(error.validationErrors);
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Имя сервиса:</label>
        <input
          type="text"
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
        />
        {errors.name && <span className="error">{errors.name}</span>}
      </div>
      
      <div>
        <label>URL:</label>
        <input
          type="url"
          value={formData.url}
          onChange={(e) => setFormData({...formData, url: e.target.value})}
        />
        {errors.url && <span className="error">{errors.url}</span>}
      </div>
      
      <button type="submit" disabled={submitting}>
        {submitting ? 'Создание...' : 'Создать сервис'}
      </button>
    </form>
  );
};
```

## Retry механизм

### Автоматические повторы

```javascript
const apiWithRetry = async (url, options = {}, maxRetries = 3) => {
  let lastError;
  
  for (let attempt = 1; attempt <= maxRetries; attempt++) {
    try {
      return await api(url, options);
    } catch (error) {
      lastError = error;
      
      // Не повторяем для ошибок аутентификации
      if (error.status === 401 || error.status === 403) {
        throw error;
      }
      
      // Не повторяем для ошибок валидации
      if (error.status === 400 || error.status === 422) {
        throw error;
      }
      
      if (attempt < maxRetries) {
        // Экспоненциальная задержка
        const delay = Math.pow(2, attempt) * 1000;
        console.log(`Повторная попытка ${attempt}/${maxRetries} через ${delay}ms`);
        await new Promise(resolve => setTimeout(resolve, delay));
      }
    }
  }
  
  throw lastError;
};
```

### Retry с React Query

```javascript
import { useQuery } from 'react-query';

const useServiceData = (serviceId) => {
  return useQuery(
    ['service', serviceId],
    () => apiWithRetry(`/api/v1/services/${serviceId}`),
    {
      retry: (failureCount, error) => {
        // Не повторяем для ошибок аутентификации
        if (error.status === 401 || error.status === 403) {
          return false;
        }
        // Максимум 3 попытки
        return failureCount < 3;
      },
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000)
    }
  );
};
```

## Мониторинг ошибок

### Логирование ошибок

```javascript
const logError = (error, context = {}) => {
  const errorLog = {
    timestamp: new Date().toISOString(),
    message: error.message,
    status: error.status,
    url: context.url,
    userId: localStorage.getItem('userId'),
    userAgent: navigator.userAgent,
    stack: error.stack
  };

  // Отправка в систему мониторинга (например, Sentry)
  if (window.Sentry) {
    window.Sentry.captureException(error, {
      tags: {
        component: context.component,
        action: context.action
      },
      extra: errorLog
    });
  }

  // Локальное логирование
  console.error('API Error:', errorLog);
};
```

### Уведомления об ошибках

```javascript
const showErrorNotification = (error, context = '') => {
  const notification = {
    type: 'error',
    title: 'Ошибка',
    message: getErrorMessage(error),
    duration: 5000
  };

  // Использование библиотеки уведомлений (например, react-toastify)
  if (window.toast) {
    window.toast.error(notification.message);
  } else {
    // Fallback на alert
    alert(notification.message);
  }
};

const getErrorMessage = (error) => {
  switch (error.status) {
    case 400:
      return 'Неверные параметры запроса';
    case 401:
      return 'Сессия истекла. Пожалуйста, войдите снова';
    case 403:
      return 'У вас недостаточно прав';
    case 404:
      return 'Ресурс не найден';
    case 500:
      return 'Внутренняя ошибка сервера';
    default:
      return 'Произошла ошибка при загрузке данных';
  }
};
```

## Тестирование обработки ошибок

### Мокирование ошибок

```javascript
// Мокирование для тестов
const mockApiError = (status, message) => {
  return Promise.reject(new ApiError(status, message));
};

// Тест обработки ошибок
describe('Error Handling', () => {
  it('should handle 401 error correctly', async () => {
    const mockFetch = jest.fn().mockResolvedValue({
      status: 401,
      ok: false,
      json: () => Promise.resolve({ message: 'Unauthorized' })
    });
    
    global.fetch = mockFetch;
    
    await expect(api('/test')).rejects.toThrow('Unauthorized');
  });
});
```

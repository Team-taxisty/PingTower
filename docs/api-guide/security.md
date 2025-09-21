# Безопасность и лучшие практики

## Обзор

Данный раздел содержит рекомендации по обеспечению безопасности при работе с PingTower API.

## 1. Управление JWT токенами

### Безопасное хранение токенов

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
      // Декодирование JWT токена (без проверки подписи)
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Date.now() / 1000;
      
      // Проверка времени истечения
      return payload.exp > now;
    } catch (error) {
      return false;
    }
  },
  
  // Удаление токена
  clearToken: () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
  },
  
  // Получение информации о пользователе из токена
  getUserInfo: () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) return null;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        id: payload.sub,
        username: payload.username,
        email: payload.email,
        roles: payload.roles || []
      };
    } catch (error) {
      return null;
    }
  }
};
```

### Автоматическое обновление токенов

```javascript
const tokenRefreshManager = {
  refreshToken: null,
  refreshTimer: null,
  
  // Установка refresh токена
  setRefreshToken: (token) => {
    this.refreshToken = token;
    localStorage.setItem('refreshToken', token);
  },
  
  // Автоматическое обновление токена
  setupAutoRefresh: () => {
    const token = tokenManager.getToken();
    if (!token) return;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expirationTime = payload.exp * 1000; // Конвертация в миллисекунды
      const currentTime = Date.now();
      const timeUntilExpiry = expirationTime - currentTime;
      
      // Обновляем токен за 5 минут до истечения
      const refreshTime = timeUntilExpiry - (5 * 60 * 1000);
      
      if (refreshTime > 0) {
        this.refreshTimer = setTimeout(() => {
          this.refreshAccessToken();
        }, refreshTime);
      }
    } catch (error) {
      console.error('Error setting up token refresh:', error);
    }
  },
  
  // Обновление access токена
  refreshAccessToken: async () => {
    try {
      const response = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.refreshToken}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        tokenManager.setToken(data.token);
        this.setupAutoRefresh(); // Настраиваем следующий refresh
      } else {
        // Refresh токен недействителен, перенаправляем на вход
        tokenManager.clearToken();
        window.location.href = '/login';
      }
    } catch (error) {
      console.error('Error refreshing token:', error);
      tokenManager.clearToken();
      window.location.href = '/login';
    }
  },
  
  // Очистка таймера
  clearRefreshTimer: () => {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }
};
```

## 2. Валидация на frontend

### Валидация входных данных

```javascript
const validators = {
  // Валидация email
  email: (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  },
  
  // Валидация пароля
  password: (password) => {
    const minLength = 6;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumbers = /\d/.test(password);
    
    return {
      isValid: password.length >= minLength,
      errors: [
        ...(password.length < minLength ? ['Пароль должен содержать минимум 6 символов'] : []),
        ...(hasUpperCase ? [] : ['Пароль должен содержать заглавные буквы']),
        ...(hasLowerCase ? [] : ['Пароль должен содержать строчные буквы']),
        ...(hasNumbers ? [] : ['Пароль должен содержать цифры'])
      ]
    };
  },
  
  // Валидация URL
  url: (url) => {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  },
  
  // Валидация имени пользователя
  username: (username) => {
    const minLength = 3;
    const maxLength = 50;
    const validChars = /^[a-zA-Z0-9_]+$/;
    
    if (username.length < minLength) {
      return { isValid: false, error: 'Имя пользователя должно содержать минимум 3 символа' };
    }
    
    if (username.length > maxLength) {
      return { isValid: false, error: 'Имя пользователя не должно превышать 50 символов' };
    }
    
    if (!validChars.test(username)) {
      return { isValid: false, error: 'Имя пользователя может содержать только буквы, цифры и подчеркивания' };
    }
    
    return { isValid: true };
  }
};

// Универсальная функция валидации
const validateForm = (formData, rules) => {
  const errors = {};
  
  for (const [field, value] of Object.entries(formData)) {
    if (rules[field]) {
      const validation = rules[field](value);
      if (!validation.isValid) {
        errors[field] = validation.error || validation.errors;
      }
    }
  }
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

### Санитизация данных

```javascript
const sanitizers = {
  // Очистка HTML
  html: (input) => {
    const div = document.createElement('div');
    div.textContent = input;
    return div.innerHTML;
  },
  
  // Очистка SQL (для демонстрации, в реальности используйте параметризованные запросы)
  sql: (input) => {
    return input.replace(/['"\\;]/g, '');
  },
  
  // Очистка URL
  url: (input) => {
    try {
      const url = new URL(input);
      return url.toString();
    } catch {
      return '';
    }
  },
  
  // Очистка текста
  text: (input) => {
    return input.trim().replace(/[<>]/g, '');
  }
};

// Применение санитизации к форме
const sanitizeFormData = (formData) => {
  const sanitized = {};
  
  for (const [key, value] of Object.entries(formData)) {
    if (typeof value === 'string') {
      sanitized[key] = sanitizers.text(value);
    } else {
      sanitized[key] = value;
    }
  }
  
  return sanitized;
};
```

## 3. Обработка ошибок безопасности

### Безопасная обработка ошибок

```javascript
const secureErrorHandler = {
  // Обработка ошибок аутентификации
  handleAuthError: (error) => {
    if (error.status === 401) {
      // Токен истек или недействителен
      tokenManager.clearToken();
      tokenRefreshManager.clearRefreshTimer();
      
      // Перенаправление на страницу входа
      window.location.href = '/login';
    } else if (error.status === 403) {
      // Недостаточно прав
      alert('У вас недостаточно прав для выполнения этого действия');
    }
  },
  
  // Обработка ошибок валидации
  handleValidationError: (error) => {
    if (error.status === 422) {
      // Ошибка валидации на сервере
      return error.validationErrors || {};
    }
    return {};
  },
  
  // Логирование ошибок (без чувствительных данных)
  logError: (error, context = {}) => {
    const safeError = {
      message: error.message,
      status: error.status,
      timestamp: new Date().toISOString(),
      context: {
        component: context.component,
        action: context.action
      }
      // НЕ логируем токены, пароли и другие чувствительные данные
    };
    
    console.error('API Error:', safeError);
    
    // Отправка в систему мониторинга (без чувствительных данных)
    if (window.Sentry) {
      window.Sentry.captureException(error, {
        tags: safeError.context,
        extra: safeError
      });
    }
  }
};
```

## 4. Content Security Policy (CSP)

### Настройка CSP заголовков

```html
<meta http-equiv="Content-Security-Policy" content="
  default-src 'self';
  script-src 'self' 'unsafe-inline' 'unsafe-eval';
  style-src 'self' 'unsafe-inline';
  img-src 'self' data: https:;
  connect-src 'self' ws: wss:;
  font-src 'self';
  object-src 'none';
  base-uri 'self';
  form-action 'self';
">
```

### Обработка CSP нарушений

```javascript
// Мониторинг CSP нарушений
document.addEventListener('securitypolicyviolation', (event) => {
  console.warn('CSP Violation:', {
    blockedURI: event.blockedURI,
    violatedDirective: event.violatedDirective,
    originalPolicy: event.originalPolicy
  });
  
  // Отправка в систему мониторинга
  if (window.Sentry) {
    window.Sentry.captureMessage('CSP Violation', {
      level: 'warning',
      extra: {
        blockedURI: event.blockedURI,
        violatedDirective: event.violatedDirective
      }
    });
  }
});
```

## 5. Защита от XSS

### Экранирование HTML

```javascript
const escapeHtml = (text) => {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  
  return text.replace(/[&<>"']/g, (m) => map[m]);
};

// Безопасный рендеринг пользовательского контента
const SafeText = ({ children }) => {
  return <span dangerouslySetInnerHTML={{ __html: escapeHtml(children) }} />;
};
```

### Валидация и санитизация пользовательского ввода

```javascript
const sanitizeUserInput = (input) => {
  if (typeof input !== 'string') return input;
  
  // Удаление потенциально опасных тегов
  const dangerousTags = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
  const sanitized = input.replace(dangerousTags, '');
  
  // Экранирование HTML
  return escapeHtml(sanitized);
};
```

## 6. Защита от CSRF

### CSRF токены

```javascript
const csrfManager = {
  token: null,
  
  // Получение CSRF токена
  getToken: async () => {
    if (this.token) return this.token;
    
    try {
      const response = await fetch('/api/csrf-token', {
        credentials: 'include'
      });
      
      if (response.ok) {
        const data = await response.json();
        this.token = data.token;
        return this.token;
      }
    } catch (error) {
      console.error('Error getting CSRF token:', error);
    }
    
    return null;
  },
  
  // Включение CSRF токена в запросы
  addToRequest: async (options = {}) => {
    const token = await this.getToken();
    
    if (token) {
      options.headers = {
        ...options.headers,
        'X-CSRF-Token': token
      };
    }
    
    return options;
  }
};

// Модифицированная функция API с CSRF защитой
const secureApi = async (url, options = {}) => {
  const secureOptions = await csrfManager.addToRequest(options);
  return api(url, secureOptions);
};
```

## 7. Безопасное хранение данных

### Шифрование чувствительных данных

```javascript
// Простое шифрование для демонстрации (в продакшене используйте библиотеки типа crypto-js)
const simpleEncrypt = (text, key) => {
  let result = '';
  for (let i = 0; i < text.length; i++) {
    result += String.fromCharCode(text.charCodeAt(i) ^ key.charCodeAt(i % key.length));
  }
  return btoa(result);
};

const simpleDecrypt = (encryptedText, key) => {
  const text = atob(encryptedText);
  let result = '';
  for (let i = 0; i < text.length; i++) {
    result += String.fromCharCode(text.charCodeAt(i) ^ key.charCodeAt(i % key.length));
  }
  return result;
};

// Безопасное хранение чувствительных данных
const secureStorage = {
  setItem: (key, value) => {
    const encrypted = simpleEncrypt(JSON.stringify(value), 'your-secret-key');
    localStorage.setItem(key, encrypted);
  },
  
  getItem: (key) => {
    const encrypted = localStorage.getItem(key);
    if (!encrypted) return null;
    
    try {
      const decrypted = simpleDecrypt(encrypted, 'your-secret-key');
      return JSON.parse(decrypted);
    } catch (error) {
      console.error('Error decrypting data:', error);
      return null;
    }
  },
  
  removeItem: (key) => {
    localStorage.removeItem(key);
  }
};
```

## 8. Аудит безопасности

### Логирование действий пользователя

```javascript
const auditLogger = {
  logAction: (action, details = {}) => {
    const auditEntry = {
      timestamp: new Date().toISOString(),
      userId: tokenManager.getUserInfo()?.id,
      username: tokenManager.getUserInfo()?.username,
      action,
      details,
      userAgent: navigator.userAgent,
      ip: 'client-side' // IP будет получен на сервере
    };
    
    // Отправка в систему аудита
    fetch('/api/audit/log', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${tokenManager.getToken()}`
      },
      body: JSON.stringify(auditEntry)
    }).catch(error => {
      console.error('Error logging audit entry:', error);
    });
  }
};

// Использование аудита
const secureApiCall = async (url, options = {}) => {
  const method = options.method || 'GET';
  
  try {
    const response = await api(url, options);
    
    // Логирование успешных действий
    auditLogger.logAction('API_CALL_SUCCESS', {
      url,
      method,
      status: response.status
    });
    
    return response;
  } catch (error) {
    // Логирование неудачных действий
    auditLogger.logAction('API_CALL_FAILED', {
      url,
      method,
      error: error.message,
      status: error.status
    });
    
    throw error;
  }
};
```

## 9. Рекомендации по безопасности

### Общие принципы

1. **Никогда не доверяйте клиенту** - вся валидация должна дублироваться на сервере
2. **Используйте HTTPS** для всех запросов в продакшене
3. **Регулярно обновляйте зависимости** для исправления уязвимостей
4. **Используйте принцип минимальных привилегий** - давайте пользователям только необходимые права
5. **Логируйте все важные действия** для аудита

### Настройки браузера

1. **Включите HSTS** (HTTP Strict Transport Security)
2. **Настройте SameSite cookies** для защиты от CSRF
3. **Используйте Secure флаг** для cookies
4. **Настройте CORS** правильно на сервере

### Мониторинг безопасности

1. **Отслеживайте подозрительную активность**
2. **Мониторьте неудачные попытки входа**
3. **Анализируйте логи на предмет аномалий**
4. **Используйте системы обнаружения вторжений**

## 10. Тестирование безопасности

### Тестирование XSS

```javascript
// Тестовые векторы XSS
const xssTestVectors = [
  '<script>alert("XSS")</script>',
  '<img src="x" onerror="alert(\'XSS\')">',
  'javascript:alert("XSS")',
  '<svg onload="alert(\'XSS\')">',
  '"><script>alert("XSS")</script>'
];

// Тестирование защиты от XSS
const testXSSProtection = (input) => {
  const sanitized = sanitizeUserInput(input);
  const hasScript = /<script/i.test(sanitized);
  const hasOnError = /onerror/i.test(sanitized);
  
  return {
    isProtected: !hasScript && !hasOnError,
    sanitized
  };
};
```

### Тестирование CSRF

```javascript
// Тестирование CSRF защиты
const testCSRFProtection = async () => {
  try {
    // Попытка запроса без CSRF токена
    const response = await fetch('/api/v1/services', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ name: 'test' })
    });
    
    return response.status === 403; // Должен вернуть 403 Forbidden
  } catch (error) {
    return false;
  }
};
```

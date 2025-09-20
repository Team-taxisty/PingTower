# Аутентификация и управление пользователями

## Обзор

PingTower использует JWT (JSON Web Token) для аутентификации пользователей. Все API запросы требуют JWT токен в заголовке Authorization.

## Базовый URL

```
http://localhost:8080
```

## Аутентификация

Все API запросы требуют JWT токен в заголовке Authorization:

```http
Authorization: Bearer <your-jwt-token>
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
  "roles": ["USER"],
  "createdAt": "2024-01-15T10:30:00",
  "lastLoginAt": "2024-01-15T12:00:00"
}
```

**Ошибки:**
- `401 Unauthorized` - недействительный или отсутствующий токен

## Примеры использования

### 1. Регистрация нового пользователя

```javascript
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
      // Сохраняем токен в localStorage
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
    console.error('Ошибка регистрации:', error);
    throw error;
  }
};
```

### 2. Вход в систему

```javascript
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
    console.error('Ошибка входа:', error);
    throw error;
  }
};
```

### 3. Получение профиля пользователя

```javascript
const getUserProfile = async () => {
  try {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      throw new Error('Токен не найден');
    }

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
    console.error('Ошибка получения профиля:', error);
    throw error;
  }
};
```

### 4. Универсальная функция для API запросов

```javascript
const apiRequest = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  
  const defaultOptions = {
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers,
    },
  };

  const response = await fetch(`http://localhost:8080${url}`, {
    ...defaultOptions,
    ...options,
  });

  if (response.status === 401) {
    // Токен недействителен, очищаем localStorage и перенаправляем
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    window.location.href = '/login';
    return;
  }

  return response;
};
```

### 5. React компонент для аутентификации

```jsx
import React, { useState, useEffect } from 'react';

const AuthComponent = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const token = localStorage.getItem('jwtToken');
      if (token) {
        const userProfile = await getUserProfile();
        setUser(userProfile);
        setIsAuthenticated(true);
      }
    } catch (error) {
      console.error('Ошибка проверки аутентификации:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async (credentials) => {
    try {
      setLoading(true);
      const userData = await loginUser(credentials);
      setUser(userData);
      setIsAuthenticated(true);
    } catch (error) {
      console.error('Ошибка входа:', error);
      alert('Ошибка входа: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    setUser(null);
    setIsAuthenticated(false);
  };

  if (loading) {
    return <div>Загрузка...</div>;
  }

  if (!isAuthenticated) {
    return <LoginForm onLogin={handleLogin} />;
  }

  return (
    <div>
      <h1>Добро пожаловать, {user.username}!</h1>
      <button onClick={handleLogout}>Выйти</button>
    </div>
  );
};

const LoginForm = ({ onLogin }) => {
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onLogin(credentials);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Имя пользователя:</label>
        <input
          type="text"
          value={credentials.username}
          onChange={(e) => setCredentials({
            ...credentials,
            username: e.target.value
          })}
          required
        />
      </div>
      <div>
        <label>Пароль:</label>
        <input
          type="password"
          value={credentials.password}
          onChange={(e) => setCredentials({
            ...credentials,
            password: e.target.value
          })}
          required
        />
      </div>
      <button type="submit">Войти</button>
    </form>
  );
};

export default AuthComponent;
```

## Безопасность

### Управление JWT токенами

1. **Хранение токенов**: Токены хранятся в localStorage браузера
2. **Автоматическое обновление**: Токены имеют ограниченное время жизни
3. **Обработка истечения**: При истечении токена пользователь автоматически перенаправляется на страницу входа

### Валидация на frontend

```javascript
const validateUserInput = (userData) => {
  const errors = {};

  // Валидация username
  if (!userData.username || userData.username.length < 3) {
    errors.username = 'Имя пользователя должно содержать минимум 3 символа';
  }

  // Валидация email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!userData.email || !emailRegex.test(userData.email)) {
    errors.email = 'Введите корректный email адрес';
  }

  // Валидация пароля
  if (!userData.password || userData.password.length < 6) {
    errors.password = 'Пароль должен содержать минимум 6 символов';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

## Лимиты и ограничения

- **Время жизни токена**: 24 часа
- **Максимальная длина username**: 50 символов
- **Минимальная длина пароля**: 6 символов
- **Максимальное количество попыток входа**: 5 в минуту

import React, { useState } from 'react';
import api from '../utils/api';

function Auth({ onAuthSuccess }) {
  const [isLogin, setIsLogin] = useState(true);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const validateForm = () => {
    const newErrors = {};
    if (!isLogin) {
      if (!name.trim()) {
        newErrors.name = 'Имя пользователя обязательно';
      } else if (name.trim().length < 3) {
        newErrors.name = 'Имя пользователя должно быть не менее 3 символов';
      } else if (name.trim().length > 50) {
        newErrors.name = 'Имя пользователя должно быть не более 50 символов';
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

  const handleToggle = () => {
    setIsLogin(!isLogin);
    setName('');
    setEmail('');
    setPassword('');
    setErrors({}); // Clear errors on toggle
    setMessage(null); // Clear messages on toggle
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage(null); // Clear previous messages
    if (validateForm()) {
      setIsLoading(true);
      try {
        const url = isLogin ? '/v1/api/auth/login' : '/v1/api/auth/register';
        const body = isLogin ? { email: email, password } : { username: name, email, password };

        const response = await api(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(body),
        });

        const data = await response.json();

        if (response.ok) {
          setMessage({ type: 'success', text: isLogin ? 'Вход выполнен успешно!' : 'Регистрация прошла успешно!' });
          // Save JWT token and user details
          localStorage.setItem('jwtToken', data.token);
          localStorage.setItem('userId', data.id);
          localStorage.setItem('username', data.username);
          localStorage.setItem('email', data.email);
          onAuthSuccess();
        } else {
          setMessage({ type: 'error', text: data.message || 'Произошла ошибка авторизации.' });
        }
      } catch (error) {
        console.error('API Error:', error);
        setMessage({ type: 'error', text: 'Произошла ошибка. Пожалуйста, попробуйте снова.' });
      } finally {
        setIsLoading(false);
      }
    }
  };

  const formContainerStyle = {
    maxWidth: '400px',
    margin: '50px auto',
    padding: '30px',
    borderRadius: '12px',
    background: '#ffffff',
    boxShadow: '0 4px 12px rgba(109, 4, 117, 0.1)',
    fontFamily: 'Arial, sans-serif',
  };

  const titleStyle = {
    textAlign: 'center',
    color: '#6D0475',
    marginBottom: '25px',
    fontSize: '28px',
    fontWeight: '700',
  };

  const formGroupStyle = {
    marginBottom: '20px',
  };

  const labelStyle = {
    display: 'block',
    marginBottom: '8px',
    color: '#333',
    fontSize: '15px',
    fontWeight: '600',
  };

  const inputStyle = {
    width: 'calc(100% - 24px)',
    padding: '12px',
    border: '1px solid #E5B8E8',
    borderRadius: '8px',
    fontSize: '16px',
    transition: 'border-color 0.2s ease',
  };

  const errorTextStyle = {
    color: '#ff4d4f',
    fontSize: '12px',
    marginTop: '5px',
  };

  const buttonStyle = {
    width: '100%',
    padding: '14px',
    borderRadius: '8px',
    border: 'none',
    background: '#6D0475',
    color: '#ffffff',
    fontSize: '18px',
    fontWeight: '600',
    cursor: isLoading ? 'not-allowed' : 'pointer',
    transition: 'background-color 0.2s ease',
    marginTop: '10px',
    opacity: isLoading ? 0.7 : 1,
  };

  const toggleButtonStyle = {
    background: 'none',
    border: 'none',
    color: '#6D0475',
    cursor: 'pointer',
    fontSize: '15px',
    marginTop: '20px',
    textAlign: 'center',
    display: 'block',
    width: '100%',
  };

  return (
    <div style={formContainerStyle}>
      <h2 style={titleStyle}>{isLogin ? 'Вход' : 'Регистрация'}</h2>
      <form onSubmit={handleSubmit}>
        {!isLogin && (
          <div style={formGroupStyle}>
            <label htmlFor="name" style={labelStyle}>Имя пользователя</label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              style={inputStyle}
            />
            {errors.name && <p style={errorTextStyle}>{errors.name}</p>}
          </div>
        )}
        <div style={formGroupStyle}>
          <label htmlFor="email" style={labelStyle}>Email</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={inputStyle}
          />
          {errors.email && <p style={errorTextStyle}>{errors.email}</p>}
        </div>
        <div style={formGroupStyle}>
          <label htmlFor="password" style={labelStyle}>Пароль</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={inputStyle}
          />
          {errors.password && <p style={errorTextStyle}>{errors.password}</p>}
        </div>
        {message && (
          <p style={{ color: message.type === 'success' ? 'green' : 'red', textAlign: 'center', marginBottom: '15px' }}>
            {message.text}
          </p>
        )}
        <button type="submit" style={buttonStyle} disabled={isLoading}>{isLogin ? (isLoading ? 'Вход...' : 'Войти') : (isLoading ? 'Регистрация...' : 'Зарегистрироваться')}</button>
      </form>
      <button onClick={handleToggle} style={toggleButtonStyle} disabled={isLoading}>
        {isLogin ? 'Нет аккаунта? Зарегистрируйтесь' : 'Уже есть аккаунт? Войдите'}
      </button>
    </div>
  );
}

export default Auth;

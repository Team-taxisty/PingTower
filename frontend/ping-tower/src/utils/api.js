// В dev-режиме мы обращаемся к nginx на порт 80, в prod - относительные пути
const BASE_URL = process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : '';

const api = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };
  
  // Все запросы теперь идут через nginx proxy
  const response = await fetch(`${BASE_URL}${url}`, { ...options, headers });
  
  if (response.status === 401) {
    // Handle unauthorized access, e.g., redirect to login
    console.log('Unauthorized access, redirecting to login...');
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    // You might want to trigger a global logout state here if App.js manages it
    window.location.href = '/'; // Simple redirect to root, which should lead to Auth page
  }
  return response;
};

export default api;
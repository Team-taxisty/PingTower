// В dev режиме обращаемся к nginx на порту 80, в prod - относительные пути
const BASE_URL = process.env.NODE_ENV === 'development' ? 'http://localhost' : '';

const api = async (url, options = {}) => {
  const token = localStorage.getItem('jwtToken');
  const headers = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };
  
  const response = await fetch(`${BASE_URL}${url}`, { ...options, headers });
  
  if (response.status === 401) {
    console.log('Unauthorized access, redirecting to login...');
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    window.location.href = '/';
  }
  return response;
};

export default api;
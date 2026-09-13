const BASE_URL = 'http://localhost:8080/api/v1';

export const getStoredAuth = () => {
  try {
    const raw = localStorage.getItem('fleetflow_auth');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const setStoredAuth = (auth) => {
  if (auth) {
    localStorage.setItem('fleetflow_auth', JSON.stringify(auth));
  } else {
    localStorage.removeItem('fleetflow_auth');
  }
};

async function request(endpoint, options = {}) {
  const auth = getStoredAuth();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (auth && auth.accessToken) {
    headers['Authorization'] = `Bearer ${auth.accessToken}`;
  }

  const url = `${BASE_URL}${endpoint}`;
  try {
    const res = await fetch(url, { ...options, headers });
    const data = await res.json();

    if (res.status === 401) {
      setStoredAuth(null);
      window.dispatchEvent(new Event('fleetflow_unauthorized'));
      throw new Error('Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.');
    }

    if (!res.ok) {
      throw new Error(data.message || data.error?.details || `Lỗi HTTP ${res.status}`);
    }
    return data.data !== undefined ? data.data : data;
  } catch (err) {
    console.error(`API Error on ${endpoint}:`, err);
    throw err;
  }
}

export const api = {
  // Auth
  login: async (username, password) => {
    const data = await request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    setStoredAuth(data);
    return data;
  },

  logout: () => {
    setStoredAuth(null);
  },

  // Dashboard
  getDashboard: () => request('/admin/dashboard/stats'),

  // Shipments
  getShipmentsByStatus: (status, page = 0, size = 20) =>
    request(`/shipments/status/${status}?page=${page}&size=${size}`),

  getShipmentById: (id) => request(`/shipments/${id}`),

  createShipment: (data) =>
    request('/shipments', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  updateStatus: (id, status, note) =>
    request(`/shipments/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status, note }),
    }),

  // Dispatch & Concurrency
  assignDriver: (shipmentId, driverId, expectedVersion) =>
    request(`/dispatch/${shipmentId}/assign`, {
      method: 'POST',
      body: JSON.stringify({ driverId, expectedVersion }),
    }),

  unassignDriver: (shipmentId) =>
    request(`/dispatch/${shipmentId}/unassign`, {
      method: 'DELETE',
    }),

  // Pricing Engine
  estimatePricing: (data) =>
    request('/pricing/estimate', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  // Driver Portal
  getMyDriverTasks: (driverId = null, page = 0, size = 20) => {
    const query = driverId ? `driverId=${driverId}&page=${page}&size=${size}` : `page=${page}&size=${size}`;
    return request(`/shipments/driver/my-tasks?${query}`);
  },

  recordDeliveryAttempt: (shipmentId, attemptData) =>
    request(`/shipments/${shipmentId}/attempts`, {
      method: 'POST',
      body: JSON.stringify(attemptData),
    }),

  pingDriverLocation: (driverId, lat, lng, speed = 30) =>
    request(`/drivers/${driverId}/location`, {
      method: 'POST',
      body: JSON.stringify({ latitude: lat, longitude: lng, speed }),
    }),

  // COD & Settlement
  getPendingCod: (driverId) => request(`/cod/driver/${driverId}/pending`),

  settleCod: (driverId, idempotencyKey, note) =>
    request('/cod/settle', {
      method: 'POST',
      headers: { 'Idempotency-Key': idempotencyKey },
      body: JSON.stringify({ driverId, idempotencyKey, note }),
    }),

  // Public Tracking
  trackPublic: (trackingCode) => request(`/public/tracking/${trackingCode}`),
};


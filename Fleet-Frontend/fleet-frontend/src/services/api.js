import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Vehicle APIs
export const getVehicles = () => api.get('/vehicles');
export const getVehicleById = (id) => api.get(`/vehicles/${id}`);
export const createVehicle = (vehicle) => api.post('/vehicles', vehicle);
export const updateVehicle = (id, vehicle) => api.put(`/vehicles/${id}`, vehicle);
export const deleteVehicle = (id) => api.delete(`/vehicles/${id}`);

// Driver APIs
export const getDrivers = () => api.get('/drivers');
export const getDriverById = (id) => api.get(`/drivers/${id}`);
export const createDriver = (driver) => api.post('/drivers', driver);
export const updateDriver = (id, driver) => api.put(`/drivers/${id}`, driver);
export const deleteDriver = (id) => api.delete(`/drivers/${id}`);

// Delivery APIs
export const getDeliveries = () => api.get('/deliveries');
export const getDeliveryById = (id) => api.get(`/deliveries/${id}`);
export const createDelivery = (delivery) => api.post('/deliveries', delivery);
export const updateDelivery = (id, delivery) => api.put(`/deliveries/${id}`, delivery);
export const deleteDelivery = (id) => api.delete(`/deliveries/${id}`);

// Dispatch APIs
export const optimizeRoute = (data) => api.post('/dispatch/create-and-optimize', data);

// Mobile APIs
export const updateDeliveryStatus = (deliveryId, newStatus, vehicleId) => 
  api.put(`/driver/mobile/delivery/${deliveryId}/status?newStatus=${newStatus}&vehicleId=${vehicleId}`);

export const updateLocation = (driverId, latitude, longitude) => 
  api.post(`/driver/mobile/${driverId}/location?latitude=${latitude}&longitude=${longitude}`);

export const getTodayRoute = (driverId) => api.get(`/driver/mobile/${driverId}/today-route`);

export const cancelDelivery = (deliveryId, reason) => 
  api.post(`/driver/mobile/delivery/${deliveryId}/cancel?reason=${reason}`);

export default api;
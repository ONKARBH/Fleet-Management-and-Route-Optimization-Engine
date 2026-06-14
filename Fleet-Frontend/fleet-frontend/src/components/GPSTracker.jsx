import React, { useState, useEffect } from 'react';
import { FaMapMarkerAlt, FaTruck, FaLocationArrow } from 'react-icons/fa';
import { getVehicles, updateLocation, getTodayRoute } from '../services/api';

const GPSTracker = () => {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [route, setRoute] = useState([]);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadVehicles();
  }, []);

  const loadVehicles = async () => {
    try {
      const response = await getVehicles();
      setVehicles(response.data);
    } catch (error) {
      console.error('Error loading vehicles:', error);
    }
  };

  const loadDriverRoute = async (vehicleId) => {
    try {
      // Assuming driver ID is same as vehicle ID for demo
      const response = await getTodayRoute(1);
      setRoute(response.data);
    } catch (error) {
      console.error('Error loading route:', error);
    }
  };

  const handleVehicleSelect = (e) => {
    const vehicleId = e.target.value;
    setSelectedVehicle(vehicleId);
    if (vehicleId) {
      loadDriverRoute(vehicleId);
    }
  };

  const handleUpdateLocation = async () => {
    if (!selectedVehicle) {
      alert('Please select a vehicle');
      return;
    }
    if (!latitude || !longitude) {
      alert('Please enter latitude and longitude');
      return;
    }

    try {
      const response = await updateLocation(1, parseFloat(latitude), parseFloat(longitude));
      setMessage('Location updated successfully!');
      if (response.data.distanceToNextDelivery) {
        setMessage(`Location updated! ${response.data.distanceToNextDelivery.toFixed(2)} km to next delivery`);
      }
      setTimeout(() => setMessage(''), 3000);
    } catch (error) {
      console.error('Error updating location:', error);
      setMessage('Error updating location');
    }
  };

  const getCurrentLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLatitude(position.coords.latitude.toFixed(6));
          setLongitude(position.coords.longitude.toFixed(6));
        },
        (error) => {
          console.error('Error getting location:', error);
          alert('Unable to get your location. Please enter manually.');
        }
      );
    } else {
      alert('Geolocation is not supported by your browser');
    }
  };

  return (
    <div className="tracker-container">
      <h2><FaMapMarkerAlt /> GPS Tracking</h2>
      
      <div className="tracker-controls">
        <div className="form-group">
          <label><FaTruck /> Select Vehicle</label>
          <select value={selectedVehicle} onChange={handleVehicleSelect}>
            <option value="">Select a vehicle...</option>
            {vehicles.map(vehicle => (
              <option key={vehicle.id} value={vehicle.id}>
                {vehicle.licensePlate} - {vehicle.model}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Latitude</label>
          <input
            type="number"
            step="0.000001"
            value={latitude}
            onChange={(e) => setLatitude(e.target.value)}
            placeholder="19.0760"
          />
        </div>

        <div className="form-group">
          <label>Longitude</label>
          <input
            type="number"
            step="0.000001"
            value={longitude}
            onChange={(e) => setLongitude(e.target.value)}
            placeholder="72.8777"
          />
        </div>

        <div className="tracker-buttons">
          <button className="btn-secondary" onClick={getCurrentLocation}>
            <FaLocationArrow /> Use My Location
          </button>
          <button className="btn-primary" onClick={handleUpdateLocation}>
            Update Location
          </button>
        </div>
      </div>

      {message && <div className="tracker-message">{message}</div>}

      {route.length > 0 && (
        <div className="today-route">
          <h3>Today's Route</h3>
          <div className="route-list">
            {route.map((delivery, index) => (
              <div key={delivery.id} className={`route-item ${delivery.status === 'DELIVERED' ? 'completed' : ''}`}>
                <div className="route-number">{index + 1}</div>
                <div className="route-details">
                  <strong>{delivery.customerName}</strong>
                  <p>{delivery.deliveryAddress}</p>
                  <span className="route-status">{delivery.status}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="map-placeholder">
        <div className="map-info">
          <FaMapMarkerAlt />
          <p>GPS coordinates updated in real-time</p>
          <small>Latitude: {latitude || 'Not set'} | Longitude: {longitude || 'Not set'}</small>
        </div>
      </div>
    </div>
  );
};

export default GPSTracker;
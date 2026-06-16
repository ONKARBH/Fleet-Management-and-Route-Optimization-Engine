import React, { useState, useEffect, useRef } from 'react';
import { 
  FaMapMarkerAlt, 
  FaTruck, 
  FaLocationArrow, 
  FaPlay, 
  FaStop, 
  FaSync, 
  FaRoad, 
  FaSatellite,
  FaHistory,
  FaCompass,
  FaChartLine
} from 'react-icons/fa';
import { getVehicles, getTodayRoute } from '../services/api';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const GPSTracker = () => {
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [route, setRoute] = useState([]);
  const [currentLocation, setCurrentLocation] = useState(null);
  const [speed, setSpeed] = useState(0);
  const [simulating, setSimulating] = useState(false);
  const [distanceToNext, setDistanceToNext] = useState(null);
  const [trackingHistory, setTrackingHistory] = useState([]);
  const intervalRef = useRef(null);

  useEffect(() => {
    loadVehicles();
    return () => stopLocationTracking();
  }, []);

  useEffect(() => {
    if (selectedVehicle) {
      loadDriverRoute();
      startLocationTracking();
    }
  }, [selectedVehicle]);

  const loadVehicles = async () => {
    try {
      const response = await getVehicles();
      setVehicles(response.data);
    } catch (error) {
      console.error('Error loading vehicles:', error);
    }
  };

  const loadDriverRoute = async () => {
    try {
      const response = await getTodayRoute(1);
      setRoute(response.data);
    } catch (error) {
      console.error('Error loading route:', error);
    }
  };

  const startLocationTracking = () => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    
    intervalRef.current = setInterval(async () => {
      if (selectedVehicle) {
        await fetchCurrentLocation();
      }
    }, 3000);
  };

  const stopLocationTracking = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  };

  const fetchCurrentLocation = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/driver/mobile/${selectedVehicle}/current-location`);
      const data = response.data;
      if (data.latitude && data.longitude) {
        setCurrentLocation({ lat: data.latitude, lng: data.longitude });
        setSpeed(data.speed || 0);
        
        setTrackingHistory(prev => [...prev.slice(-20), { 
          lat: data.latitude, 
          lng: data.longitude, 
          time: new Date().toLocaleTimeString() 
        }]);
        
        if (route.length > 0) {
          const nextDelivery = route.find(d => d.status !== 'DELIVERED');
          if (nextDelivery && data.latitude && data.longitude) {
            const distance = calculateDistance(
              data.latitude, data.longitude,
              nextDelivery.latitude, nextDelivery.longitude
            );
            setDistanceToNext(distance);
          }
        }
      }
    } catch (error) {
      console.error('Error fetching location:', error);
    }
  };

  const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371;
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  };

  const updateManualLocation = async () => {
    if (!currentLocation) return;
    try {
      await axios.post(`${API_BASE_URL}/driver/mobile/${selectedVehicle}/location`, null, {
        params: { latitude: currentLocation.lat, longitude: currentLocation.lng }
      });
      alert('Location updated manually!');
      await fetchCurrentLocation();
    } catch (error) {
      console.error('Error updating location:', error);
      alert('Error updating location. Make sure backend is running.');
    }
  };

  const startSimulation = async () => {
    setSimulating(true);
    try {
      await axios.post(`${API_BASE_URL}/driver/mobile/${selectedVehicle}/simulate-location?continuous=true`);
      if (intervalRef.current) clearInterval(intervalRef.current);
      intervalRef.current = setInterval(fetchCurrentLocation, 2000);
    } catch (error) {
      console.error('Error starting simulation:', error);
      alert('Error starting simulation. Make sure backend is running.');
    }
  };

  const stopSimulation = async () => {
    setSimulating(false);
    try {
      await axios.post(`${API_BASE_URL}/driver/mobile/${selectedVehicle}/stop-simulation`);
    } catch (error) {
      console.error('Error stopping simulation:', error);
    }
  };

  const getCurrentPosition = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setCurrentLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude
          });
          updateManualLocation();
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
      <h2><FaMapMarkerAlt /> GPS Tracking System</h2>
      
      <div className="tracker-controls">
        <div className="form-group">
          <label><FaTruck /> Select Vehicle</label>
          <select value={selectedVehicle} onChange={(e) => setSelectedVehicle(e.target.value)}>
            <option value="">Select a vehicle...</option>
            {vehicles.map(vehicle => (
              <option key={vehicle.id} value={vehicle.id}>
                {vehicle.licensePlate} - {vehicle.model}
              </option>
            ))}
          </select>
        </div>

        {currentLocation && (
          <div className="location-info">
            <div className="location-card">
              <h4><FaSatellite /> Current Location</h4>
              <p><strong>Latitude:</strong> {currentLocation.lat?.toFixed(6)}</p>
              <p><strong>Longitude:</strong> {currentLocation.lng?.toFixed(6)}</p>
              <p><strong>Speed:</strong> {speed?.toFixed(1)} km/h</p>
              {distanceToNext && (
                <p><strong>Distance to next delivery:</strong> {distanceToNext.toFixed(2)} km</p>
              )}
            </div>
          </div>
        )}

        <div className="tracker-buttons">
          <button className="btn-secondary" onClick={getCurrentPosition}>
            <FaLocationArrow /> Use My Location
          </button>
          <button className="btn-primary" onClick={updateManualLocation}>
            <FaSync /> Update Location
          </button>
          {!simulating ? (
            <button className="btn-success" onClick={startSimulation}>
              <FaPlay /> Start Simulation
            </button>
          ) : (
            <button className="btn-danger" onClick={stopSimulation}>
              <FaStop /> Stop Simulation
            </button>
          )}
        </div>
      </div>

      {/* Map Visualization */}
      <div className="map-container">
        <div className="map-visualization">
          <div className="map-header">
            <h4><FaRoad /> Live Vehicle Tracking</h4>
            {simulating && <span className="simulation-badge">SIMULATING</span>}
          </div>
          
          {currentLocation ? (
            <div className="map-coordinates">
              <div className="coordinate-card">
                <div className="coordinate-label">Current Position</div>
                <div className="coordinate-value">
                  <FaCompass /> {currentLocation.lat?.toFixed(6)}, {currentLocation.lng?.toFixed(6)}
                </div>
                <div className="coordinate-status">
                  <span className="status-dot"></span>
                  Live Tracking Active
                </div>
              </div>
              
              {/* Simple map representation */}
              <div className="simple-map">
                <div className="map-title">Vehicle Position Map</div>
                <div className="map-grid">
                  {[...Array(10)].map((_, i) => (
                    <div key={i} className="map-row">
                      {[...Array(10)].map((_, j) => {
                        const isVehicle = currentLocation && 
                          Math.abs((currentLocation.lat - (19.0760 - i * 0.03))) < 0.02 &&
                          Math.abs((currentLocation.lng - (72.8777 + j * 0.03))) < 0.02;
                        return (
                          <div key={j} className={`map-cell ${isVehicle ? 'vehicle' : ''}`}>
                            {isVehicle && <FaTruck />}
                          </div>
                        );
                      })}
                    </div>
                  ))}
                </div>
                <div className="map-legend">
                  <span><span className="legend-box vehicle-box"></span> Vehicle Location</span>
                  <span><span className="legend-box"></span> Search Area</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="no-location">
              <FaMapMarkerAlt />
              <p>No location data yet. Start simulation or update location.</p>
            </div>
          )}
        </div>
      </div>

      {/* Route Information for understanding */}
      {route.length > 0 && (
        <div className="route-info">
          <h3><FaRoad /> Today's Route</h3>
          <div className="route-list">
            {route.map((delivery, index) => (
              <div key={delivery.id} className={`route-item ${delivery.status === 'DELIVERED' ? 'completed' : ''}`}>
                <div className="route-number">{index + 1}</div>
                <div className="route-details">
                  <strong>{delivery.customerName}</strong>
                  <p>{delivery.deliveryAddress}</p>
                  <span className="route-status">{delivery.status}</span>
                </div>
                {currentLocation && delivery.status !== 'DELIVERED' && delivery.latitude && (
                  <div className="route-distance">
                    {calculateDistance(
                      currentLocation.lat, currentLocation.lng,
                      delivery.latitude, delivery.longitude
                    ).toFixed(1)} km away
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Tracking History  of vehicle */}
      {trackingHistory.length > 0 && (
        <div className="history-info">
          <h3><FaHistory /> Location History (Last {trackingHistory.length} updates)</h3>
          <div className="history-list">
            {trackingHistory.slice(-5).reverse().map((point, idx) => (
              <div key={idx} className="history-item">
                <span className="history-time">{point.time}</span>
                <span className="history-coords">
                  📍 {point.lat.toFixed(4)}, {point.lng.toFixed(4)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Speed Chart Placeholder with proper explaination */}
      {speed > 0 && (
        <div className="speed-info">
          <h3><FaChartLine /> Speed Monitor</h3>
          <div className="speed-bar">
            <div className="speed-fill" style={{ width: `${Math.min(speed / 100 * 100, 100)}%` }}>
              {speed.toFixed(0)} km/h
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GPSTracker;
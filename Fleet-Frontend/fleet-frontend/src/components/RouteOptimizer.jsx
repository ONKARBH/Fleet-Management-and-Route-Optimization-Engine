import React, { useState, useEffect } from 'react';
import { FaRoute, FaTruck, FaIdCard, FaBox, FaMagic } from 'react-icons/fa';
import { getVehicles, getDrivers, getDeliveries, optimizeRoute } from '../services/api';

//route optimizer   
const RouteOptimizer = () => {
  const [vehicles, setVehicles] = useState([]);
  const [drivers, setDrivers] = useState([]);
  const [deliveries, setDeliveries] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [selectedDriver, setSelectedDriver] = useState('');
  const [selectedDeliveries, setSelectedDeliveries] = useState([]);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  // Function to load vehicles, drivers, and deliveries from the API
  const loadData = async () => {
    try {
      const [vehiclesRes, driversRes, deliveriesRes] = await Promise.all([
        getVehicles(),
        getDrivers(),
        getDeliveries()
      ]);
      setVehicles(vehiclesRes.data);
      setDrivers(driversRes.data);
      setDeliveries(deliveriesRes.data.filter(d => d.status !== 'DELIVERED'));
    } catch (error) {
      console.error('Error loading data:', error);
    }
  };

  const handleDeliveryToggle = (deliveryId) => {
    setSelectedDeliveries(prev =>
      prev.includes(deliveryId)
        ? prev.filter(id => id !== deliveryId)
        : [...prev, deliveryId]
    );
  };

  const handleOptimize = async (e) => {
    e.preventDefault();
    if (!selectedVehicle || !selectedDriver || selectedDeliveries.length === 0) {
      alert('Please select vehicle, driver, and at least one delivery');
      return;
    }

    setLoading(true);
    try {
      const response = await optimizeRoute({
        vehicleId: parseInt(selectedVehicle),
        driverId: parseInt(selectedDriver),
        deliveryTaskIds: selectedDeliveries
      });
      setResult(response.data);
    } catch (error) {
      console.error('Error optimizing route:', error);
      alert('Error optimizing route. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="optimizer-container">
      <h2><FaRoute /> Route Optimization</h2>
      
      <form onSubmit={handleOptimize} className="optimizer-form">
        <div className="form-group">
          <label><FaTruck /> Select Vehicle</label>
          <select value={selectedVehicle} onChange={(e) => setSelectedVehicle(e.target.value)} required>
            <option value="">Choose a vehicle...</option>
            {vehicles.map(vehicle => (
              <option key={vehicle.id} value={vehicle.id}>
                {vehicle.licensePlate} - {vehicle.model} ({vehicle.status})
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label><FaIdCard /> Select Driver</label>
          <select value={selectedDriver} onChange={(e) => setSelectedDriver(e.target.value)} required>
            <option value="">Choose a driver...</option>
            {drivers.map(driver => (
              <option key={driver.id} value={driver.id}>
                {driver.name} - {driver.licenseNumber} ({driver.status})
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label><FaBox /> Select Deliveries</label>
          <div className="deliveries-list">
            {deliveries.length === 0 ? (
              <p>No available deliveries. Please create some deliveries first.</p>
            ) : (
              deliveries.map(delivery => (
                <label key={delivery.id} className="delivery-checkbox">
                  <input
                    type="checkbox"
                    value={delivery.id}
                    checked={selectedDeliveries.includes(delivery.id)}
                    onChange={() => handleDeliveryToggle(delivery.id)}
                  />
                  <span>
                    #{delivery.id} - {delivery.customerName} ({delivery.deliveryAddress})
                    <small>Weight: {delivery.packageWeight} kg</small>
                  </span>
                </label>
              ))
            )}
          </div>
        </div>

        <button type="submit" className="btn-primary" disabled={loading}>
          <FaMagic /> {loading ? 'Optimizing...' : 'Optimize Route'}
        </button>
      </form>

      {result && (
        <div className="optimization-result">
          <h3>Optimization Result</h3>
          <div className="result-stats">
            <div className="stat">
              <span className="stat-label">Total Distance:</span>
              <span className="stat-value">{result.totalDistance?.toFixed(2)} km</span>
            </div>
            <div className="stat">
              <span className="stat-label">Total Duration:</span>
              <span className="stat-value">{result.totalDuration?.toFixed(2)} hours</span>
            </div>
            <div className="stat">
              <span className="stat-label">Fuel Cost:</span>
              <span className="stat-value">₹{result.totalFuelCost?.toFixed(2)}</span>
            </div>
          </div>
          
          <div className="optimized-sequence">
            <h4>Optimized Delivery Sequence:</h4>
            <div className="sequence-list">
              {result.optimizedSequence?.map((delivery, index) => (
                <div key={delivery.id} className="sequence-item">
                  <div className="sequence-number">{index + 1}</div>
                  <div className="sequence-details">
                    <strong>{delivery.customerName}</strong>
                    <p>{delivery.deliveryAddress}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RouteOptimizer;
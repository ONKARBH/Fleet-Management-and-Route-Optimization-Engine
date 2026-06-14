import React, { useState, useEffect } from 'react';
import { FaPlus, FaEdit, FaTrash, FaTruck } from 'react-icons/fa';
import { getVehicles, deleteVehicle } from '../services/api';
import AddVehicleModal from './Modals/AddVehicleModal';

const Vehicles = ({ triggerRefresh }) => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadVehicles();
  }, []);

  const loadVehicles = async () => {
    try {
      const response = await getVehicles();
      setVehicles(response.data);
    } catch (error) {
      console.error('Error loading vehicles:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this vehicle?')) {
      await deleteVehicle(id);
      loadVehicles();
      triggerRefresh();
    }
  };

  const getStatusBadge = (status) => {
    const statusClass = {
      'AVAILABLE': 'badge-success',
      'ON_ROUTE': 'badge-warning',
      'MAINTENANCE': 'badge-danger',
      'OUT_OF_SERVICE': 'badge-danger'
    }[status] || 'badge-info';
    return <span className={`badge ${statusClass}`}>{status || 'UNKNOWN'}</span>;
  };

  const filteredVehicles = vehicles.filter(vehicle =>
    vehicle.licensePlate?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    vehicle.model?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) return <div className="loading">Loading vehicles...</div>;

  return (
    <div className="vehicles-container">
      <div className="page-header">
        <h2><FaTruck /> Vehicles</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          <FaPlus /> Add Vehicle
        </button>
      </div>

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by license plate or model..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="vehicles-grid">
        {filteredVehicles.map(vehicle => (
          <div key={vehicle.id} className="vehicle-card">
            <div className="vehicle-header">
              <FaTruck className="vehicle-icon" />
              <h3>{vehicle.licensePlate}</h3>
            </div>
            <div className="vehicle-details">
              <p><strong>Model:</strong> {vehicle.model || '-'}</p>
              <p><strong>Capacity:</strong> {vehicle.capacity || '-'} tons</p>
              <p><strong>Status:</strong> {getStatusBadge(vehicle.status)}</p>
              <p><strong>Odometer:</strong> {vehicle.currentOdometer || 0} km</p>
            </div>
            <div className="vehicle-actions">
              <button className="btn-danger" onClick={() => handleDelete(vehicle.id)}>
                <FaTrash /> Delete
              </button>
            </div>
          </div>
        ))}
      </div>

      {filteredVehicles.length === 0 && (
        <div className="empty-state">
          <p>No vehicles found. Click "Add Vehicle" to get started.</p>
        </div>
      )}

      <AddVehicleModal 
        show={showModal} 
        onClose={() => setShowModal(false)} 
        onSave={loadVehicles}
      />
    </div>
  );
};

export default Vehicles;
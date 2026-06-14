import React, { useState, useEffect } from 'react';
import { FaPlus, FaTrash, FaIdCard } from 'react-icons/fa';
import { getDrivers, deleteDriver } from '../services/api';
import AddDriverModal from './Modals/AddDriverModal';

const Drivers = ({ triggerRefresh }) => {
  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadDrivers();
  }, []);

  const loadDrivers = async () => {
    try {
      const response = await getDrivers();
      setDrivers(response.data);
    } catch (error) {
      console.error('Error loading drivers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this driver?')) {
      await deleteDriver(id);
      loadDrivers();
      triggerRefresh();
    }
  };

  const filteredDrivers = drivers.filter(driver =>
    driver.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    driver.licenseNumber?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) return <div className="loading">Loading drivers...</div>;

  return (
    <div className="drivers-container">
      <div className="page-header">
        <h2><FaIdCard /> Drivers</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          <FaPlus /> Add Driver
        </button>
      </div>

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by name or license number..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="drivers-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>License Number</th>
              <th>Phone</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredDrivers.map(driver => (
              <tr key={driver.id}>
                <td>{driver.id}</td>
                <td>{driver.name || '-'}</td>
                <td>{driver.licenseNumber || '-'}</td>
                <td>{driver.phoneNumber || '-'}</td>
                <td>{driver.status || 'AVAILABLE'}</td>
                <td>
                  <button className="btn-icon-danger" onClick={() => handleDelete(driver.id)}>
                    <FaTrash />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredDrivers.length === 0 && (
        <div className="empty-state">
          <p>No drivers found. Click "Add Driver" to get started.</p>
        </div>
      )}

      <AddDriverModal 
        show={showModal} 
        onClose={() => setShowModal(false)} 
        onSave={loadDrivers}
      />
    </div>
  );
};

export default Drivers;
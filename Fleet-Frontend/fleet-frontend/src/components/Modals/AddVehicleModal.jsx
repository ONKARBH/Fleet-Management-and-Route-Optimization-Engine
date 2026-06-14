

import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import { createVehicle } from '../../services/api';

const AddVehicleModal = ({ show, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    licensePlate: '',
    model: '',
    capacity: '',
    status: 'AVAILABLE'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createVehicle({
        ...formData,
        capacity: parseFloat(formData.capacity)
      });
      onSave();
      onClose();
      setFormData({ licensePlate: '', model: '', capacity: '', status: 'AVAILABLE' });
    } catch (error) {
      console.error('Error creating vehicle:', error);
      alert('Error creating vehicle');
    }
  };

  if (!show) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Add New Vehicle</h3>
          <button className="modal-close" onClick={onClose}><FaTimes /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>License Plate *</label>
            <input
              type="text"
              required
              value={formData.licensePlate}
              onChange={(e) => setFormData({...formData, licensePlate: e.target.value})}
              placeholder="MH12AB1234"
            />
          </div>
          <div className="form-group">
            <label>Model *</label>
            <input
              type="text"
              required
              value={formData.model}
              onChange={(e) => setFormData({...formData, model: e.target.value})}
              placeholder="Tata Motors Ace"
            />
          </div>
          <div className="form-group">
            <label>Capacity (tons) *</label>
            <input
              type="number"
              step="0.1"
              required
              value={formData.capacity}
              onChange={(e) => setFormData({...formData, capacity: e.target.value})}
              placeholder="1.5"
            />
          </div>
          <div className="form-group">
            <label>Status</label>
            <select
              value={formData.status}
              onChange={(e) => setFormData({...formData, status: e.target.value})}
            >
              <option value="AVAILABLE">Available</option>
              <option value="ON_ROUTE">On Route</option>
              <option value="MAINTENANCE">Maintenance</option>
            </select>
          </div>
          <div className="modal-buttons">
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary">Save Vehicle</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddVehicleModal;
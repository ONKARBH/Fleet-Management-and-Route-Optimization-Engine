import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import { createDriver } from '../../services/api';

const AddDriverModal = ({ show, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    name: '',
    licenseNumber: '',
    phoneNumber: '',
    status: 'AVAILABLE'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createDriver(formData);
      onSave();
      onClose();
      setFormData({ name: '', licenseNumber: '', phoneNumber: '', status: 'AVAILABLE' });
    } catch (error) {
      console.error('Error creating driver:', error);
      alert('Error creating driver');
    }
  };

  if (!show) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Add New Driver</h3>
          <button className="modal-close" onClick={onClose}><FaTimes /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Name *</label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              placeholder="John Doe"
            />
          </div>
          <div className="form-group">
            <label>License Number *</label>
            <input
              type="text"
              required
              value={formData.licenseNumber}
              onChange={(e) => setFormData({...formData, licenseNumber: e.target.value})}
              placeholder="DL1234567890"
            />
          </div>
          <div className="form-group">
            <label>Phone Number *</label>
            <input
              type="tel"
              required
              value={formData.phoneNumber}
              onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})}
              placeholder="9876543210"
            />
          </div>
          <div className="modal-buttons">
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary">Save Driver</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddDriverModal;
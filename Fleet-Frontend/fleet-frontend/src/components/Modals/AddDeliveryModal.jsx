import React, { useState } from 'react';
import { FaTimes } from 'react-icons/fa';
import { createDelivery } from '../../services/api';
// CSS styles for the modal
const AddDeliveryModal = ({ show, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    customerName: '',
    deliveryAddress: '',
    latitude: '',
    longitude: '',
    packageWeight: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createDelivery({
        ...formData,
        latitude: parseFloat(formData.latitude),
        longitude: parseFloat(formData.longitude),
        packageWeight: parseFloat(formData.packageWeight)
      });
      onSave();
      onClose();
      setFormData({ customerName: '', deliveryAddress: '', latitude: '', longitude: '', packageWeight: '' });
    } catch (error) {
      console.error('Error creating delivery:', error);
      alert('Error creating delivery');
    }
  };

  if (!show) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Add New Delivery</h3>
          <button className="modal-close" onClick={onClose}><FaTimes /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Customer Name *</label>
            <input
              type="text"
              required
              value={formData.customerName}
              onChange={(e) => setFormData({...formData, customerName: e.target.value})}
              placeholder="ABC Store"
            />
          </div>
          {/* Delivery Address field */}
          <div className="form-group">
            <label>Delivery Address *</label>
            <input
              type="text"
              required
              value={formData.deliveryAddress}
              onChange={(e) => setFormData({...formData, deliveryAddress: e.target.value})}
              placeholder="123 Main Street, Mumbai"
            />
          </div>
          {/* Latitude and Longitude fields with validation for numeric input */}
          <div className="form-group">
            <label>Latitude *</label>
            <input
              type="number"
              step="0.000001"
              required
              value={formData.latitude}
              onChange={(e) => setFormData({...formData, latitude: e.target.value})}
              placeholder="19.0760"
            />
          </div>
          <div className="form-group">
            <label>Longitude *</label>
            <input
              type="number"
              step="0.000001"
              required
              value={formData.longitude}
              onChange={(e) => setFormData({...formData, longitude: e.target.value})}
              placeholder="72.8777"
            />
          </div>
          <div className="form-group">
            <label>Package Weight (kg) *</label>
            <input
              type="number"
              step="0.1"
              required
              value={formData.packageWeight}
              onChange={(e) => setFormData({...formData, packageWeight: e.target.value})}
              placeholder="10.5"
            />
          </div>
          <div className="modal-buttons">
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn-primary">Save Delivery</button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddDeliveryModal;
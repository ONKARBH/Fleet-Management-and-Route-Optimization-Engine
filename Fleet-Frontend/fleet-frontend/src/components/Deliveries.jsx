import React, { useState, useEffect } from 'react';
import { FaPlus, FaTrash, FaPlay, FaCheck, FaBox } from 'react-icons/fa';
import { getDeliveries, deleteDelivery, updateDeliveryStatus } from '../services/api';
import AddDeliveryModal from './Modals/AddDeliveryModal';

const Deliveries = ({ triggerRefresh }) => {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadDeliveries();
  }, []);

  const loadDeliveries = async () => {
    try {
      const response = await getDeliveries();
      setDeliveries(response.data);
    } catch (error) {
      console.error('Error loading deliveries:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this delivery?')) {
      await deleteDelivery(id);
      loadDeliveries();
      triggerRefresh();
    }
  };

  const handleStatusUpdate = async (id, newStatus) => {
    await updateDeliveryStatus(id, newStatus, 1);
    loadDeliveries();
    triggerRefresh();
  };

  const getStatusBadge = (status) => {
    const statusClass = {
      'UNASSIGNED': 'badge-secondary',
      'DISPATCHED': 'badge-info',
      'IN_TRANSIT': 'badge-warning',
      'DELIVERED': 'badge-success',
      'FAILED': 'badge-danger'
    }[status] || 'badge-secondary';
    return <span className={`badge ${statusClass}`}>{status || 'UNASSIGNED'}</span>;
  };

  const filteredDeliveries = deliveries.filter(delivery =>
    delivery.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    delivery.deliveryAddress?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) return <div className="loading">Loading deliveries...</div>;

  return (
    <div className="deliveries-container">
      <div className="page-header">
        <h2><FaBox /> Deliveries</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          <FaPlus /> Add Delivery
        </button>
      </div>

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by customer or address..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="deliveries-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Customer</th>
              <th>Address</th>
              <th>Weight (kg)</th>
              <th>Sequence</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredDeliveries.map(delivery => (
              <tr key={delivery.id}>
                <td>{delivery.id}</td>
                <td>{delivery.customerName || '-'}</td>
                <td>{delivery.deliveryAddress || '-'}</td>
                <td>{delivery.packageWeight || '-'}</td>
                <td>{delivery.sequenceOrder || '-'}</td>
                <td>{getStatusBadge(delivery.status)}</td>
                <td>
                  <div className="action-buttons">
                    {delivery.status === 'DISPATCHED' && (
                      <button 
                        className="btn-icon-warning" 
                        onClick={() => handleStatusUpdate(delivery.id, 'IN_TRANSIT')}
                        title="Start Delivery"
                      >
                        <FaPlay />
                      </button>
                    )}
                    {delivery.status === 'IN_TRANSIT' && (
                      <button 
                        className="btn-icon-success" 
                        onClick={() => handleStatusUpdate(delivery.id, 'DELIVERED')}
                        title="Complete Delivery"
                      >
                        <FaCheck />
                      </button>
                    )}
                    <button 
                      className="btn-icon-danger" 
                      onClick={() => handleDelete(delivery.id)}
                      title="Delete"
                    >
                      <FaTrash />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredDeliveries.length === 0 && (
        <div className="empty-state">
          <p>No deliveries found. Click "Add Delivery" to get started.</p>
        </div>
      )}

      <AddDeliveryModal 
        show={showModal} 
        onClose={() => setShowModal(false)} 
        onSave={loadDeliveries}
      />
    </div>
  );
};

export default Deliveries;
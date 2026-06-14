import React, { useState, useEffect } from 'react';
import { FaPlus, FaTrash, FaPlay, FaCheck, FaBox, FaSync, FaTimesCircle, FaRedo } from 'react-icons/fa';
import { getDeliveries, deleteDelivery, updateDeliveryStatus } from '../services/api';
import AddDeliveryModal from './Modals/AddDeliveryModal';

const Deliveries = ({ triggerRefresh }) => {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [updating, setUpdating] = useState(null);

  useEffect(() => {
    loadDeliveries();
  }, []);

  const loadDeliveries = async () => {
    try {
      setLoading(true);
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
      await loadDeliveries();
      if (triggerRefresh) triggerRefresh();
    }
  };

  const handleStatusUpdate = async (id, newStatus, vehicleId = 1) => {
    setUpdating(id);
    try {
      await updateDeliveryStatus(id, newStatus, vehicleId);
      await loadDeliveries();
      if (triggerRefresh) triggerRefresh();
      
      // Show success message
      showTemporaryMessage(`Delivery ${id} status updated to ${newStatus}`, 'success');
    } catch (error) {
      console.error('Error updating status:', error);
      showTemporaryMessage(`Failed to update delivery ${id}`, 'error');
    } finally {
      setUpdating(null);
    }
  };

  const showTemporaryMessage = (message, type) => {
    const msgDiv = document.createElement('div');
    msgDiv.className = `toast-message ${type}`;
    msgDiv.textContent = message;
    document.body.appendChild(msgDiv);
    setTimeout(() => msgDiv.remove(), 3000);
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'UNASSIGNED': { class: 'badge-secondary', icon: <FaBox />, text: 'Unassigned' },
      'DISPATCHED': { class: 'badge-info', icon: <FaSync />, text: 'Dispatched' },
      'IN_TRANSIT': { class: 'badge-warning', icon: <FaPlay />, text: 'In Transit' },
      'DELIVERED': { class: 'badge-success', icon: <FaCheck />, text: 'Delivered' },
      'FAILED': { class: 'badge-danger', icon: <FaTimesCircle />, text: 'Failed' },
      'CANCELLED': { class: 'badge-danger', icon: <FaTimesCircle />, text: 'Cancelled' }
    };
    const config = statusConfig[status] || statusConfig['UNASSIGNED'];
    return (
      <span className={`badge ${config.class}`}>
        {config.icon} {config.text}
      </span>
    );
  };

  const getAvailableActions = (status, id) => {
    const actions = [];
    
    switch(status) {
      case 'UNASSIGNED':
        actions.push(
          <button 
            key="dispatch"
            className="btn-icon-info" 
            onClick={() => handleStatusUpdate(id, 'DISPATCHED')}
            title="Dispatch Delivery"
            disabled={updating === id}
          >
            <FaSync /> Dispatch
          </button>
        );
        break;
      case 'DISPATCHED':
        actions.push(
          <button 
            key="start"
            className="btn-icon-warning" 
            onClick={() => handleStatusUpdate(id, 'IN_TRANSIT')}
            title="Start Delivery"
            disabled={updating === id}
          >
            <FaPlay /> Start
          </button>
        );
        break;
      case 'IN_TRANSIT':
        actions.push(
          <button 
            key="complete"
            className="btn-icon-success" 
            onClick={() => handleStatusUpdate(id, 'DELIVERED')}
            title="Complete Delivery"
            disabled={updating === id}
          >
            <FaCheck /> Complete
          </button>,
          <button 
            key="fail"
            className="btn-icon-danger" 
            onClick={() => handleStatusUpdate(id, 'FAILED')}
            title="Mark as Failed"
            disabled={updating === id}
          >
            <FaTimesCircle /> Fail
          </button>
        );
        break;
      case 'FAILED':
        actions.push(
          <button 
            key="retry"
            className="btn-icon-warning" 
            onClick={() => handleStatusUpdate(id, 'DISPATCHED')}
            title="Retry Delivery"
            disabled={updating === id}
          >
            <FaRedo /> Retry
          </button>
        );
        break;
      default:
        break;
    }
    
    return actions;
  };

  const filteredDeliveries = deliveries.filter(delivery =>
    delivery.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    delivery.deliveryAddress?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    delivery.id?.toString().includes(searchTerm)
  );

  const stats = {
    total: deliveries.length,
    dispatched: deliveries.filter(d => d.status === 'DISPATCHED').length,
    inTransit: deliveries.filter(d => d.status === 'IN_TRANSIT').length,
    delivered: deliveries.filter(d => d.status === 'DELIVERED').length,
    failed: deliveries.filter(d => d.status === 'FAILED').length
  };

  if (loading) return <div className="loading">Loading deliveries...</div>;

  return (
    <div className="deliveries-container">
      <div className="page-header">
        <h2><FaBox /> Deliveries</h2>
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          <FaPlus /> Add Delivery
        </button>
      </div>

      {/* Statistics Cards */}
      <div className="delivery-stats">
        <div className="stat-card-mini">
          <span className="stat-value">{stats.total}</span>
          <span className="stat-label">Total</span>
        </div>
        <div className="stat-card-mini">
          <span className="stat-value">{stats.dispatched}</span>
          <span className="stat-label">Dispatched</span>
        </div>
        <div className="stat-card-mini">
          <span className="stat-value">{stats.inTransit}</span>
          <span className="stat-label">In Transit</span>
        </div>
        <div className="stat-card-mini">
          <span className="stat-value">{stats.delivered}</span>
          <span className="stat-label">Delivered</span>
        </div>
        <div className="stat-card-mini">
          <span className="stat-value">{stats.failed}</span>
          <span className="stat-label">Failed</span>
        </div>
      </div>

      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by ID, customer or address..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        <button className="btn-refresh" onClick={loadDeliveries}>
          <FaSync /> Refresh
        </button>
      </div>

      <div className="deliveries-table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Customer</th>
              <th>Address</th>
              <th>Weight</th>
              <th>Sequence</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredDeliveries.map(delivery => (
              <tr key={delivery.id} className={`delivery-row status-${delivery.status?.toLowerCase()}`}>
                <td>{delivery.id}</td>
                <td><strong>{delivery.customerName || '-'}</strong></td>
                <td>{delivery.deliveryAddress || '-'}</td>
                <td>{delivery.packageWeight || 0} kg</td>
                <td>{delivery.sequenceOrder || '-'}</td>
                <td>{getStatusBadge(delivery.status)}</td>
                <td className="actions-cell">
                  <div className="action-buttons-group">
                    {getAvailableActions(delivery.status, delivery.id)}
                    <button 
                      className="btn-icon-danger" 
                      onClick={() => handleDelete(delivery.id)}
                      title="Delete Delivery"
                    >
                      <FaTrash />
                    </button>
                  </div>
                  {updating === delivery.id && <span className="updating-spinner">Updating...</span>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {filteredDeliveries.length === 0 && (
        <div className="empty-state">
          <FaBox />
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
import React, { useState, useEffect } from 'react';
import { 
  FaTruck, FaIdCard, FaBox, FaCheckCircle, FaHourglassHalf, 
  FaPlay, FaSync, FaTimesCircle, FaChartLine 
} from 'react-icons/fa';
import { getVehicles, getDrivers, getDeliveries } from '../services/api';

const Dashboard = ({ refresh }) => {
  const [stats, setStats] = useState({
    vehicles: 0,
    drivers: 0,
    deliveries: 0,
    completed: 0,
    inTransit: 0,
    dispatched: 0,
    failed: 0
  });
  const [recentDeliveries, setRecentDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [lastUpdate, setLastUpdate] = useState(new Date());

  useEffect(() => {
    loadDashboardData();
    // Auto refresh every 10 seconds
    const interval = setInterval(loadDashboardData, 10000);
    return () => clearInterval(interval);
  }, [refresh]);

  const loadDashboardData = async () => {
    try {
      const [vehiclesRes, driversRes, deliveriesRes] = await Promise.all([
        getVehicles(),
        getDrivers(),
        getDeliveries()
      ]);

      const vehicles = vehiclesRes.data;
      const drivers = driversRes.data;
      const deliveries = deliveriesRes.data;

      const completed = deliveries.filter(d => d.status === 'DELIVERED').length;
      const inTransit = deliveries.filter(d => d.status === 'IN_TRANSIT').length;
      const dispatched = deliveries.filter(d => d.status === 'DISPATCHED').length;
      const failed = deliveries.filter(d => d.status === 'FAILED').length;

      setStats({
        vehicles: vehicles.length,
        drivers: drivers.length,
        deliveries: deliveries.length,
        completed,
        inTransit,
        dispatched,
        failed
      });

      // Get recent deliveries (last 5 updated)
      setRecentDeliveries(deliveries.slice(-5).reverse());
      setLastUpdate(new Date());
    } catch (error) {
      console.error('Error loading dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ icon, label, value, color, bgColor }) => (
    <div className="stat-card" style={{ borderLeftColor: color, background: bgColor }}>
      <div className="stat-card-icon" style={{ color }}>{icon}</div>
      <div className="stat-card-info">
        <h3>{value}</h3>
        <p>{label}</p>
      </div>
    </div>
  );

  const getStatusIcon = (status) => {
    switch(status) {
      case 'DELIVERED': return <FaCheckCircle style={{ color: '#48bb78' }} />;
      case 'IN_TRANSIT': return <FaPlay style={{ color: '#4299e1' }} />;
      case 'DISPATCHED': return <FaHourglassHalf style={{ color: '#ed8936' }} />;
      case 'FAILED': return <FaTimesCircle style={{ color: '#f56565' }} />;
      default: return <FaBox style={{ color: '#a0aec0' }} />;
    }
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2><FaChartLine /> Dashboard</h2>
        <div className="refresh-info">
          <span>Last updated: {lastUpdate.toLocaleTimeString()}</span>
          <button className="btn-refresh-small" onClick={loadDashboardData}>
            <FaSync /> Refresh
          </button>
        </div>
      </div>
      
      <div className="stats-grid">
        <StatCard icon={<FaTruck />} label="Total Vehicles" value={stats.vehicles} color="#4299e1" bgColor="#ebf8ff" />
        <StatCard icon={<FaIdCard />} label="Total Drivers" value={stats.drivers} color="#48bb78" bgColor="#f0fff4" />
        <StatCard icon={<FaBox />} label="Total Deliveries" value={stats.deliveries} color="#ed8936" bgColor="#fffaf0" />
        <StatCard icon={<FaCheckCircle />} label="Completed" value={stats.completed} color="#38a169" bgColor="#f0fff4" />
        <StatCard icon={<FaPlay />} label="In Transit" value={stats.inTransit} color="#3182ce" bgColor="#ebf8ff" />
        <StatCard icon={<FaHourglassHalf />} label="Dispatched" value={stats.dispatched} color="#dd6b20" bgColor="#fffaf0" />
        <StatCard icon={<FaTimesCircle />} label="Failed" value={stats.failed} color="#e53e3e" bgColor="#fff5f5" />
      </div>

      <div className="recent-activity-card">
        <h3>Recent Activity</h3>
        <div className="recent-activity-list">
          {recentDeliveries.length === 0 ? (
            <div className="empty-activity">
              <p>No recent deliveries. Create some deliveries to see activity here.</p>
            </div>
          ) : (
            recentDeliveries.map(delivery => (
              <div key={delivery.id} className="activity-item">
                <div className="activity-icon">
                  {getStatusIcon(delivery.status)}
                </div>
                <div className="activity-details">
                  <p className="activity-title">
                    Delivery #{delivery.id} - {delivery.customerName}
                  </p>
                  <p className="activity-status">
                    Status: <strong>{delivery.status}</strong>
                    {delivery.sequenceOrder && ` | Sequence: ${delivery.sequenceOrder}`}
                    {delivery.packageWeight && ` | Weight: ${delivery.packageWeight} kg`}
                  </p>
                  <p className="activity-address">{delivery.deliveryAddress}</p>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
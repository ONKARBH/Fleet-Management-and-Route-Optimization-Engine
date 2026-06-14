import React, { useState, useEffect } from 'react';
import { 
  FaTruck, 
  FaIdCard, 
  FaBox, 
  FaCheckCircle,
  FaHourglassHalf,
  FaPlay,
  FaExclamationTriangle 
} from 'react-icons/fa';
import { 
  PieChart, 
  Pie, 
  Cell, 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend,
  ResponsiveContainer 
} from 'recharts';
import { getVehicles, getDrivers, getDeliveries } from '../services/api';

const Dashboard = ({ refresh }) => {
  const [stats, setStats] = useState({
    vehicles: 0,
    drivers: 0,
    deliveries: 0,
    completed: 0,
    inTransit: 0,
    dispatched: 0
  });
  const [deliveryData, setDeliveryData] = useState([]);
  const [recentDeliveries, setRecentDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
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

      setStats({
        vehicles: vehicles.length,
        drivers: drivers.length,
        deliveries: deliveries.length,
        completed,
        inTransit,
        dispatched
      });

      // Prepare chart data
      const statusData = [
        { name: 'Delivered', value: completed, color: '#48bb78' },
        { name: 'In Transit', value: inTransit, color: '#4299e1' },
        { name: 'Dispatched', value: dispatched, color: '#ed8936' },
        { name: 'Pending', value: deliveries.length - (completed + inTransit + dispatched), color: '#a0aec0' }
      ];
      setDeliveryData(statusData);

      // Recent deliveries
      setRecentDeliveries(deliveries.slice(-5).reverse());

    } catch (error) {
      console.error('Error loading dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ icon, label, value, color }) => (
    <div className="stat-card" style={{ borderLeftColor: color }}>
      <div className="stat-card-icon" style={{ color }}>{icon}</div>
      <div className="stat-card-info">
        <h3>{value}</h3>
        <p>{label}</p>
      </div>
    </div>
  );

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div className="dashboard">
      <h2 className="page-title">Dashboard</h2>
      
      <div className="stats-grid">
        <StatCard icon={<FaTruck />} label="Total Vehicles" value={stats.vehicles} color="#4299e1" />
        <StatCard icon={<FaIdCard />} label="Total Drivers" value={stats.drivers} color="#48bb78" />
        <StatCard icon={<FaBox />} label="Total Deliveries" value={stats.deliveries} color="#ed8936" />
        <StatCard icon={<FaCheckCircle />} label="Completed" value={stats.completed} color="#38a169" />
        <StatCard icon={<FaPlay />} label="In Transit" value={stats.inTransit} color="#3182ce" />
        <StatCard icon={<FaHourglassHalf />} label="Dispatched" value={stats.dispatched} color="#dd6b20" />
      </div>

      <div className="charts-grid">
        <div className="chart-card">
          <h3>Delivery Status Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={deliveryData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {deliveryData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="chart-card">
          <h3>Recent Activity</h3>
          <div className="recent-activity">
            {recentDeliveries.length === 0 ? (
              <p>No recent deliveries</p>
            ) : (
              recentDeliveries.map(delivery => (
                <div key={delivery.id} className="activity-item">
                  <div className="activity-icon">
                    {delivery.status === 'DELIVERED' ? <FaCheckCircle style={{ color: '#48bb78' }} /> :
                     delivery.status === 'IN_TRANSIT' ? <FaPlay style={{ color: '#4299e1' }} /> :
                     <FaHourglassHalf style={{ color: '#ed8936' }} />}
                  </div>
                  <div className="activity-details">
                    <p className="activity-title">Delivery #{delivery.id} - {delivery.customerName}</p>
                    <p className="activity-status">Status: {delivery.status}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
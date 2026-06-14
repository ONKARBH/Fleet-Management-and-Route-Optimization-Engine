import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Dashboard from './components/Dashboard';
import Vehicles from './components/Vehicles';
import Drivers from './components/Drivers';
import Deliveries from './components/Deliveries';
import RouteOptimizer from './components/RouteOptimizer';
import GPSTracker from './components/GPSTracker';
import './styles/App.css';

function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [refresh, setRefresh] = useState(false);

  const triggerRefresh = () => setRefresh(!refresh);

  return (
    <div className="app">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />
      <main className="main-content">
        {activeTab === 'dashboard' && <Dashboard refresh={refresh} />}
        {activeTab === 'vehicles' && <Vehicles triggerRefresh={triggerRefresh} />}
        {activeTab === 'drivers' && <Drivers triggerRefresh={triggerRefresh} />}
        {activeTab === 'deliveries' && <Deliveries triggerRefresh={triggerRefresh} />}
        {activeTab === 'optimize' && <RouteOptimizer />}
        {activeTab === 'tracking' && <GPSTracker />}
      </main>
    </div>
  );
}

export default App;
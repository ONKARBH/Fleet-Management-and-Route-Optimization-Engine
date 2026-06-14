import React from 'react';
import { 
  FaTachometerAlt, 
  FaTruck, 
  FaIdCard, 
  FaBox, 
  FaRoute, 
  FaMapMarkerAlt 
} from 'react-icons/fa';

const Navbar = ({ activeTab, setActiveTab }) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: <FaTachometerAlt /> },
    { id: 'vehicles', label: 'Vehicles', icon: <FaTruck /> },
    { id: 'drivers', label: 'Drivers', icon: <FaIdCard /> },
    { id: 'deliveries', label: 'Deliveries', icon: <FaBox /> },
    { id: 'optimize', label: 'Optimize Route', icon: <FaRoute /> },
    { id: 'tracking', label: 'GPS Tracking', icon: <FaMapMarkerAlt /> },
  ];

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <FaTruck className="brand-icon" />
        <h1>Fleet Manager</h1>
      </div>
      <div className="navbar-menu">
        {menuItems.map((item) => (
          <button
            key={item.id}
            className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
            onClick={() => setActiveTab(item.id)}
          >
            {item.icon}
            <span>{item.label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
};

export default Navbar;
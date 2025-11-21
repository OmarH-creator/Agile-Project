import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from '../pages/Login_View/Login';
import AdminDashboard from '../pages/Admin_View/AdminDashboard';
import FacilitiesUI from '../pages/Facilities/FacilitiesUI';
import Curriculum from '../pages/Curriculum/Curriculum';

function AppRoutes() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<AdminDashboard />} />
        <Route path="/login" element={<Login />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/facilities" element={<FacilitiesUI />} />
        <Route path="/admin/curriculum" element={<Curriculum />} />
      </Routes>
    </Router>
  );
}

export default AppRoutes;

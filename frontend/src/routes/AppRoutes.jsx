import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from '../pages/Login_View/Login';
import AdminDashboard from '../pages/Admin_View/AdminDashboard';
import FacilitiesUI from '../pages/Facilities/FacilitiesUI';
import Curriculum from '../pages/Curriculum/Curriculum';
import EditCourses from '../pages/EditCourses/EditCourses';

function AppRoutes() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="/admin/facilities" element={<FacilitiesUI />} />
        <Route path="/admin/curriculum" element={<Curriculum />} />
          <Route path="/admin/curriculum/edit-courses" element={<EditCourses />} />
        <Route path="/student" />
      </Routes>
    </Router>
  );
}

export default AppRoutes;

import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from '../pages/Login_View/Login';
import AdminDashboard from '../pages/Admin_View/AdminDashboard';

function AppRoutes() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/Admin" element={<AdminDashboard/>}/>
      </Routes>
    </Router>
  );
}

export default AppRoutes;

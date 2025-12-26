import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from '../pages/Login_View/Login';
import AdminDashboard from '../pages/Admin_View/AdminDashboard';
import FacilitiesUI from '../pages/Facilities/FacilitiesUI';
import Curriculum from '../pages/Curriculum/Curriculum';
import EditCourses from '../pages/EditCourses/EditCourses';
import StudentServices from "../pages/Admin_View/StudentServices";
import ProfessorRecords from "../pages/Admin_View/ProfessorRecords";
import ProfessorManagement from "../pages/Admin_View/ProfessorManagement";
import StudentDashboard from "../pages/Student_View/StudentDashboard";
import Course_Reg from "../pages/Student_View/Course_Reg/Course_Reg"; // Import the new file
import StudentProfile from "../pages/Student_View/StudentProfile/StudentProfile";
import Announcements from "../pages/Admin_View/Announcements/Announcements";
import StudentAnnouncements from "../pages/Student_View/StudentAnnouncements/StudentAnnouncements";
import StudentCourses from "../pages/Student_View/StudentCourses/StudentCourses";

function AppRoutes() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        {/*<Route path="/login" element={<Login />} />*/}
        <Route path="/Admin" element={<AdminDashboard />} />
        <Route path="/Admin/Students" element={<StudentServices/>}/>
        <Route path="/Admin/Professors" element={<ProfessorManagement/>}/>
        <Route path="/Admin/Facilities" element={<FacilitiesUI />} />
        <Route path="/Admin/Curriculum" element={<Curriculum />} />
        <Route path="/Admin/Curriculum/Edit-courses" element={<EditCourses />} />
        <Route path="/Admin/Announcements" element={<Announcements />} />
        <Route path="/Student" element={<StudentDashboard/>}/>
        <Route path="/student/courses" element={<StudentCourses />} />
        <Route path="/Student/registration" element={<Course_Reg/>}/>
        <Route path="/student/profile" element={<StudentProfile />} />
        <Route path="/student/announcements" element={<StudentAnnouncements />} />
      </Routes>
    </Router>
  );
}

export default AppRoutes;

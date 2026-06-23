import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./routes/ProtectedRoute";
import LoginForm from "./pages/LoginForm";
import UserPage from "./pages/UserPage";
import BinDetailPage from "./pages/BinDetailPage";
import BinEditForm from "./pages/BinEditForm";
import BinReportForm from "./pages/BinReportForm";

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginForm />} />
          <Route path="/" element={<LoginForm />} />
          
          <Route
            path="/user/:domainId"
            element={
              <ProtectedRoute>
                <UserPage />
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/bin/:binId"
            element={
              <ProtectedRoute>
                <BinDetailPage />
              </ProtectedRoute>
            }
          />
          
          <Route path="/bin/edit/:binId" element={<BinEditForm />} />
          <Route path="/bin/:binId/report" element={<BinReportForm/>} />
        
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
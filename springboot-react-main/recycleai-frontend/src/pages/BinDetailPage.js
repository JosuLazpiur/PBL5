import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import axios from "axios"; 

import BinService from "../services/BinService";
import { getLogsByBin } from "../services/LogService";

import HeaderComponent from "../components/HeaderComponent";
import { FooterComponent } from "../components/FooterComponent";
import BinMonitor from "../components/BinMonitor";

import { 
  Box, 
  Container, 
  CircularProgress, 
  Alert,
  CssBaseline,
  ThemeProvider,
  createTheme
} from "@mui/material";

const theme = createTheme({
  palette: {
    primary: { main: "#2E7D32" },
    secondary: { main: "#1565C0" },
    background: { default: "#F1F8E9" }
  },
  typography: {
    fontFamily: '"Poppins", "Roboto", "Helvetica", "Arial", sans-serif',
  },
});

const BinDetailPage = () => {
  const { binId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [bin, setBin] = useState(null);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const API_URL = "http://localhost:8080/api";

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);

        const [binData, logsData] = await Promise.all([
           BinService.getBinById(binId),
           getLogsByBin(binId)
        ]);

        let currentImageUrl = "";
        try {
            const imageRes = await axios.get(`${API_URL}/images/latest/${binId}`);
            if (imageRes.data?.path) {
                currentImageUrl = imageRes.data.path;
            }
        } catch (error_) {
            currentImageUrl = "";
            console.warn("Using default image placeholder because no previous image was found:", error_.message);
        }

        setBin({ ...binData.data, imageUrl: currentImageUrl });
        setLogs(logsData);
        setLoading(false);

      } catch (err) {
        console.error("Error loading bin data:", err);
        setError("Could not load bin details. Please try again.");
        setLoading(false);
        
      }
    }

    if (binId) loadData();
  }, [binId, navigate, user]);

  const renderContent = () => {
    if (loading) {
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
          <CircularProgress color="primary" size={60} />
        </Box>
      );
    }
    if (error) {
      return <Alert severity="error" sx={{ mt: 4 }}>{error}</Alert>;
    }
    return (
      <BinMonitor 
        bin={bin} 
        image={bin?.imageUrl || ""} 
        logs={logs} 
      />
    );
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'background.default' }}>
        
        <HeaderComponent />
        
        <Box component="main" sx={{ flexGrow: 1, py: 4 }}>
          <Container maxWidth={false} sx={{ px: { xs: 2, md: 4, lg: 6 } }}>
            {renderContent()}
          </Container>
        </Box>

        <FooterComponent />
      </Box>
    </ThemeProvider>
  );
};

export default BinDetailPage;
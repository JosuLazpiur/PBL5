import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import BinService from "../services/BinService";
import HeaderComponent from "../components/HeaderComponent";
import { FooterComponent } from "../components/FooterComponent";
import { useAuth } from "../context/AuthContext";

// Material UI
import {
  Box,
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  CircularProgress,
  InputAdornment,
  CssBaseline,
  createTheme,
  ThemeProvider,
  Stack
} from "@mui/material";

// Icons
import {
  Save as SaveIcon,
  ArrowBack as ArrowBackIcon,
  LocationOn as LocationIcon,
  Domain as DomainIcon,
  QrCode as IdIcon
} from "@mui/icons-material";

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

const BinEditForm = () => {
  const { binId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [bin, setBin] = useState(null);
  const [ubication, setUbication] = useState("");
  const [loading, setLoading] = useState(true); 
  const [saving, setSaving] = useState(false); 

  useEffect(() => {
    BinService.getBinById(binId)
      .then((res) => {
        setBin(res.data);
        setUbication(res.data.ubication);
        setLoading(false);
      })
      .catch((err) => {
        console.error(err);
        if (user?.domain) {
          navigate(`/user/${user.domain.domainId}`);
        } else {
          navigate("/login");
        }
      });
  }, [binId, navigate, user]);

  const handleSubmit = (e) => {
    e.preventDefault();
    setSaving(true);
    
    const updatedBin = { ...bin, ubication };

    BinService.updateBin(binId, updatedBin)
      .then(() => {
        if (user?.domain) {
          navigate(`/user/${user.domain.domainId}`);
        } else {
          navigate("/login");
        }
      })
      .catch((err) => {
        console.error(err);
        setSaving(false);
      });
  };

  const handleCancel = () => {
    if (user?.domain) {
      navigate(`/user/${user.domain.domainId}`);
    } else {
      navigate("/");
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'background.default' }}>
        
        <HeaderComponent />

        <Box component="main" sx={{ flexGrow: 1, py: 4, px: 2, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <Container maxWidth="sm"> 
            
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                <CircularProgress />
              </Box>
            ) : (
              <Paper 
                elevation={3} 
                sx={{ 
                  p: 4, 
                  borderRadius: 3,
                  borderTop: '6px solid #2E7D32' 
                }}
              >
                <Box sx={{ mb: 4, textAlign: 'center' }}>
                  <Typography variant="h4" component="h1" fontWeight="bold" color="primary">
                    Configure Bin
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Update location details below
                  </Typography>
                </Box>

                <form onSubmit={handleSubmit}>
                  <Stack spacing={3}>
                    <TextField
                      fullWidth
                      label="Bin ID"
                      value={bin.binId}
                      InputProps={{
                        readOnly: true,
                        startAdornment: (
                          <InputAdornment position="start">
                            <IdIcon color="action" />
                          </InputAdornment>
                        ),
                      }}
                      variant="filled" 
                      sx={{ bgcolor: 'rgba(0, 0, 0, 0.04)' }}
                    />

                    <TextField
                      fullWidth
                      label="Domain"
                      value={bin.domain?.name || "Unknown"}
                      InputProps={{
                        readOnly: true,
                        startAdornment: (
                          <InputAdornment position="start">
                            <DomainIcon color="action" />
                          </InputAdornment>
                        ),
                      }}
                      variant="filled"
                      sx={{ bgcolor: 'rgba(0, 0, 0, 0.04)' }}
                    />

                    <TextField
                      data-testid="location-input"
                      fullWidth
                      label="Location"
                      required
                      value={ubication}
                      onChange={(e) => setUbication(e.target.value)}
                      placeholder="E.g. Main Entrance, Floor 2..."
                      InputProps={{
                        startAdornment: (
                          <InputAdornment position="start">
                            <LocationIcon color="primary" />
                          </InputAdornment>
                        ),
                      }}
                      sx={{ 
                        '& .MuiOutlinedInput-root': {
                          backgroundColor: 'white'
                        }
                      }}
                    />

                    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 2 }}>
                      <Button
                        variant="outlined"
                        color="inherit"
                        size="large"
                        startIcon={<ArrowBackIcon />}
                        onClick={handleCancel}
                        disabled={saving}
                        sx={{ minWidth: '140px' }}
                      >
                        Cancel
                      </Button>
                      
                      <Button
                        data-testid="save-btn"
                        type="submit"
                        variant="contained"
                        color="primary"
                        size="large"
                        startIcon={saving ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                        disabled={saving}
                        sx={{ fontWeight: 'bold', minWidth: '140px' }}
                      >
                        {saving ? "Saving..." : "Save"}
                      </Button>
                    </Box>

                  </Stack>
                </form>
              </Paper>
            )}
          </Container>
        </Box>

        <FooterComponent />
        
      </Box>
    </ThemeProvider>
  );
};

export default BinEditForm;
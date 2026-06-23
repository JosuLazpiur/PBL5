import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import HeaderComponent from "../components/HeaderComponent";
import { FooterComponent } from "../components/FooterComponent";

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
  Stack,
  Alert
} from "@mui/material";

import {
  Send as SendIcon,
  ArrowBack as ArrowBackIcon,
  ReportProblem as ReportIcon,
  Title as TitleIcon,
  Description as DescriptionIcon
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

const BinReportForm = () => {
  const { binId } = useParams();
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccessMsg("");

    if (!title.trim() || !description.trim()) {
      setError("Please fill in both title and description.");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        title: title,
        description: description,
        bin: { 
            binId: Number.parseInt(binId, 10) 
        }
      };

      const response = await fetch("http://localhost:1880/new-alert", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        throw new Error("Server responded with an error");
      }

      setSuccessMsg("Report submitted successfully!");
      setTitle("");
      setDescription("");

      setTimeout(() => {
        navigate(`/bin/${binId}`);
      }, 1500);
      
    } catch (err) {
      console.error(err);
      setError("Failed to submit report. Please check connection.");
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate(`/bin/${binId}`);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: 'background.default' }}>
        
        <HeaderComponent />

        <Box component="main" sx={{ flexGrow: 1, py: 4, px: 2, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          
          <Container maxWidth="sm"> 
            
            <Paper 
              elevation={3} 
              sx={{ 
                p: 4, 
                borderRadius: 3,
                borderTop: '6px solid #d32f2f'
              }}
            >
              <Box sx={{ mb: 4, textAlign: 'center' }}>
                <Typography variant="h4" component="h1" fontWeight="bold" color="#d32f2f" sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
                  <ReportIcon fontSize="large" /> Report Issue
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  Reporting problem for Bin #{binId}
                </Typography>
              </Box>

              <form onSubmit={handleSubmit}>
                <Stack spacing={3}>

                  <TextField
                    fullWidth
                    label="Issue Title"
                    placeholder="e.g. Lid Broken, Overflowing..."
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <TitleIcon color="action" />
                        </InputAdornment>
                      ),
                    }}
                    variant="outlined"
                    sx={{ 
                      '& .MuiOutlinedInput-root': { backgroundColor: 'white' }
                    }}
                  />

                  <TextField
                    fullWidth
                    label="Description"
                    placeholder="Describe the issue in detail..."
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    required
                    multiline
                    rows={4}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start" sx={{ alignSelf: 'flex-start', mt: 0 }}>
                          <DescriptionIcon color="action" />
                        </InputAdornment>
                      ),
                    }}
                    variant="outlined"
                    sx={{ 
                      '& .MuiOutlinedInput-root': { backgroundColor: 'white' }
                    }}
                  />

                  {error && <Alert severity="error">{error}</Alert>}
                  {successMsg && <Alert severity="success">{successMsg}</Alert>}

                  <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 2 }}>
                    <Button
                      variant="outlined"
                      color="inherit"
                      size="large"
                      startIcon={<ArrowBackIcon />}
                      onClick={handleCancel}
                      disabled={loading}
                      sx={{ minWidth: '140px' }}
                    >
                      Cancel
                    </Button>
                    
                    <Button
                      type="submit"
                      variant="contained"
                      color="error"
                      size="large"
                      startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SendIcon />}
                      disabled={loading}
                      sx={{ fontWeight: 'bold', minWidth: '140px' }}
                    >
                      {loading ? "Sending..." : "Submit"}
                    </Button>
                  </Box>

                </Stack>
              </form>
            </Paper>
          </Container>
        </Box>

        <FooterComponent />
        
      </Box>
    </ThemeProvider>
  );
};

export default BinReportForm;

import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import LoginService from "../services/LoginService";
import { useAuth } from "../context/AuthContext";

import {
  Button,
  TextField,
  Paper,
  Box,
  Grid,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  CssBaseline,
  GlobalStyles,
  createTheme,
  ThemeProvider
} from "@mui/material";

import {
  Visibility,
  VisibilityOff,
  Login as LoginIcon
} from "@mui/icons-material";

const theme = createTheme({
  palette: {
    primary: { main: "#2E7D32" },
    secondary: { main: "#1565C0" },
  },
  typography: {
    fontFamily: '"Poppins", "Roboto", "Helvetica", "Arial", sans-serif',
  },
});

function LoginForm() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleClickShowPassword = () => setShowPassword((show) => !show);

  const handleSubmit = (event) => {
    event.preventDefault();
    setError("");
    setIsLoading(true);

    LoginService.login(username, password)
      .then((res) => {
        login(res.data);
        navigate(`/user/${res.data.domain.domainId}`);
      })
      .catch((err) => {
        console.error(err);
        setError("Invalid username or password.");
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const formBackgroundColor = "#F1F8E9";

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <GlobalStyles styles={{
        'html, body, #root': { 
            margin: 0, 
            padding: 0, 
            width: '100%', 
            height: '100%', 
            overflow: 'hidden' 
        }
      }} />

      <Grid 
        container 
        component="main" 
        sx={{ 
          height: "100vh",
          width: "100vw",       
          position: "fixed",
          top: 0,
          left: 0,
          right: 0,             
          bottom: 0,
          margin: 0,
          padding: 0,
          overflow: "hidden",
          flexWrap: "nowrap", 
          zIndex: 9999
        }}
      >
        
        {/* --- LADO IZQUIERDA: IMAGEN (66% del ancho - Mucho más grande) --- */}
        <Grid
          item
          xs={8} 
          sx={{
            backgroundImage: "url(https://images.unsplash.com/photo-1518173946687-a4c8892bbd9f?q=80&w=2574&auto=format&fit=crop)",
            backgroundRepeat: "no-repeat",
            backgroundColor: (t) => t.palette.grey[50],
            backgroundSize: "cover",
            backgroundPosition: "center",
            position: "relative",
            display: "flex",
            flexDirection: "column",
            justifyContent: "center",
            alignItems: "center",
            textAlign: "center",
            p: 4,
            minWidth: 0 
          }}
        >
          {/* Overlay oscuro */}
          <Box
            sx={{
              position: "absolute",
              top: 0,
              left: 0,
              width: "100%",
              height: "100%",
              background: "rgba(0,0,0,0.5)",
              zIndex: 1
            }}
          />
          
          <Box sx={{ position: "relative", zIndex: 2, color: "white" }}>
            <Typography variant="h1" fontWeight="700" sx={{ fontSize: { xs: '2.5rem', md: '4rem' }, mb: 2 }}>
              RecyclAI
            </Typography>
            <Typography variant="h5" fontWeight="300" sx={{ opacity: 0.9 }}>
              Smart waste management for a sustainable future
            </Typography>
          </Box>
        </Grid>

        <Grid 
          item 
          xs={4} 
          component={Paper} 
          elevation={0}
          square
          sx={{
            display: "flex",
            flexDirection: "column",
            height: "100%",
            width: "100%",
            position: "relative",
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: formBackgroundColor,
            borderLeft: "1px solid rgba(0,0,0,0.05)"
          }}
        >
          
          {/* CAJA DEL FORMULARIO */}
          <Box
            sx={{
              width: "100%", 
              maxWidth: "380px", 
              p: 3, 
              display: "flex",
              flexDirection: "column",
              alignItems: "center"
            }}
          >
            
            <Box sx={{ textAlign: "center", mb: 5 }}> 
              <Typography 
                component="h1" 
                variant="h3"
                sx={{ 
                  fontWeight: '800', 
                  color: 'primary.main',
                  fontSize: { xs: '2rem', md: '2.5rem' } 
                }}
              >
                Welcome
              </Typography>
              <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>
                Please sign in to continue
              </Typography>
            </Box>

            {error && (
              <Alert severity="error" sx={{ width: "100%", mb: 3 }}>
                {error}
              </Alert>
            )}

            <Box component="form" noValidate onSubmit={handleSubmit} sx={{ width: "100%" }}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="username"
                label="Username"
                name="username"
                autoComplete="username"
                autoFocus
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                disabled={isLoading}
                sx={{ 
                  '& .MuiOutlinedInput-root': { backgroundColor: formBackgroundColor } 
                }} 
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type={showPassword ? "text" : "password"}
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isLoading}
                sx={{ 
                  '& .MuiOutlinedInput-root': { backgroundColor: formBackgroundColor } 
                }}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        onClick={handleClickShowPassword}
                        edge="end"
                      >
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                }}
              />
              
              <Button
                type="submit"
                fullWidth
                variant="contained"
                size="large"
                disabled={isLoading}
                endIcon={!isLoading && <LoginIcon />}
                sx={{ 
                  mt: 4, 
                  mb: 2, 
                  py: 1.5, 
                  fontSize: '1rem', 
                  fontWeight: 'bold',
                  boxShadow: 'none',
                  '&:hover': { boxShadow: '0 4px 12px rgba(46, 125, 50, 0.3)' }
                }}
              >
                {isLoading ? <CircularProgress size={26} color="inherit" /> : "LOGIN"}
              </Button>
            </Box>
          </Box>

          <Box sx={{ position: 'absolute', bottom: 20, width: '100%', textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {'Copyright © RecyclAI '}
              {new Date().getFullYear()}
            </Typography>
          </Box>

        </Grid>
      </Grid>
    </ThemeProvider>
  );
}

export default LoginForm;
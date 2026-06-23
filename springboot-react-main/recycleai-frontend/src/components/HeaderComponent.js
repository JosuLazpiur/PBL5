import React from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { 
  AppBar, 
  Toolbar, 
  Typography, 
  Box, 
  Avatar, 
  Tooltip, 
  IconButton,
  Container
} from "@mui/material";
import { Logout, Recycling } from "@mui/icons-material";

export const HeaderComponent = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const goHome = () => {
    if (user?.domain?.domainId) {
      navigate(`/user/${user.domain.domainId}`);
    }
  };

  const userInitial = user?.name ? user.name.charAt(0).toUpperCase() : "U";

  return (
    <AppBar 
      position="sticky" 
      elevation={1} 
      sx={{ 
        backgroundColor: 'white', 
        color: '#333',
        borderBottom: '1px solid rgba(0,0,0,0.05)'
      }}
    >
      <Container maxWidth="xl">
        <Toolbar disableGutters sx={{ display: 'flex', justifyContent: 'space-between' }}>
          
          {/* --- IZQUIERDA: LOGO Y MARCA --- */}
          <Box 
            onClick={goHome} 
            sx={{ 
              display: 'flex', 
              alignItems: 'center', 
              cursor: 'pointer',
              '&:hover': { opacity: 0.8 } 
            }}
          >
            {/* Icono de Reciclaje */}
            <Box sx={{ 
              mr: 1.5, 
              bgcolor: '#e8f5e9', 
              p: 0.5, 
              borderRadius: 1, 
              display: 'flex' 
            }}>
              <Recycling sx={{ color: '#2E7D32', fontSize: 28 }} />
            </Box>
            
            <Typography
              variant="h6"
              noWrap
              sx={{
                fontWeight: 800,
                letterSpacing: '.05rem',
                color: '#2E7D32',
                textDecoration: 'none',
              }}
            >
              RECYCL<span style={{ color: '#1565C0' }}>AI</span>
            </Typography>
          </Box>

          {/* --- DERECHA: PERFIL USUARIO Y LOGOUT --- */}
          {user && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              
              {/* Información del Usuario (Visible en escritorio) */}
              <Box sx={{ display: { xs: 'none', sm: 'block' }, textAlign: 'right' }}>
                <Typography variant="subtitle2" fontWeight="bold" sx={{ color: '#333' }}>
                  {user.name}
                </Typography>
                <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mt: -0.5 }}>
                  {user.role || 'Admin'}
                </Typography>
              </Box>

              {/* Avatar con inicial */}
              <Avatar sx={{ bgcolor: '#1565C0', width: 35, height: 35, fontSize: '0.9rem' }}>
                {userInitial}
              </Avatar>

              {/* Botón de Logout Separado */}
              <Tooltip title="Logout">
                <IconButton onClick={handleLogout} size="small" sx={{ ml: 1, color: '#d32f2f' }}>
                  <Logout />
                </IconButton>
              </Tooltip>
            </Box>
          )}

        </Toolbar>
      </Container>
    </AppBar>
  );
};

export default HeaderComponent;
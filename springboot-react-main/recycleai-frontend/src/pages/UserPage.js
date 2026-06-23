import React, { useState, useEffect } from "react";
import BinListComponent from "../components/BinListComponent";
import HeaderComponent from "../components/HeaderComponent";
import { FooterComponent } from "../components/FooterComponent";
import BinService from "../services/BinService";
import { useAuth } from "../context/AuthContext";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import { CssBaseline, Box, Container, createTheme, ThemeProvider, CircularProgress } from "@mui/material";

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

const updateBinsList = (prevBins, updatedBin, userDomainId) => {
  const index = prevBins.findIndex((b) => b.binId === updatedBin.binId);
  if (index >= 0) {
    const newBins = [...prevBins];
    newBins[index] = updatedBin;
    return newBins;
  } 

  if (updatedBin.domain?.domainId === userDomainId) {
    return [...prevBins, updatedBin];
  }
  return prevBins;
};

const UserPage = () => {
  const { user } = useAuth();
  const [bins, setBins] = useState([]);
  const [loading, setLoading] = useState(true);

  const onBinUpdate = React.useCallback((message) => {
    const updatedBin = JSON.parse(message.body);
    setBins((prevBins) => updateBinsList(prevBins, updatedBin, user?.domain?.domainId));
  }, [user]);

  useEffect(() => {
    if (user?.domain) {
      setLoading(true);
      BinService.getBinsByDomain(user.domain.domainId)
        .then((res) => {
          setBins(res.data);
          setLoading(false)
        })
        .catch((err) => {
          console.error("Error fetching bins:", err);
          setLoading(false);
        });
    }
  }, [user]);

  useEffect(() => {
    if (!user) {
      return undefined;
    }

    const socket = new SockJS("http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      onConnect: () => {
        client.subscribe("/topic/bins", onBinUpdate);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [user, onBinUpdate]);

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        
        <HeaderComponent />

        <Box component="main" sx={{ flexGrow: 1, py: 4, px: 2 }}>
          <Container maxWidth="xl">
            {loading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
                <CircularProgress color="primary" />
              </Box>
            ) : (
              <BinListComponent bins={bins} />
            )}
          </Container>
        </Box>

        <FooterComponent />
      
      </Box>
    </ThemeProvider>
  );
};

export default UserPage;
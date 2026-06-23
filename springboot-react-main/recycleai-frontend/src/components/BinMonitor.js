import React, { useEffect, useState, useMemo } from "react";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";
import dayjs from "dayjs";

import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Divider,
} from "@mui/material";

import {
  PhotoCamera as PhotoIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  ReportProblem as ReportIcon,
  LocationOn as LocationIcon,
  Image as ImageIcon
} from "@mui/icons-material";

const BASE_URL = "http://localhost:8080";

const processEvents = (logs, alerts) => {
  const rawEvents = [
      ...logs.map((l) => ({ ...l, type: "log", id: l.logId })),
      ...alerts.map((a) => ({ ...a, type: "alert", id: a.alertId })),
  ];

  const seen = new Set();
  const cleanEvents = rawEvents.filter(item => {
      const uniqueKey = `${item.type}-${item.id}`;
      if (seen.has(uniqueKey)) {
        return false;
      }
      seen.add(uniqueKey);
      return true;
  });

  return cleanEvents
      .filter((item) => item.datetime)
      .sort((a, b) => new Date(b.datetime) - new Date(a.datetime))
      .slice(0, 50);
};

const BinMonitor = ({ bin, image: initialImage, logs: initialLogs = [] }) => {
  
  const formatImageUrl = React.useCallback((path) => {
    if (!path) {
      return "https://placehold.co/800x600/e0e0e0/808080?text=No+Signal";
    }
    
    if (path.startsWith("http")) {
      return path;
    }
    
    return `${BASE_URL}${path}`;
  }, []);

  const [currentImage, setCurrentImage] = useState(formatImageUrl(initialImage));
  
  const [logs, setLogs] = useState(initialLogs);
  const [alerts, setAlerts] = useState([]); 
  const navigate = useNavigate();

  const onLogMessage = React.useCallback((message) => {
    if (message.body) {
      const newLog = JSON.parse(message.body);
      setLogs((prev) => [newLog, ...prev]);
    }
  }, []);

  const onAlertMessage = React.useCallback((message) => {
    if (message.body) {
      const newAlert = JSON.parse(message.body);
      setAlerts((prev) => [newAlert, ...prev]);
    }
  }, []);

  const onImageMessage = React.useCallback((message) => {
    if (message.body) {
      const newImage = JSON.parse(message.body);
      console.log(">> Nueva imagen recibida:", newImage.path);
      setCurrentImage(formatImageUrl(newImage.path));
    }
  }, [formatImageUrl]);

  useEffect(() => {
    if (initialLogs.length > 0) {
        setLogs(prevLogs => {
            const combined = [...initialLogs, ...prevLogs];
            return combined.filter((item, index, self) => 
                index === self.findIndex((t) => String(t.logId) === String(item.logId))
            );
        });
    }
  }, [initialLogs]);

  useEffect(() => {
    setCurrentImage(formatImageUrl(initialImage));
    
    fetch(`${BASE_URL}/api/alerts/${bin.binId}`)
        .then(res => res.json())
        .then(data => setAlerts(data))
        .catch(err => console.error("Error fetching alerts:", err));

  }, [initialImage, bin.binId, formatImageUrl]); 


  useEffect(() => {
    const webSocketFactory = () => new SockJS(`${BASE_URL}/ws`);
    
    const client = new Client({
      webSocketFactory,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log(`[WS] Monitor conectado al Bin ${bin.binId}`);
        client.subscribe(`/topic/logs/${bin.binId}`, onLogMessage);
        client.subscribe(`/topic/alerts/${bin.binId}`, onAlertMessage);
        client.subscribe(`/topic/images/${bin.binId}`, onImageMessage);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [bin.binId, onLogMessage, onAlertMessage, onImageMessage]);


  const uniqueEvents = useMemo(() => processEvents(logs, alerts), [logs, alerts]);


  return (
    <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column' }}>
      
      {/* HEADER */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" fontWeight="800" color="primary">
            Monitor Dashboard
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', color: 'text.secondary', mt: 1 }}>
            <LocationIcon sx={{ mr: 1 }} />
            <Typography variant="h6">
                {bin.ubication || "Unknown Location"} <span style={{ opacity: 0.6, fontSize: '0.9em' }}>(ID: {bin.binId})</span>
            </Typography>
        </Box>
      </Box>

      {/* CONTENEDOR PRINCIPAL */}
      <Box sx={{ height: { xs: 'auto', md: '75vh' }, width: '100%' }}>
        <Grid container spacing={2} sx={{ height: '100%' }}>
          
          {/* COLUMNA IMAGEN */}
          <Grid item xs={12} md={7} sx={{ height: { xs: '500px', md: '100%' } }}>
            <Card elevation={3} sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 3, overflow: 'hidden' }}>
              <Box sx={{ p: 2, bgcolor: '#f5f5f5', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #e0e0e0', flexShrink: 0 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center' }}>
                     <ImageIcon sx={{ mr: 1, color: '#555' }} />
                     <Typography variant="subtitle1" fontWeight="bold">Live View</Typography>
                  </Box>
                  <Chip 
                    icon={<PhotoIcon sx={{ fontSize: '16px !important' }} />} 
                    label="Latest Snapshot" 
                    size="small"
                    sx={{ bgcolor: 'white', border: '1px solid #ccc', fontWeight: 'bold' }} 
                  />
              </Box>
              
              <Box sx={{ flexGrow: 1, bgcolor: 'black', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', position: 'relative' }}>
                <img
                  src={currentImage}
                  alt="Bin Snapshot"
                  style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain', display: 'block' }}
                  onError={(e) => { 
                      console.log("Error cargando imagen:", currentImage);
                      e.target.src = "https://placehold.co/800x600/333/fff?text=No+Signal"; 
                  }}
                />
              </Box>
            </Card>
          </Grid>

          {/* COLUMNA LOGS & ALERTAS */}
          <Grid item xs={12} md={5} sx={{ height: '100%' }}>
            <Card elevation={3} sx={{ height: '100%', display: 'flex', flexDirection: 'column', borderRadius: 3 }}>
              
              <Box sx={{ p: 2, borderBottom: '1px solid #eee', flexShrink: 0 }}>
                  <Typography variant="h6" fontWeight="bold">
                      Recent Activity
                  </Typography>
              </Box>

              <CardContent sx={{ flexGrow: 1, overflowY: 'auto', p: 0, height: '0px' }}>
                {uniqueEvents.length === 0 ? (
                  <Box sx={{ textAlign: 'center', py: 5, color: 'text.secondary', px: 2 }}>
                      <InfoIcon sx={{ fontSize: 40, mb: 1, opacity: 0.3 }} />
                      <Typography variant="body2">No recent activity.</Typography>
                  </Box>
                ) : (
                  <List sx={{ p: 0 }}>
                    {uniqueEvents.map((item, index) => {
                      const isAlert = item.type === "alert";
                      return (
                        <React.Fragment key={`${item.type}-${item.id}`}>
                          <ListItem alignItems="flex-start" sx={{ px: 3, py: 2 }}>
                            <ListItemIcon sx={{ minWidth: 40, mt: 0.5 }}>
                              {isAlert ? <WarningIcon color="error" /> : <InfoIcon color="primary" />}
                            </ListItemIcon>
                            <ListItemText
                              primary={
                                <Typography variant="body1" fontWeight={isAlert ? "bold" : "medium"} color={isAlert ? "error.main" : "text.primary"} sx={{ lineHeight: 1.2 }}>
                                  {isAlert ? item.title + ": " + item.description : item.description}
                                </Typography>
                              }
                              secondary={
                                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                                  {dayjs(item.datetime).format("DD MMM · HH:mm:ss")}
                                </Typography>
                              }
                            />
                          </ListItem>
                          <Divider component="li" />
                        </React.Fragment>
                      );
                    })}
                  </List>
                )}
              </CardContent>

              <Box sx={{ p: 2, borderTop: '1px solid #eee', bgcolor: '#fff', flexShrink: 0 }}>
                <Button
                  variant="outlined" color="error" fullWidth size="large" startIcon={<ReportIcon />}
                  onClick={() => navigate(`/bin/${bin.binId}/report`)}
                  sx={{ borderWidth: 2, fontWeight: 'bold', '&:hover': { borderWidth: 2 } }}
                >
                  Report Issue
                </Button>
              </Box>
            </Card>
          </Grid>

        </Grid>
      </Box>
    </Box>
  );
};

BinMonitor.propTypes = {
    bin: PropTypes.shape({
        binId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        ubication: PropTypes.string,
    }).isRequired,
    image: PropTypes.string,
    logs: PropTypes.arrayOf(PropTypes.shape({
        logId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        datetime: PropTypes.string,
        description: PropTypes.string,
    })),
};

export default BinMonitor;
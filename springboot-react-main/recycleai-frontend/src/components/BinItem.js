import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";
import BinService from "../services/BinService";


import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

import { 
  Card, CardContent, CardActions, Typography, Box, Chip, Button, Divider
} from "@mui/material";
import { 
  Edit as EditIcon, LocationOn as LocationIcon, ReportProblem as BrokenIcon, Engineering as TechIcon
} from "@mui/icons-material";

const BinItem = ({ bin }) => {
  const navigate = useNavigate();
  
 
  const [isOperative, setIsOperative] = useState(bin.operative === true || bin.operative === "true");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const onBinUpdate = (message) => {
      if (message.body) {
        const updatedBin = JSON.parse(message.body);
        console.log(`[WS] Bin ${bin.binId} actualizado:`, updatedBin);
        setIsOperative(updatedBin.operative);
      }
    };

    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws', 
      webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
      
      onConnect: () => {
        client.subscribe(`/topic/bins/${bin.binId}`, onBinUpdate);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [bin.binId]);

  useEffect(() => {
    const serverStatus = bin.operative === true || bin.operative === "true";
    setIsOperative(serverStatus);
  }, [bin.operative]);

  const handleClick = () => {
    navigate(`/bin/${bin.binId}`);
  };

  const navigateEdit = (e) => {
    e.stopPropagation();
    navigate(`/bin/edit/${bin.binId}`);
  };

  const handleBreak = async (e) => {
    e.stopPropagation();
    setLoading(true); 
    try {
      const updatedBin = { ...bin, operative: false };
      await BinService.updateBin(bin.binId, updatedBin);
      setIsOperative(false); 
    } catch (error) {
      console.error("Error reporting bin:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card 
      elevation={2}
      onClick={handleClick}
      sx={{
        width: '100%',
        cursor: 'pointer',
        borderRadius: '12px',
        borderLeft: isOperative ? '6px solid #2E7D32' : '6px solid #d32f2f',
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 8px 16px rgba(0,0,0,0.1)' }
      }}
    >
      <CardContent sx={{ pb: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h5" fontWeight="bold" color={isOperative ? "#2E7D32" : "#d32f2f"}>
                Bin {bin.binId}
            </Typography>
            <Chip 
                label={isOperative ? "OPERATIVE" : "MAINTENANCE"} 
                color={isOperative ? "success" : "error"} 
                size="small" variant="filled"
                icon={isOperative ? undefined : <TechIcon />}
                sx={{ fontWeight: 'bold', minWidth: '90px' }}
            />
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', color: 'text.secondary', mt: 1 }}>
            <LocationIcon fontSize="small" sx={{ mr: 0.5, color: '#757575' }} />
            <Typography variant="body1">{bin.ubication || "Unknown Location"}</Typography>
        </Box>
      </CardContent>
      <Divider light />
      <CardActions sx={{ justifyContent: 'space-between', p: 2 }}> 
        <Button variant="text" startIcon={<EditIcon />} onClick={navigateEdit} size="small" sx={{ color: '#555' }}>
            Edit Info
        </Button>
        {isOperative ? (
            <Button variant="outlined" color="error" startIcon={<BrokenIcon />} onClick={handleBreak} disabled={loading} size="small" sx={{ fontWeight: 'bold', border: '2px solid' }}>
                {loading ? "Reporting..." : "Broken"}
            </Button>
        ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', color: '#d32f2f' }}>
                <Typography variant="caption" fontWeight="bold" sx={{ display: 'flex', alignItems: 'center' }}>
                   <TechIcon fontSize="small" sx={{ mr: 0.5 }}/> Waiting for repairs...
                </Typography>
            </Box>
        )}
      </CardActions>
    </Card>
  );
};

BinItem.propTypes = {
    bin: PropTypes.shape({
        binId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        operative: PropTypes.oneOfType([PropTypes.bool, PropTypes.string]),
        ubication: PropTypes.string,
    }).isRequired,
};

export default BinItem;
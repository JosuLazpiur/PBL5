import React from "react";
import PropTypes from "prop-types";
import BinItem from "./BinItem";
import { Grid, Typography, Box, Paper } from "@mui/material";

const BinListComponent = ({ bins }) => {
  
  if (!bins || bins.length === 0) {
    return (
      <Paper 
        elevation={0} 
        sx={{ 
          p: 5, 
          textAlign: 'center', 
          bgcolor: 'transparent',
          mt: 4,
          border: '2px dashed rgba(0,0,0,0.1)',
          borderRadius: 2
        }}
      >
        <Typography variant="h6" color="text.secondary" sx={{ mt: 2 }}>
          No bins found.
        </Typography>
      </Paper>
    );
  }

  return (
    <Box sx={{ width: '100%', mt: 2 }}>
      <Typography variant="h4" component="h2" sx={{ mb: 4, fontWeight: '800', color: '#2E7D32' }}>
        Your Bins: 
      </Typography>
      
      <Grid container spacing={3}>
        {bins.map((bin) => (
          <Grid item key={bin.binId} xs={12} md={6}>
            <BinItem bin={bin} />
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

BinListComponent.propTypes = {
    bins: PropTypes.arrayOf(PropTypes.shape({
        binId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
    })),
};

export default BinListComponent;
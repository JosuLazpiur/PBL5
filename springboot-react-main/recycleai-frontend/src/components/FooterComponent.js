import React from "react";
import { Box, Container, Typography, Link, Grid } from "@mui/material";

export const FooterComponent = () => {
  return (
    <Box
      component="footer"
      sx={{
        py: 3,
        mt: 'auto',
        backgroundColor: "white",
        borderTop: "1px solid rgba(0,0,0,0.05)",
        width: "100%"
      }}
    >
      <Container maxWidth={false}>
        <Grid container justifyContent="space-between" alignItems="center">
          
          <Grid item xs={12} sm={6} sx={{ textAlign: { xs: 'center', sm: 'left' } }}>
            <Typography variant="body2" color="text.secondary">
              © {new Date().getFullYear()} 
              <Box component="span" sx={{ color: '#2E7D32', fontWeight: 'bold', ml: 0.5 }}>
                RECYCLAI
              </Box>
              . All rights reserved.
            </Typography>
          </Grid>

          <Grid item xs={12} sm={6} sx={{ textAlign: { xs: 'center', sm: 'right' } }}>
            <Link href="#" color="inherit" underline="hover" sx={{ ml: 3, fontSize: '0.875rem' }}>
              Privacy Policy
            </Link>
            <Link href="#" color="inherit" underline="hover" sx={{ ml: 3, fontSize: '0.875rem' }}>
              Terms of Use
            </Link>
            <Link href="#" color="inherit" underline="hover" sx={{ ml: 3, fontSize: '0.875rem' }}>
              Support
            </Link>
          </Grid>

        </Grid>
      </Container>
    </Box>
  );
};
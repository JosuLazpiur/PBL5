import React from 'react';
import { render, screen } from '@testing-library/react';
import { FooterComponent } from '../../components/FooterComponent';

describe('FooterComponent', () => {
  test('renders copyright and links', () => {
    render(<FooterComponent />);

    const currentYear = new Date().getFullYear();
    expect(screen.getByText(new RegExp(`© ${currentYear}`))).toBeInTheDocument();
    // Use regex to be safer
    expect(screen.getByText(/RECYCLAI/)).toBeInTheDocument();
    expect(screen.getByText('Privacy Policy')).toBeInTheDocument();
    expect(screen.getByText('Terms of Use')).toBeInTheDocument();
    expect(screen.getByText('Support')).toBeInTheDocument();
  });
});

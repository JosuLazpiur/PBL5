import React from 'react';
import { render, screen } from '@testing-library/react';
import AlertMessage from '../../components/AllertMessage';

describe('AllertMessage', () => {
  test('renders message when provided', () => {
    const message = "Something went wrong";
    render(<AlertMessage message={message} />);
    
    expect(screen.getByText(message)).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveClass('alert alert-danger');
  });

  test('does not render when message is empty or null', () => {
    const { container } = render(<AlertMessage message={null} />);
    expect(container).toBeEmptyDOMElement();

    const { container: container2 } = render(<AlertMessage message="" />);
    expect(container2).toBeEmptyDOMElement();
  });
});

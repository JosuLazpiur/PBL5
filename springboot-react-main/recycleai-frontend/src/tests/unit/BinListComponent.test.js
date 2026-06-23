import React from 'react';
import { render, screen } from '@testing-library/react';
import BinListComponent from '../../components/BinListComponent';

// Mock de BinItem para aislar el test de la lista
jest.mock('../../components/BinItem', () => (props) => (
  <div data-testid="bin-item">{props.bin.name}</div>
));

describe('BinListComponent', () => {
  test('renders message when list is empty', () => {
    render(<BinListComponent bins={[]} />);
    expect(screen.getByText('No bins found.')).toBeInTheDocument();
  });

  test('renders message when bins is null', () => {
    render(<BinListComponent bins={null} />);
    expect(screen.getByText('No bins found.')).toBeInTheDocument();
  });

  test('renders list of bins', () => {
    const bins = [
      { binId: 1, name: 'Bin 1' },
      { binId: 2, name: 'Bin 2' }
    ];

    render(<BinListComponent bins={bins} />);

    expect(screen.getByText('Your Bins:')).toBeInTheDocument();
    const items = screen.getAllByTestId('bin-item');
    expect(items).toHaveLength(2);
    expect(screen.getByText('Bin 1')).toBeInTheDocument();
    expect(screen.getByText('Bin 2')).toBeInTheDocument();
  });
});

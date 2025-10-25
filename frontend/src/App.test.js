import { render, screen } from '@testing-library/react';
import App from './App';

test('renders admin dashboard heading', () => {
  render(<App />);
  const heading = screen.getByText(/University Management — Admin/i);
  expect(heading).toBeInTheDocument();
});

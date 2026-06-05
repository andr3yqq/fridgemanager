import { render, screen } from '@testing-library/react';
import App from './App';
import { AppProvider } from "./context/AppContext";

test('renders login page title', () => {
  render(
    <AppProvider>
      <App />
    </AppProvider>
  );
  const titleElement = screen.getByRole('heading', { name: /login/i });
  expect(titleElement).toBeInTheDocument();
});

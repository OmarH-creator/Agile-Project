import AppRoutes from './routes/AppRoutes';
import './App.css';

import { Toaster } from 'react-hot-toast';

function App() {
  return (
    <>
      <Toaster position="bottom-right" reverseOrder={false} />
      <AppRoutes />
    </>
  );
}

export default App;

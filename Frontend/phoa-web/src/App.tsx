import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { appRoutes } from './routes';
import Navbar from './components/Navbar/Navbar';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        {appRoutes.map((route) => (
          <Route 
            key={route.path} 
            path={route.path} 
            element={route.element} 
          />
        ))}
      </Routes>
    </Router>
  );
}

export default App;
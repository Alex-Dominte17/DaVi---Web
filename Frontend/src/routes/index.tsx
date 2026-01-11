import { ReactNode } from 'react';
import DashboardView from '../components/Dashboard/components/DashboardView';
import Home from '../components/Home/Home';
import Resources from '../components/Resources/Resources';
import Entourage from '../components/Entourage/Entourage';
import Sparql from '../components/SPARQL/SPARQL';

export const ROUTES = {
  HOME: '/',
  DASHBOARD: '/dashboard',
  RESOURCES: '/resources',
  ENTOURAGE: '/entourage',
  SPARQL: '/sparql'
};

interface RouteConfig {
  path: string;
  element: ReactNode;
}

export const appRoutes: RouteConfig[] = [
  { path: ROUTES.HOME, element: <Home /> },
  { path: ROUTES.DASHBOARD, element: <DashboardView /> },
  { path: ROUTES.RESOURCES, element: <Resources /> },
  { path: ROUTES.ENTOURAGE, element: <Entourage /> },
  { path: ROUTES.SPARQL, element: <Sparql /> }
];
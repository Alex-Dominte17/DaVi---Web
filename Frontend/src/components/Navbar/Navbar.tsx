import { NavLink } from "react-router-dom";
import { ROUTES } from "../../routes/index";
import {
  Brain,
  Home,
  LayoutDashboard,
  Library,
  Users,
  Share2,
} from "lucide-react";
import styles from "./Navbar.module.css";

const Navbar = () => {
  return (
    <nav className={styles.nav}>
      <NavLink to={ROUTES.HOME} className={styles.logoSection}>
        <div className={styles.logoIcon}>
          <Brain size={24} />
        </div>
        <span className={styles.logoText}>
          Pho<span>A</span>
        </span>
      </NavLink>

      <div className={styles.links}>
        <NavLink
          to={ROUTES.HOME}
          className={({ isActive }) => (isActive ? styles.active : "")}
        >
          <Home size={18} /> Home
        </NavLink>
        <NavLink
          to={ROUTES.DASHBOARD}
          className={({ isActive }) => (isActive ? styles.active : "")}
        >
          <LayoutDashboard size={18} /> Dashboard
        </NavLink>
        <NavLink
          to={ROUTES.RESOURCES}
          className={({ isActive }) => (isActive ? styles.active : "")}
        >
          <Library size={18} /> Resources
        </NavLink>
        <NavLink
          to={ROUTES.ENTOURAGE}
          className={({ isActive }) => (isActive ? styles.active : "")}
        >
          <Users size={18} /> Entourage
        </NavLink>
        <NavLink
          to={ROUTES.SPARQL}
          className={({ isActive }) => (isActive ? styles.active : "")}
        >
          <Share2 size={18} /> SPARQL
        </NavLink>
      </div>

      <div className={styles.authButtons}>
        <button className={styles.signIn}>Sign In</button>
        <button className={styles.getStarted}>Get Started</button>
      </div>
    </nav>
  );
};

export default Navbar;

// import React from 'react';
// import { NavLink } from "react-router-dom";
// import { ROUTES } from "../../routes/index";
// import {
//   Brain,
//   Home,
//   LayoutDashboard,
//   Library,
//   Users,
//   Database
// } from "lucide-react";
// import styles from "./Navbar.module.css";

// const Navbar = () => {
//   return (
//     <nav className={styles.nav}>
//       <NavLink to={ROUTES.HOME} className={styles.logoSection}>
//         <div className={styles.logoIcon}>
//           <Brain size={24} />
//         </div>
//         <span className={styles.logoText}>
//           Pho<span>A</span>
//         </span>
//       </NavLink>

//       <div className={styles.links}>
//         <NavLink
//           to={ROUTES.HOME}
//           className={({ isActive }) => (isActive ? styles.active : "")}
//         >
//           <Home size={18} /> Home
//         </NavLink>
//         <NavLink
//           to={ROUTES.DASHBOARD}
//           className={({ isActive }) => (isActive ? styles.active : "")}
//         >
//           <LayoutDashboard size={18} /> Dashboard
//         </NavLink>
//         <NavLink
//           to={ROUTES.RESOURCES}
//           className={({ isActive }) => (isActive ? styles.active : "")}
//         >
//           <Library size={18} /> Resources
//         </NavLink>
//         <NavLink
//           to={ROUTES.ENTOURAGE}
//           className={({ isActive }) => (isActive ? styles.active : "")}
//         >
//           <Users size={18} /> Entourage
//         </NavLink>
//         <NavLink
//           to={ROUTES.INGEST}
//           className={({ isActive }) => (isActive ? styles.active : "")}
//         >
//           <Database size={18} /> Ingest
//         </NavLink>
//       </div>

//       <div className={styles.authButtons}>
//         <button className={styles.signIn}>Sign In</button>
//         <button className={styles.getStarted}>Get Started</button>
//       </div>
//     </nav>
//   );
// };

// export default Navbar;



import React, { useState } from 'react'; // Adăugat useState
import { NavLink } from "react-router-dom";
import { ROUTES } from "../../routes/index";
import {
  Brain,
  Home,
  LayoutDashboard,
  Library,
  Users,
  Database,
  Menu, // Adăugat
  X    // Adăugat
} from "lucide-react";
import styles from "./Navbar.module.css";

const Navbar = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => setIsMenuOpen(!isMenuOpen);
  const closeMenu = () => setIsMenuOpen(false);

  return (
    <nav className={styles.nav}>
      <NavLink to={ROUTES.HOME} className={styles.logoSection} onClick={closeMenu}>
        <div className={styles.logoIcon}>
          <Brain size={24} />
        </div>
        <span className={styles.logoText}>
          Pho<span>A</span>
        </span>
      </NavLink>

      {/* Buton Hamburger pentru Mobil */}
      <button className={styles.mobileMenuBtn} onClick={toggleMenu}>
        {isMenuOpen ? <X size={28} /> : <Menu size={28} />}
      </button>

      {/* Overlay/Container Link-uri */}
      <div className={`${styles.navContent} ${isMenuOpen ? styles.show : ""}`}>
        <div className={styles.links}>
          <NavLink
            to={ROUTES.HOME}
            className={({ isActive }) => (isActive ? styles.active : "")}
            onClick={closeMenu}
          >
            <Home size={18} /> Home
          </NavLink>
          <NavLink
            to={ROUTES.DASHBOARD}
            className={({ isActive }) => (isActive ? styles.active : "")}
            onClick={closeMenu}
          >
            <LayoutDashboard size={18} /> Dashboard
          </NavLink>
          <NavLink
            to={ROUTES.INGEST}
            className={({ isActive }) => (isActive ? styles.active : "")}
            onClick={closeMenu}
          >
            <Database size={18} /> Ingest
          </NavLink>
          <NavLink
            to={ROUTES.RESOURCES}
            className={({ isActive }) => (isActive ? styles.active : "")}
            onClick={closeMenu}
          >
            <Library size={18} /> Resources
          </NavLink>
          <NavLink
            to={ROUTES.ENTOURAGE}
            className={({ isActive }) => (isActive ? styles.active : "")}
            onClick={closeMenu}
          >
            <Users size={18} /> Entourage
          </NavLink>
        </div>

        <div className={styles.authButtons}>
          <button className={styles.signIn}>Sign In</button>
          <button className={styles.getStarted}>Get Started</button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

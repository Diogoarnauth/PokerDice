import React from "react";
import { NavLink } from "react-router-dom";
import "./Navbar.css";

export function Navbar() {
    return (
        <nav className="navbar">
            <span className="navbar-logo">PokerDice</span>

            <NavLink to="/login" className="nav-link">
                Login
            </NavLink>

            <NavLink to="/signup" className="nav-link">
                Sign Up
            </NavLink>

            <NavLink to="/lobbies" className="nav-link">
                Lobbies
            </NavLink>

            <NavLink to="/lobbies/create" className="nav-link">
                Create Lobby
            </NavLink>

            <NavLink to="/playerProfile" className="nav-link">
                Player Profile
            </NavLink>

            <NavLink to="/about" className="nav-link">
                About
            </NavLink>

            <NavLink to="/appInvite" className="nav-link">
                App Invite
            </NavLink>
        </nav>
    );
}

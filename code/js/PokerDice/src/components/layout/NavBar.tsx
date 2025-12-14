import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import "../../styles/NavBar.css";
import { useAuthentication } from "../../providers/Authentication";
import { settingsService } from "../../services/api/settings";

export function NavBar() {
    const { username, setUsername } = useAuthentication();
    const isAuthenticated = !!username;
    const firstLetter = username ? username.charAt(0).toUpperCase() : "?";
    const navigate = useNavigate();

    async function handleLogout() {
        try {
            await settingsService.logout();
        } catch (err) {
            console.error("Error in backend logout.", err);
        } finally {
            setUsername(null);
            navigate("/login");
        }
    }

    return (
        <nav className="navBar">
            <div className="navBar-left">
                <NavLink to="/" className="navBar-logo">
                    PokerDice
                </NavLink>

                {!isAuthenticated && (
                    <>
                        <NavLink to="/login" className="nav-link">
                            Login
                        </NavLink>

                        <NavLink to="/signup" className="nav-link">
                            Sign Up
                        </NavLink>
                    </>
                )}

                {isAuthenticated && (
                    <>
                        <NavLink to="/lobbies" className="nav-link">
                            Lobbies
                        </NavLink>

                        <NavLink to="/lobbies/create" className="nav-link">
                            Create Lobby
                        </NavLink>

                        <NavLink to="/appInvite" className="nav-link">
                            App Invite
                        </NavLink>
                    </>
                )}
            </div>

            {isAuthenticated && (
                <div className="navBar-right">
                    <NavLink to="/about" className="navBar-about-icon" title="About">
                        <span>i</span>
                    </NavLink>

                    <div className="navBar-profile-wrapper" title={username || "Profile"}>
                        <div className="navBar-avatar">
                            <span>{firstLetter}</span>
                        </div>

                        <div className="navBar-dropdown">
                            <NavLink to="/profile" className="navBar-dropdown-item">
                                Player Profile
                            </NavLink>
                            <NavLink to="/deposit" className="navBar-dropdown-item">
                                Deposit
                            </NavLink>
                            <button
                                type="button"
                                className="navBar-dropdown-item navBar-dropdown-button"
                                onClick={handleLogout}
                            >
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </nav>
    );
}

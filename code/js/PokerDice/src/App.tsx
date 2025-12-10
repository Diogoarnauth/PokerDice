import React from 'react';
import { BrowserRouter, Link, Route, Routes, Outlet } from "react-router-dom";
import Login from "./components/auth/Login";
import Signup from "./components/auth/Signup";
import Lobbies from "./components/lobby/Lobbies";
import LobbyDetails from "./components/lobby/LobbyDetails";
import LobbyCreation from "./components/lobby/LobbyCreation";
import PlayerProfile from "./components/Player/PlayerProfile";
import AppInvite from "./components/auth/AppInvite";
import About from "./components/about/About";
import GamePage from "./components/game/Game";
import ErrorPage from "./components/error/ErrorPage";
import Home from "./components/layout/Home";
import { RequireAuthentication } from "./components/auth/RequireAuthentication";

// Providers
import { AlertProvider } from "./providers/AlertContexts";
import { SSEProvider } from "./providers/SSEContext";
import { AuthenticationProvider } from "./providers/authentication";

function App() {
    return (
        <AlertProvider>
            {/* O AuthenticationProvider envolve a app para gerir o estado do user */}
            <AuthenticationProvider>
                <SSEProvider>
                    <BrowserRouter>
                        { /* nav bar (pode ser criado um componentn para isto)*/}
                        <nav className="flex gap-6 p-4 bg-gray-100 border-b border-gray-300 items-center">
                            <Link to="/" className="hover:text-purple-600 font-medium">Home</Link>
                            <Link to="/login" className="hover:text-purple-600 font-medium">Login</Link>
                            <Link to="/signup" className="hover:text-purple-600 font-medium">Sign Up</Link>
                            <Link to="/lobbies" className="hover:text-purple-600 font-medium">Lobbies</Link>
                            <Link to="/lobbies/create" className="hover:text-purple-600 font-medium">Create</Link>
                            <Link to="/playerProfile" className="hover:text-purple-600 font-medium">Profile</Link>
                            <Link to="/about" className="hover:text-purple-600 font-medium">About</Link>
                            <Link to="/appInvite" className="hover:text-purple-600 font-medium">Invite</Link>
                        </nav>

                        <div className="container mx-auto p-6">
                            <Routes>
                                {/* Rotas s/login*/}
                                <Route path="/" element={<Home />} />
                                <Route path="/login" element={<Login />} />
                                <Route path="/signup" element={<Signup />} />
                                <Route path="/about" element={<About />} />
                                <Route path="/error" element={<ErrorPage />} />

                                {/* Rotas c/ login */}
                                {/* Usamos um Route wrapper para proteger todas as rotas filhas de uma vez */}
                                <Route element={
                                    <RequireAuthentication>
                                        <Outlet />
                                    </RequireAuthentication>
                                }>
                                    <Route path="/lobbies" element={<Lobbies />} />
                                    <Route path="/lobbies/:id/info" element={<LobbyDetails />} />
                                    <Route path="/lobbies/create" element={<LobbyCreation />} />
                                    <Route path="/games/lobby/:lobbyId" element={<GamePage />} />
                                    <Route path="/playerProfile" element={<PlayerProfile />} />
                                    <Route path="/appInvite" element={<AppInvite />} />
                                </Route>

                                {/* Rota para os inválidos */}
                                <Route path="*" element={<ErrorPage />} />
                            </Routes>
                        </div>
                    </BrowserRouter>
                </SSEProvider>
            </AuthenticationProvider>
        </AlertProvider>
    );
}

export default App;
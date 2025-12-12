import React from 'react';
import { BrowserRouter, Route, Routes, Outlet } from "react-router-dom";
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
import { NavBar } from "./components/layout/NavBar";

// Providers
import { AlertProvider } from "./providers/AlertContexts";
import { SSEProvider } from "./providers/SSEContext";
import { AuthenticationProvider } from "./providers/Authentication";

function App() {
    return (
        <AlertProvider>
            {/* O AuthenticationProvider envolve a app para gerir o estado do user */}
            <AuthenticationProvider> {/* 1º: Carrega a sessão */}
                <SSEProvider> {/* 2º: Depende da sessão para ligar o socket */}
                    <BrowserRouter>

                        <NavBar />

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
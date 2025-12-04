import React from 'react';
import { BrowserRouter, Route, Routes } from "react-router-dom";
import Login from "./components/auth/Login";
import Signup from "./components/auth/Signup";
import Lobbies from "./components/lobby/Lobbies";
import LobbyDetails from "./components/lobby/LobbyDetails";
import LobbyCreation from "./components/lobby/LobbyCreation";
import PlayerProfile from "./components/Player/PlayerProfile";
import AppInvite from "./components/auth/AppInvite";
import About from "./components/about/About";
import GamePage from "./components/game/Game";
import HomePage from "./components/layout/Home"
import { Navbar } from "./components/layout/NavBar"

function App() {
    return (
        <BrowserRouter>
            <Navbar />

            <Routes>
                {/* página principal */}
                <Route path="/" element={<HomePage />} />

                {/* resto das páginas */}
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/lobbies" element={<Lobbies />} />
                <Route path="/lobbies/:id/info" element={<LobbyDetails />} />
                <Route path="/lobbies/create" element={<LobbyCreation />} />
                <Route path="/playerProfile" element={<PlayerProfile />} />
                <Route path="/about" element={<About />} />
                <Route path="/appInvite" element={<AppInvite />} />
                <Route path="/games/lobby/:lobbyId" element={<GamePage />} />

                {/* rota para URLs inválidos */}
                <Route
                    path="*"
                    element={
                        <div>
                            <h1>404 - Page not found</h1>
                            <p>This page does not exist.</p>
                        </div>
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;

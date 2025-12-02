import React from 'react';
import {BrowserRouter, Link, Route, Routes} from "react-router-dom";
import Login from "./components/auth/Login";
import Signup from "./components/auth/Signup"
import Lobbies from "./components/lobby/Lobbies"
import LobbyDetails from "./components/lobby/LobbyDetails"
import LobbyCreation from "./components/lobby/LobbyCreation";
import PlayerProfile from "./components/Player/PlayerProfile";
import AppInvite from "./components/auth/AppInvite";
import About from "./components/about/About";
import GamePage from "./components/game/Game";


// App.tsx
function App() {
    return (
        <BrowserRouter>
            <nav>
                <Link to="/login">Login</Link>
                <Link to="/signup">signUp</Link>
                <Link to="/lobbies">Lobbies</Link>
                <Link to="/lobbies/create">Create Lobby</Link>
                <Link to="/playerProfile">PlayerProfile</Link>
                <Link to="/about">About</Link>
                <Link to="/appInvite">App Invite</Link>

            </nav>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/signup" element={<Signup />} />
                <Route path="/lobbies" element={<Lobbies />} />
                <Route path="/lobbies/:id/info" element={<LobbyDetails />} />
                <Route path="/lobbies/create" element={<LobbyCreation />} />
                <Route path="/playerProfile" element={<PlayerProfile />} />
                <Route path="/about" element={<About />} />
                <Route path="/appInvite" element={<AppInvite />} />
                <Route path="/games/lobby/:lobbyId" element={<GamePage/>} />




                <Route path="/games/lobby/:lobbyId" element={<GamePage />} />





                {/*adicionar outras rotas */}
                <Route path="*" element={
                    <div>
                        <h1>PokerDice App</h1>
                        <p>Bem vindo à página principal.</p>
                    </div>
                } />
            </Routes>
        </BrowserRouter>

    );
}

export default App;



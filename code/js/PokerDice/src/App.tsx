import React from 'react';
import {BrowserRouter, Link, Route, Routes} from "react-router-dom";
import Login from "./components/auth/Login";

// App.tsx
function App() {
    return (
        <BrowserRouter>
            <nav>
                <Link to="/login">Login</Link>
            </nav>
            <Routes>
                <Route path="/login" element={<Login />} />
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



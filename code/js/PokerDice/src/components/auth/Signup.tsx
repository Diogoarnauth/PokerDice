import React, {useEffect, useReducer, useState} from "react";
import {useNavigate} from "react-router-dom";
import {authService} from "../../services/api/auth";
import {getTokenFromCookies, isOk} from "../../services/api/utils";
import "../../styles/auth.css";

export default function Signup() {

  const navigate = useNavigate();

    useEffect(() => {
        const token = getTokenFromCookies();
        if (token) {
            navigate("/");
        }
    }, [navigate]);

    // Fields
    const [username, setUsername] = useState("");
    const [name, setName] = useState("");
    const [age, setAge] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [inviteCode, setInviteCode] = useState("");

    // State
    const [isFirstUser, setIsFirstUser] = useState<boolean | null>(null);
    const [loadingCheck, setLoadingCheck] = useState(true);
    const [loadingSubmit, setLoadingSubmit] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [resultMsg, setResultMsg] = useState<string | null>(null);

    // 1. Fazer checkAdmin antes de renderizar o formulário
    useEffect(() => {

        async function check() {
            const response = await authService.checkAdmin();
            if (isOk(response)) {
                setIsFirstUser(response.value.firstUser);
            } else {
                setError(response.error.title || "Error checking admin status");
            }
            setLoadingCheck(false);
        }
        check();
    }, []);

    if (loadingCheck) {
        return <p>Loading...</p>;
    }

    // 2. Submeter o formulário
    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setResultMsg(null);

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        const parsedAge = Number(age);
        if (isNaN(parsedAge) || parsedAge < 18) {
            setError("Age must be a positive integer");
            return;
        }

        setLoadingSubmit(true);
        const payload = {username, name, age: parsedAge, password, inviteCode};

        const response = isFirstUser
            ? await authService.signupAdmin(payload)
            : await authService.signup(payload);

        if (isOk(response)) {
            setResultMsg("Signup made with success. Redirecting to login...");
            // Aqui você deve armazenar o token no cookie após o signup com sucesso
            // setCookie("token", response.value.tokenValue, 1); // Armazenar o token por 1 dia

            setTimeout(() => {
                navigate("/login");
                console.log( "Redirecionando para login..." );
            }, 1500);
        } else {
            const problem = response.error;
            console.log(" --- Signup error details --- $s", problem);
            if (problem.status && problem.status >= 500) {
                navigate('/error', {state: {error: problem}});
            } else {
                setError(problem.title || "Erro ao criar conta");
            }
        }
        setLoadingSubmit(false);
    }

    return (
        <div className="auth-container">
            <h1 className="auth-title">
                {isFirstUser ? "Create Admin Account" : "Sign Up"}
            </h1>

            {isFirstUser && (
                <p className="auth-text" style={{marginBottom: "10px"}}>
                    This will create the first user as <strong>admin</strong>.
                </p>
            )}

            <form onSubmit={handleSubmit}>
                {/* Desativa tudo enquanto está a submeter */}
                <fieldset disabled={loadingSubmit}>
                    <div className="auth-form-group">
                        <div>
                            <label htmlFor="username" className="auth-label">
                                Username
                            </label>
                            <input
                                className="auth-input"
                                type="text"
                                id="username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                placeholder="Enter your username"
                                required
                            />
                        </div>

                        <div>
                            <label htmlFor="name" className="auth-label">Name</label>
                            <input
                                className="auth-input"
                                type="text"
                                id="name"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="Enter your name"
                                required
                            />
                        </div>

                        <div>
                            <label htmlFor="age" className="auth-label">Age</label>
                            <input
                                className="auth-input"
                                type="number"
                                id="age"
                                value={age}
                                onChange={(e) => setAge(e.target.value)}
                                min={1}
                                placeholder="Enter your age"
                                required
                            />
                        </div>

                        <div>
                            <label htmlFor="password" className="auth-label">Password</label>
                            <input
                                className="auth-input"
                                type="password"
                                id="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="Enter your password"
                                required
                            />
                        </div>

                        <div>
                            <label htmlFor="confirmPassword" className="auth-label">Confirm Password</label>
                            <input
                                className="auth-input"
                                type="password"
                                id="confirmPassword"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                placeholder="Confirm your password"
                                required
                            />
                        </div>

                        {!isFirstUser && (
                            <div>
                                <label htmlFor="inviteCode" className="auth-label">Invite Code</label>
                                <input
                                    className="auth-input"
                                    type="text"
                                    id="inviteCode"
                                    value={inviteCode}
                                    onChange={(e) => setInviteCode(e.target.value)}
                                    placeholder="Enter your invite code"
                                    required
                                />
                            </div>
                        )}

                        <button
                            type="submit"
                            className="auth-submit"
                            disabled={loadingSubmit}
                        >
                            {loadingSubmit
                                ? "Submitting..."
                                : isFirstUser
                                    ? "Create Admin"
                                    : "Sign Up"}
                        </button>
                    </div>
                </fieldset>

                {/* Mensagem de Erro */}
                {error && <div className="auth-error" style={{color: 'red', marginTop: '10px'}}>{error}</div>}

                {/* Mensagem de Sucesso */}
                {resultMsg && <p className="auth-info" style={{color: 'green', marginTop: '10px'}}>{resultMsg}</p>}
            </form>
        </div>
    );
}
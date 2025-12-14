import React, {useEffect, useReducer, useState} from "react";
import {useNavigate} from "react-router-dom";
import {authService} from "../../services/api/auth";
import {isOk} from "../../services/api/utils";
import '../../styles/Signup.css';
import {useAuthentication} from "../../providers/Authentication";


export default function Signup() {

    const navigate = useNavigate();

    const {username} = useAuthentication();

    useEffect(() => {
        if (username) {
            navigate("/");
        }
    }, [username, navigate]);

    // Fields
    const [usernameInput, setUsernameInput] = useState("");
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

    // 1. Fazer checkAdmin antes de renderizar o formulÃ¡rio
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

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);
        setResultMsg(null);

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        const parsedAge = Number(age);
        if (isNaN(parsedAge)) {
            setError("Age must be a positive integer");
            return;
        }

        setLoadingSubmit(true);
        const payload = {username: usernameInput, name, age: parsedAge, password, inviteCode};

        const response = isFirstUser
            ? await authService.signupAdmin(payload)
            : await authService.signup(payload);

        if (isOk(response)) {
            setResultMsg("Signup made with success. Redirecting to login...");
            setTimeout(() => {
                navigate("/login");
            }, 1500);
        } else {
            const problem = response.error;
            console.log(" --- Signup error details --- $s", problem);
            if (problem.status && problem.status >= 500) {
                navigate('/error', {state: {error: problem}});
            } else {
                setError(problem.detail || problem.title || "Error creating account.");
            }
        }
        setLoadingSubmit(false);
    }

    return (
        <div className="login-page">
            <div className="login-card">
                <h1 className="title">
                    {isFirstUser ? "Create Admin Account" : "Sign Up"}
                </h1>

                {isFirstUser && (
                    <p className="signup-text" style={{ marginBottom: "10px" }}>
                        This will create the first user as <strong>admin</strong>.
                    </p>
                )}

                <form onSubmit={handleSubmit}>
                    <fieldset disabled={loadingSubmit}>
                        <div className="input-container">
                            <div>
                                <label htmlFor="username" className="label">
                                    Username
                                </label>
                                <input
                                    className="input"
                                    type="text"
                                    id="username"
                                    value={usernameInput}
                                    onChange={(e) => setUsernameInput(e.target.value)}
                                    placeholder="Enter your username"
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="name" className="label">Name</label>
                                <input
                                    className="input"
                                    type="text"
                                    id="name"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="Enter your name"
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="age" className="label">Age</label>
                                <input
                                    className="input"
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
                                <label htmlFor="password" className="label">Password</label>
                                <input
                                    className="input"
                                    type="password"
                                    id="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="Enter your password"
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="confirmPassword" className="label">Confirm Password</label>
                                <input
                                    className="input"
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
                                    <label htmlFor="inviteCode" className="label">Invite Code</label>
                                    <input
                                        className="input"
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
                                className="submit-button"
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

                    {error && (
                        <div className="auth-error">
                            {error}
                        </div>
                    )}

                    {resultMsg && (
                        <div className="auth-info">
                            {resultMsg}
                        </div>
                    )}
                </form>
            </div>
        </div>
    );
}
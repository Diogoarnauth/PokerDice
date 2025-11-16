import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../../services/api/auth";
import { isOk } from "../../services/api/utils";
import "../../styles/auth.css";

export default function Signup() {
  // Router
  const navigate = useNavigate();

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
  const [result, setResult] = useState<string | null>(null);

  // 1. Fazer checkAdmin antes de renderizar o formulário
  useEffect(() => {
    async function check() {
      const response = await authService.checkAdmin();
      if (isOk(response)) {
        setIsFirstUser(response.value.firstUser);
      } else {
        setError(response.error || "Error checking admin status");
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
    setResult(null);

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    const parsedAge = Number(age);
    if (!Number.isInteger(parsedAge) || parsedAge <= 0) {
      setError("Age must be a positive integer");
      return;
    }

    setLoadingSubmit(true);

    let response;
    if (isFirstUser) {
      // Primeiro utilizador → signup admin (sem invite)
      response = await authService.signupAdmin({
        username,
        name,
        age: parsedAge,
        password,
      });
    } else {
      // Utilizador normal → signup com invite
      response = await authService.signup({
        username,
        name,
        age: parsedAge,
        password,
        inviteCode,
      });
    }

    if (isOk(response)) {
      setResult("Signup made with success. Redirecting to login...");

      setTimeout(() => {
        navigate("/login");
      }, 1000);
    } else {
      setError(response.error || "Unknown error");
    }

    setLoadingSubmit(false);
  }

  return (
    <div className="auth-container">
      <h1 className="auth-title">
        {isFirstUser ? "Create Admin Account" : "Sign Up"}
      </h1>

      {isFirstUser && (
        <p className="auth-text" style={{ marginBottom: "10px" }}>
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
              <label htmlFor="name" className="auth-label">
                Name
              </label>
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
              <label htmlFor="age" className="auth-label">
                Age
              </label>
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
              <label htmlFor="password" className="auth-label">
                Password
              </label>
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
              <label htmlFor="confirmPassword" className="auth-label">
                Confirm Password
              </label>
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

            {/* Só mostramos inviteCode se NÃO for primeiro user */}
            {!isFirstUser && (
              <div>
                <label htmlFor="inviteCode" className="auth-label">
                  Invite Code
                </label>
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

        {error && <div className="auth-error">{error}</div>}

        {result && (
          <p className="auth-info">
            {result}
          </p>
        )}
      </form>
    </div>
  );
}

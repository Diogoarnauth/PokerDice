import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authService } from "../../services/api/auth";
import { isOk } from "../../services/api/utils";
import { useAuthentication } from "../../providers/authentication";
import "../../styles/auth.css";

//TODO VER MELHOR O QUE ESSES PROVIDERS FAZEM
export default function Login() {
  // Router
  const navigate = useNavigate();

  // Authentication provider
  const [, setUsername] = useAuthentication();


  const [username, setUsernameInput] = useState("");
  const [password, setPasswordInput] = useState("");

  // UI State
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  // Messages
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    setResult(null);

    if (!username || !password) {
      setError("Please fill in all fields");
      return;
    }

    setLoading(true);

    const response = await authService.login({ username, password });


    if (isOk(response)) {

      setResult("Login successful! Redirecting...");

     if (response.value.token) {
        localStorage.setItem("token", response.value.token);
      }

      setUsername(username); // TODO() alterar isto depois pq queremos no token penso

      setTimeout(() => {
        navigate("/"); // redirecionar para Home
      }, 1000);
    } else {
      setError(response.error || "Unknown error");
    }

    setLoading(false);
  }

  return (
    <div className="auth-container">
      <h1 className="auth-title">Login</h1>

      <form onSubmit={handleSubmit}>
        <fieldset disabled={loading}>
          <div className="auth-form-group">

            {/* Username */}
            <div>
              <label htmlFor="username" className="auth-label">Username</label>
              <input
                className="auth-input"
                type="text"
                id="username"
                value={username}
                onChange={(e) => setUsernameInput(e.target.value)}
                placeholder="Enter your username"
                required
              />
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="auth-label">Password</label>
              <div className="auth-password-container">
                <input
                  className="auth-input"
                  type={showPassword ? "text" : "password"}
                  id="password"
                  value={password}
                  onChange={(e) => setPasswordInput(e.target.value)}
                  placeholder="Enter your password"
                  required
                />

                <button
                  type="button"
                  className="auth-toggle-password"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? "🙉" : "🙈"}
                </button>
              </div>
            </div>

            {/* Submit button */}
            <button type="submit" className="auth-submit">
              {loading ? "Loading..." : "Sign In"}
            </button>
          </div>
        </fieldset>

        {/* Error message */}
        {error && <div className="auth-error">{error}</div>}

        {/* Success message */}
        {result && <div className="auth-info">{result}</div>}

        {/* Signup link */}
        <div className="auth-links">
          <p className="auth-text">
            Don't have an account?{" "}
            <a href="/signup" className="auth-link">Sign Up</a>
          </p>
        </div>
      </form>
    </div>
  );
}

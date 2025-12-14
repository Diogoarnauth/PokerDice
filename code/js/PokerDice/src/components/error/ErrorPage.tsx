import React from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import {Problem} from "../../services/api/utils";
import '../../styles/ErrorPage.css';

export const ErrorPage = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // Lê o erro passado via navigate state
    const errorState = location.state?.error as Problem | undefined;

    const status = errorState?.status || 404;
    const title = errorState?.title || "Page not found";
    let detail = "The page you are looking for does not exist or an unexpected error occurred.";
    if (errorState?.detail) {
        // Se for string usa, se for objeto converte para string
        detail = typeof errorState.detail === 'string'
            ? errorState.detail
            : JSON.stringify(errorState.detail);
    }
    const isServerError = status >= 500;

    return (
        <div className="error-page-root">
            <div className="error-page-inner">
                <div className="error-page-status-wrapper">
                    <div
                        className={
                            "error-page-status-number" +
                            (isServerError ? " server-error" : "")
                        }
                    >
                        {status}
                    </div>
                    <div className="error-page-icon-overlay">
                        <svg
                            className={
                                "error-page-icon" + (isServerError ? " server-error" : "")
                            }
                            /* resto igual */
                        >
                            {/* paths */}
                        </svg>
                    </div>
                </div>

                <div className="error-page-card">
                    <h1 className="error-page-title">{title}</h1>
                    <p className="error-page-detail">{detail.toString()}</p>

                    <div className="error-page-actions">
                        <button
                            onClick={() => navigate("/lobbies")}
                            className={
                                "error-page-main-btn" + (isServerError ? " server-error" : "")
                            }
                        >
                            Voltar ao Início
                        </button>

                        {isServerError && (
                            <button
                                onClick={() => window.location.reload()}
                                className="error-page-secondary-btn"
                            >
                                Tentar Novamente
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ErrorPage;
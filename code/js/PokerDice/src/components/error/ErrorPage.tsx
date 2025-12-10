import React from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import {Problem} from "../../services/api/utils";

export const ErrorPage = () => {
    const navigate = useNavigate();
    const location = useLocation();

    // Lê o erro passado via navigate state
    const errorState = location.state?.error as Problem | undefined;

    const status = errorState?.status || 404;
    const title = errorState?.title || "Página não encontrada";
    let detail = "A página que procuras não existe ou ocorreu um erro inesperado.";
    if (errorState?.detail) {
        // Se for string usa, se for objeto converte para string
        detail = typeof errorState.detail === 'string'
            ? errorState.detail
            : JSON.stringify(errorState.detail);
    }
    const isServerError = status >= 500;

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <div className="max-w-md w-full text-center">

                <div className="mb-8 relative">
                    <div
                        className={`text-[120px] font-bold leading-none select-none ${isServerError ? 'text-red-100' : 'text-purple-100'}`}>
                        {status}
                    </div>
                    <div className="absolute inset-0 flex items-center justify-center pointer-events-none"
                         style={{marginTop: '-20px'}}>
                        <svg
                            className={`w-32 h-32 ${isServerError ? 'text-red-500' : 'text-purple-500'}`}
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            {isServerError ? (
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5"
                                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                            ) : (
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5"
                                      d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                            )}
                        </svg>
                    </div>
                </div>

                <div className="bg-white rounded-2xl shadow-lg p-8 space-y-6">
                    <div className="space-y-2">
                        <h1 className="text-2xl font-bold text-gray-800">{title}</h1>
                        <p className="text-gray-500 break-words">{detail.toString()}</p>
                    </div>

                    <div className="space-y-3">
                        <button
                            onClick={() => navigate('/lobbies')}
                            className={`w-full py-3 text-white rounded-lg font-medium transition-colors duration-200 
                                     ${isServerError ? 'bg-red-600 hover:bg-red-700' : 'bg-purple-600 hover:bg-purple-700'}`}
                        >
                            Voltar ao Início
                        </button>

                        {isServerError && (
                            <button
                                onClick={() => window.location.reload()}
                                className="w-full py-3 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200"
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
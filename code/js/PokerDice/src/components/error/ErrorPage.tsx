import React from 'react';
import { useNavigate } from 'react-router-dom';

export const ErrorPage = () => {
    const navigate = useNavigate();
    
    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
            <div className="max-w-md w-full text-center">

                <div className="mb-8 relative">
                    <div className="text-[150px] font-bold text-purple-100 select-none">
                        404
                    </div>
                    <div className="absolute inset-0 flex items-center justify-center">
                        <svg 
                            className="w-32 h-32 text-purple-500" 
                            fill="none" 
                            stroke="currentColor" 
                            viewBox="0 0 24 24"
                        >
                            <path 
                                strokeLinecap="round" 
                                strokeLinejoin="round" 
                                strokeWidth="1.5" 
                                d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" 
                            />
                        </svg>
                    </div>
                </div>


                <div className="bg-white rounded-2xl shadow-lg p-8 space-y-6">
                    <div className="space-y-2">
                        <h1 className="text-2xl font-bold text-gray-800">
                            Page not found
                        </h1>
                        <p className="text-gray-500">
                            The page you're looking for doesn't exist or has been moved.
                        </p>
                    </div>

                    <button
                        onClick={() => navigate('/channels')}
                        className="w-full py-3 bg-purple-600 text-white rounded-lg font-medium
                                 hover:bg-purple-700 transition-colors duration-200
                                 flex items-center justify-center gap-2"
                    >
                        <svg 
                            className="w-5 h-5" 
                            fill="none" 
                            stroke="currentColor" 
                            viewBox="0 0 24 24"
                        >
                            <path 
                                strokeLinecap="round" 
                                strokeLinejoin="round" 
                                strokeWidth="2" 
                                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" 
                            />
                        </svg>
                        Return Home
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ErrorPage;
import React, { createContext, useContext, useState, useCallback } from 'react';

type AlertType = 'success' | 'error' | 'info';

interface AlertState {
    isOpen: boolean;
    message: string;
    type: AlertType;
}

interface AlertContextType {
    showAlert: (message: string, type?: AlertType) => void;
    hideAlert: () => void;
}

const AlertContext = createContext<AlertContextType | undefined>(undefined);

export function AlertProvider({ children }: { children: React.ReactNode }) {
    const [alert, setAlert] = useState<AlertState>({
        isOpen: false,
        message: '',
        type: 'info',
    });

    const hideAlert = useCallback(() => {
        setAlert((prev) => ({ ...prev, isOpen: false }));
    }, []);

    const showAlert = useCallback((message: string, type: AlertType = 'info') => {
        setAlert({ isOpen: true, message, type });
        // Fecha automaticamente após 5 segundos
        setTimeout(() => {
            setAlert((prev) => ({ ...prev, isOpen: false }));
        }, 5000);
    }, []);

    return (
        <AlertContext.Provider value={{ showAlert, hideAlert }}>
            {children}
            {}
            {alert.isOpen && (
                <div className={`fixed top-5 right-5 z-50 px-6 py-4 rounded-lg shadow-xl border-l-4 animate-bounce-in
                    ${alert.type === 'error' ? 'bg-red-100 border-red-500 text-red-700' : ''}
                    ${alert.type === 'success' ? 'bg-green-100 border-green-500 text-green-700' : ''}
                    ${alert.type === 'info' ? 'bg-blue-100 border-blue-500 text-blue-700' : ''}
                `}>
                    <div className="flex items-center justify-between gap-4">
                        <span className="font-medium">{alert.message}</span>
                        <button onClick={hideAlert} className="font-bold hover:opacity-75">✕</button>
                    </div>
                </div>
            )}
        </AlertContext.Provider>
    );
}

export function useAlert() {
    const context = useContext(AlertContext);
    if (!context) throw new Error("useAlert must be used within AlertProvider");
    return context;
}
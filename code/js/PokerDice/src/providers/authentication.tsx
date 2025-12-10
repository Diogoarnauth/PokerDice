import React, {
    createContext,
    useState,
    useContext,
    useEffect,
    ReactNode,
} from "react";
import { fetchWrapper, isOk } from "../services/api/utils";
import { RequestUri } from "../services/api/RequestUri";

type UserState = string | null;

interface AuthenticationContextType {
    username: UserState;
    setUsername: (u: UserState) => void;
    isLoading: boolean;
}

const AuthenticationContext = createContext<AuthenticationContextType>({
    username: null,
    setUsername: () => {},
    isLoading: true,
});

export function AuthenticationProvider({ children }: { children: ReactNode }) {
    const [username, setUsername] = useState<UserState>(null);
    const [isLoading, setIsLoading] = useState(true);

    // restaurar a sessão ao carregar a pag
    useEffect(() => {
        async function checkSession() {
            try {
                // O browser envia a cookie automaticamente aqui c os dados do pedido
                const result = await fetchWrapper<any>(RequestUri.user.getMe); // Ajusta para o teu URI correto

                if (isOk(result)) {
                    setUsername(result.value.username);
                } else {
                    setUsername(null);
                }
            } catch (error) {
                setUsername(null);
            } finally {
                setIsLoading(false);
            }
        }

        checkSession();
    }, []);

    return (
        <AuthenticationContext.Provider value={{ username, setUsername, isLoading }}>
            {children}
        </AuthenticationContext.Provider>
    );
}

export function useAuthentication() {
    return useContext(AuthenticationContext);
}
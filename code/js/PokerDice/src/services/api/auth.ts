import { RequestUri } from "./RequestUri";
import { fetchWrapper, Result} from './utils';

interface LoginCredentials {
    username: string;
    password: string;
}

export const authService = {
    login(credentials: LoginCredentials): Promise<Result<any>> {
        return fetchWrapper(RequestUri.user.login, {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },

    signup(credentials: LoginCredentials): Promise<Result<any>> {
        return fetchWrapper(RequestUri.user.register, {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },
};
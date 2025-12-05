import { RequestUri } from "./RequestUri";
import { fetchWrapper, Result} from './utils';

interface LoginCredentials {
    username: string;
    password: string;
}
interface SignupCredentials {
    username: string;
    name: string;
    age: number;
    password: string;
    inviteCode: string;
    }

interface SignupAdminCredentials {
        username: string;
        name: string;
        age: number;
        password: string;
    }


export const authService = {
    login(credentials: LoginCredentials): Promise<Result<any>> {
        return fetchWrapper(RequestUri.user.login, {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },

    checkAdmin(): Promise<Result<any>> {
        return fetchWrapper(RequestUri.user.checkAdmin, {
            method: 'GET'
        });
    },

    signupAdmin(credentials: SignupAdminCredentials): Promise<Result<any>> {
        return fetchWrapper(RequestUri.user.bootstrap, {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },


    signup(credentials: SignupCredentials): Promise<Result<any>> {
        return fetchWrapper(RequestUri.user.register, {
            method: 'POST',
            body: JSON.stringify(credentials)
        });
    },
};
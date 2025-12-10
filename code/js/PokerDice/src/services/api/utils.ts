export interface Problem {
    type: string;
    title: string;
    status?: number;
    detail?: string;
}

export type Result<T> =
    | { success: true; value: T }
    | { success: false; error: Problem };

export const isOk = <T>(result: Result<T>): result is { success: true; value: T } => result.success;

export function getTokenFromCookies(): string | null {
    const token = document.cookie.split('; ').find(row => row.startsWith('token='));
    return token ? token.split('=')[1] : null;
}

export async function fetchWrapper<T>(
    url: string,
    options: RequestInit = {}
): Promise<Result<T>> {
    try {
        // Tenta obter token dos cookies OU do localStorage (para compatibilidade)
        const token = getTokenFromCookies() // TODO("CONFIRMAR ISTO")

        const headers: HeadersInit = {
            ...(token ? {Authorization: `Bearer ${token}`} : {}),
            ...options.headers,
        };

        if (options.body) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(url, {...options, headers, credentials: 'include'});

        if (!response.ok) {
            const errorJson = await response.json().catch(() => ({}));

            // Normaliza o erro para a interface Problem
            const problem: Problem = {
                type: errorJson.type || 'about:blank',
                title: errorJson.title || errorJson.message || 'Erro desconhecido',
                status: response.status,
                detail: errorJson.detail || errorJson.message
            };

            return {success: false, error: problem};
        }

        if (response.status === 204) {
            return {success: true, value: undefined as T};
        }

        const data = await response.json();
        return {success: true, value: data as T};

    } catch (error: any) {
        return {
            success: false,
            error: {
                type: 'network-error',
                title: "Erro de Conexão",
                detail: error.message || "Não foi possível contactar o servidor.",
                status: 503
            }
        };
    }
}
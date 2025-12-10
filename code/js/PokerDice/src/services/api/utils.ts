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

export async function fetchWrapper<T>(
    url: string,
    options: RequestInit = {}
): Promise<Result<T>> {
    try {
        const headers: HeadersInit = {
            ...options.headers,
        };


        if (options.body && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }
        const response = await fetch(url, {
            ...options,
            headers,
            credentials: 'include'
        });

        if (!response.ok) {
            const errorJson = await response.json().catch(() => ({}));
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
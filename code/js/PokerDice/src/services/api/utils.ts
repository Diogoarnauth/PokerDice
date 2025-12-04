export type Result<T> =
| { success: true; value: T }
| { success: false; error: string };

export const isOk = <T>(result: Result<T>): result is { success: true; value: T } => result.success;
export async function fetchWrapper<T>(
  url: string,
  options: RequestInit = {}
): Promise<Result<T>> {
  try {
    const token = localStorage.getItem('token');
    //console.log('fetchWrapper', document.cookie);

    const headers: HeadersInit = {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    };

    if (options.body) {
      headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      return { success: false, error: errorData?.message || 'Request failed' };
    }

    if (response.status === 204) {
      return { success: true, value: undefined as T };
    }

    const data = await response.json();
    return { success: true, value: data as T };
  } catch (error: any) {
    return { success: false, error: error.message };
  }
}

type MessagePayload = { message: string };

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

const isMessagePayload = (value: unknown): value is MessagePayload => {
  if (typeof value !== 'object' || value === null) {
    return false;
  }

  return 'message' in value && typeof (value as MessagePayload).message === 'string';
};

const resolveErrorMessage = (payload: unknown, status: number, statusText: string): string => {
  if (isMessagePayload(payload)) {
    return payload.message;
  }

  if (typeof payload === 'string' && payload.trim().length > 0) {
    return payload;
  }

  if (statusText.trim().length > 0) {
    return statusText;
  }

  return `Request failed with status ${status}`;
};

export class ApiError extends Error {
  status: number;

  payload: unknown;

  constructor(status: number, message: string, payload: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.payload = payload;
  }
}

export const apiRequest = async <T>(path: string, init?: RequestInit): Promise<T> => {
  const headers = new Headers(init?.headers);

  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
    credentials: 'include',
  });

  const rawResponse = await response.text();
  let payload: unknown = null;

  if (rawResponse.trim().length > 0) {
    try {
      payload = JSON.parse(rawResponse) as unknown;
    } catch {
      payload = rawResponse;
    }
  }

  if (!response.ok) {
    const message = resolveErrorMessage(payload, response.status, response.statusText);
    throw new ApiError(response.status, message, payload);
  }

  return payload as T;
};

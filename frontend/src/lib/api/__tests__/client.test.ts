import { apiRequest } from '@/lib/api/client';
import { tokenStorage } from '@/lib/storage/token-storage';

jest.mock('@/lib/storage/token-storage', () => ({
  tokenStorage: {
    get: jest.fn(),
    set: jest.fn(),
    clear: jest.fn(),
  },
}));

const mockedStorage = tokenStorage as jest.Mocked<typeof tokenStorage>;

function response(status: number, body: unknown = null) {
  return {
    status,
    ok: status >= 200 && status < 300,
    json: jest.fn().mockResolvedValue(body),
  } as unknown as Response;
}

describe('apiRequest refresh coordination', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedStorage.get.mockResolvedValue({ accessToken: 'old-access', refreshToken: 'refresh-one' });
    mockedStorage.set.mockResolvedValue();
    mockedStorage.clear.mockResolvedValue();
  });

  it('uses one refresh request for concurrent 401 responses', async () => {
    const fetchMock = jest.fn(async (input: string | URL | Request, init?: RequestInit) => {
      const url = String(input);
      if (url.endsWith('/api/v1/auth/refresh')) {
        return response(200, { accessToken: 'new-access', refreshToken: 'refresh-two' });
      }

      const authorization = new Headers(init?.headers).get('Authorization');
      return authorization === 'Bearer new-access'
        ? response(200, { value: 'ok' })
        : response(401, { message: 'expired' });
    });
    globalThis.fetch = fetchMock as typeof fetch;

    const [first, second] = await Promise.all([
      apiRequest<{ value: string }>('/api/v1/example'),
      apiRequest<{ value: string }>('/api/v1/example'),
    ]);

    expect(first.value).toBe('ok');
    expect(second.value).toBe('ok');
    expect(fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/api/v1/auth/refresh'))).toHaveLength(1);
    expect(mockedStorage.set).toHaveBeenCalledWith({
      accessToken: 'new-access',
      refreshToken: 'refresh-two',
    });
  });

  it('retries with a newer stored token instead of replaying an old refresh token', async () => {
    mockedStorage.get
      .mockResolvedValueOnce({ accessToken: 'old-access', refreshToken: 'refresh-one' })
      .mockResolvedValueOnce({ accessToken: 'new-access', refreshToken: 'refresh-two' });

    const fetchMock = jest.fn(async (_input: string | URL | Request, init?: RequestInit) => {
      const authorization = new Headers(init?.headers).get('Authorization');
      return authorization === 'Bearer new-access'
        ? response(200, { value: 'ok' })
        : response(401, { message: 'expired' });
    });
    globalThis.fetch = fetchMock as typeof fetch;

    await expect(apiRequest<{ value: string }>('/api/v1/example')).resolves.toEqual({ value: 'ok' });
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(mockedStorage.set).not.toHaveBeenCalled();
  });
});

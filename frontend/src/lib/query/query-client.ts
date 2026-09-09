type CacheEntry = {
  value?: unknown;
  expiresAt: number;
  inFlight?: Promise<unknown>;
};

class QueryClient {
  private cache = new Map<string, CacheEntry>();

  async fetchQuery<T>(key: string, fetcher: () => Promise<T>, staleTimeMs = 30_000): Promise<T> {
    const now = Date.now();
    const current = this.cache.get(key);
    if (current?.value !== undefined && current.expiresAt > now) return current.value as T;
    if (current?.inFlight) return current.inFlight as Promise<T>;

    const inFlight = fetcher()
      .then(value => {
        this.cache.set(key, { value, expiresAt: Date.now() + staleTimeMs });
        return value;
      })
      .catch(error => {
        if (current?.value !== undefined) this.cache.set(key, current);
        else this.cache.delete(key);
        throw error;
      });
    this.cache.set(key, { ...current, expiresAt: current?.expiresAt ?? 0, inFlight });
    return inFlight;
  }

  invalidate(prefix?: string) {
    if (!prefix) return this.cache.clear();
    for (const key of this.cache.keys()) if (key.startsWith(prefix)) this.cache.delete(key);
  }

  clear() { this.cache.clear(); }
}

export const queryClient = new QueryClient();

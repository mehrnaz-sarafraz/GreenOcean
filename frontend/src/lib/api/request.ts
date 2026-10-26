export async function fetchWithTimeout(url: string, init: RequestInit = {}): Promise<Response> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), 20000);
  try {
    return await fetch(url, { ...init, signal: init.signal ?? controller.signal });
  } catch (error) {
    if (error instanceof Error && (error.name === 'AbortError' || error.name === 'TimeoutError')) {
      throw new Error('The request timed out. Your changes were not confirmed; please retry.');
    }
    throw new Error('Could not connect. Check your connection and try again.');
  } finally {
    clearTimeout(timer);
  }
}
export function singleFlight<Args extends unknown[],T>(operation: (...args: Args) => Promise<T>): (...args: Args) => Promise<T> {
  let pending: Promise<T> | null = null;
  return (...args: Args) => {
    if (!pending) pending = operation(...args).finally(() => { pending = null; });
    return pending;
  };
}

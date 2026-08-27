import { cookies } from "next/headers";

const BACKEND_API_URL = process.env.BACKEND_API_URL ?? "http://localhost:8080";

function buildBackendUrl(path: string): string {
  return `${BACKEND_API_URL}${path}`;
}

export async function backendFetch(
  path: string,
  init: RequestInit = {},
  includeAuth = false
): Promise<Response> {
  const headers = new Headers(init.headers ?? {});

  if (includeAuth) {
    const token = (await cookies()).get("access_token")?.value;
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  return fetch(buildBackendUrl(path), {
    ...init,
    headers,
    cache: "no-store"
  });
}

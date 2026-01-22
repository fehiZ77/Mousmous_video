import api from "@/lib/axios";
import {
  decodeToken,
  getTokenPayload,
  getToken as getTokenUtil,
  getUserId as getUserIdUtil,
  getUserName as getUserNameUtil,
  getRole as getRoleUtil,
  isAuthenticated as isAuthenticatedUtil,
} from "@/lib/utils/jwt.utils";
import type {
  LoginUserDto,
  AuthResponse,
  UserDto,
  RegisterUserDto,
  ChangeMdpDto,
} from "@/lib/types/auth";

export const authService = {
  async login(credentials: LoginUserDto): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>("/auth/login", credentials);
    return response.data;
  },

  async register(userData: RegisterUserDto): Promise<any> {
    const response = await api.post("/auth/register", userData);
    return response.data;
  },

  async changePassword(changeMdpData: ChangeMdpDto): Promise<any> {
    const response = await api.post("/auth/change", changeMdpData);
    return response.data;
  },

  async users(): Promise<UserDto[]> {
    const response = await api.get<UserDto[]>("/auth/users", {});
    return response.data;
  },

  async getOtherUsers(): Promise<UserDto[]> {
    const userId = getUserIdUtil();
    if (!userId) {
      throw new Error("User ID not found in token");
    }
    const response = await api.get<UserDto[]>("/auth/others", {
      params: { userId },
    });
    return response.data;
  },

  logout(): void {
    localStorage.removeItem("token");
  },

  getToken(): string | null {
    return getTokenUtil();
  },

  getUserName(): string | null {
    return getUserNameUtil();
  },

  getUserId(): string | null {
    return getUserIdUtil();
  },

  getRole(): string | null {
    return getRoleUtil();
  },

  getFirstLogin(): boolean {
    const payload = getTokenPayload();
    return payload?.firstLogin === true;
  },

  isAuthenticated(): boolean {
    return isAuthenticatedUtil();
  },

  saveAuthData(authResponse: AuthResponse): void {
    // On stocke seulement le token, les autres infos seront lues depuis le JWT
    localStorage.setItem("token", authResponse.token);
  },
};

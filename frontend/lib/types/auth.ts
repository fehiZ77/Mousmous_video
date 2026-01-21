export interface LoginUserDto {
  userName?: string;
  mdp: string;
}

export interface AuthResponse {
  token: string;
  userName: string;
  role: string;
  firstLogin?: boolean;
}

export interface JwtPayload {
  sub?: string; // Subject (username)
  userId?: string | number; // User ID
  userName?: string;
  role?: string;
  firstLogin?: boolean;
  exp?: number; // Expiration timestamp
  iat?: number; // Issued at timestamp
  [key: string]: any; // Pour les autres claims
}

export interface ChangeMdpDto {
  username: string;
  oldPassword: string;
  newPassword: string;
}

export interface UserDto {
  userId: number; // ID de l'utilisateur (peut être présent dans certaines réponses)
  userName: string;
  email: string;
  role: string;
}

export interface RegisterUserDto {
  userName: string;
  email: string;
  mdp: string;
  role: number; // 1 pour USER, 10 pour ADMIN
}

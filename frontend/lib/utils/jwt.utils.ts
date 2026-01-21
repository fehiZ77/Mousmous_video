import { jwtDecode } from "jwt-decode";
import type { JwtPayload } from "@/lib/types/auth";

// Fonction utilitaire pour décoder le token JWT
export const decodeToken = (token: string): JwtPayload | null => {
  try {
    return jwtDecode<JwtPayload>(token);
  } catch (error) {
    console.error("Erreur lors du décodage du token:", error);
    return null;
  }
};

// Fonction pour obtenir le payload du token actuel
export const getTokenPayload = (): JwtPayload | null => {
  const token = localStorage.getItem("token");
  if (!token) return null;
  return decodeToken(token);
};

// Fonction pour obtenir le token depuis localStorage
export const getToken = (): string | null => {
  return localStorage.getItem("token");
};

// Fonction pour obtenir l'userId depuis le token
export const getUserId = (): string | null => {
  const payload = getTokenPayload();
  return payload?.userId?.toString() || payload?.sub || null;
};

// Fonction pour obtenir le userName depuis le token
export const getUserName = (): string | null => {
  const payload = getTokenPayload();
  return payload?.userName || payload?.sub || null;
};

// Fonction pour obtenir le rôle depuis le token
export const getRole = (): string | null => {
  const payload = getTokenPayload();
  return payload?.role || null;
};

// Fonction pour vérifier si l'utilisateur est authentifié
export const isAuthenticated = (): boolean => {
  const token = getToken();
  if (!token) return false;

  // Vérifier si le token est expiré
  const payload = decodeToken(token);
  if (!payload || !payload.exp) return true; // Si pas d'expiration, considérer comme valide

  const currentTime = Date.now() / 1000;
  return payload.exp > currentTime;
};

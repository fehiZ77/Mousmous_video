import api from "@/lib/axios";
import { getUserId } from "@/lib/utils/jwt.utils";
import type { UserKeys, GenerateKeyDto, RevokeKeyRequestDto } from "@/lib/types/key";

export const keyService = {
  /**
   * Récupère la liste des clés d'un utilisateur
   * @param userId - ID de l'utilisateur (optionnel, sera extrait du token si non fourni)
   */
  async listKeys(userId?: number): Promise<UserKeys[]> {
    const targetUserId = userId || getUserId();
    if (!targetUserId) {
      throw new Error("User ID not found in token");
    }
    const response = await api.get<UserKeys[]>("/keys/getkeys", {
      params: { userId: targetUserId },
    });
    return response.data;
  },

  /**
   * Génère une nouvelle paire de clés pour l'utilisateur connecté
   * @param keyData - Données pour générer la clé (keyName et validity)
   */
  async generateKeyPair(keyData: GenerateKeyDto): Promise<string> {
    const userId = getUserId();
    if (!userId) {
      throw new Error("User ID not found in token");
    }

    const response = await api.post<string>("/keys/generate", {
      ...keyData,
      userId: Number(userId),
    });
    return response.data;
  },

  /**
   * Révoque une clé pour l'utilisateur connecté
   * @param keyId - ID de la clé à révoquer
   */
  async revokeKey(keyId: number): Promise<void> {
    const userId = getUserId();
    if (!userId) {
      throw new Error("User ID not found in token");
    }

    const revokeData: RevokeKeyRequestDto = {
      keyId,
      userId: Number(userId),
    };

    await api.post("/keys/revokekey", revokeData);
  },

  /**
   * Récupère la liste des clés valides (ACTIVE) d'un utilisateur
   * @param userId - ID de l'utilisateur (optionnel, sera extrait du token si non fourni)
   */
  async listValidKeys(userId?: number): Promise<UserKeys[]> {
    const targetUserId = userId || getUserId();
    if (!targetUserId) {
      throw new Error("User ID not found in token");
    }
    const response = await api.get<UserKeys[]>("/keys/getvalidekeys", {
      params: { userId: targetUserId },
    });
    return response.data;
  },
};

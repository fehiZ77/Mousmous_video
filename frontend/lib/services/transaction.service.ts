import api from "@/lib/axios";
import { getUserId } from "@/lib/utils/jwt.utils";
import type { TransactionRequestDto, TransactionResponseDto } from "@/lib/types/transaction";

export const transactionService = {
  /**
   * Crée une nouvelle transaction
   * @param request - Données de la transaction (sans owner_id qui sera extrait du token)
   */
  async createTransaction(
    recipientId: number,
    amount: number,
    validity: number,
    keyId: number,
    publicKey: string,
    videoFile: File
  ): Promise<string> {
    const ownerId = getUserId();
    if (!ownerId) {
      throw new Error("User ID not found in token");
    }

    // Créer FormData pour l'upload multipart
    const formData = new FormData();
    formData.append("owner_id", ownerId.toString());
    formData.append("recipient_id", recipientId.toString());
    formData.append("amount", amount.toString());
    formData.append("validity", validity.toString());
    formData.append("keyId", keyId.toString());
    formData.append("publicKey", publicKey);
    formData.append("video", videoFile);

    // axios définira automatiquement le Content-Type avec la boundary pour FormData
    const response = await api.post<string>("/transactions/create", formData);

    return response.data;
  },

  /**
   * Récupère les transactions créées par l'utilisateur
   */
  async getCreatedTransactions(): Promise<TransactionResponseDto[]> {
    const userId = getUserId();
    if (!userId) {
      throw new Error("User ID not found in token");
    }

    const response = await api.get<TransactionResponseDto[]>("/transactions/all", {
      params: {
        isOwner: true,
        userId: userId,
      },
    });

    return response.data;
  },

  /**
   * Récupère les transactions à vérifier par l'utilisateur
   */
  async getTransactionsToVerify(): Promise<TransactionResponseDto[]> {
    const userId = getUserId();
    if (!userId) {
      throw new Error("User ID not found in token");
    }

    const response = await api.get<TransactionResponseDto[]>("/transactions/all", {
      params: {
        isOwner: false,
        userId: userId,
      },
    });

    return response.data;
  },

  /**
   * Récupère le stream vidéo en tant que Blob
   */
  async getVideoStream(objectName: string): Promise<Blob> {
    const response = await api.get(`/transactions/videos/${objectName}`, {
      responseType: "blob",
    });

    return response.data;
  },

  /**
   * Vérifie une transaction avec la clé publique de l'expéditeur
   */
  async verifyTransaction(
    transactionId: number,
    publicKey: string
  ): Promise<boolean> {
    const response = await api.post<boolean>("/transactions/verify", {
      transactionId,
      publicKey,
    });

    return response.data;
  },
};

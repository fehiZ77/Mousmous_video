export enum KeyStatus {
  ACTIVE = "ACTIVE",
  REVOKED = "REVOKED",
  EXPIRED = "EXPIRED",
}

export interface UserKeys {
  id: number;
  userId: number;
  keyName: string;
  publicKey: string;
  status: KeyStatus;
  expiredAt: string | null; // LocalDateTime en string ISO
}

export interface GenerateKeyDto {
  keyName: string;
  validity: number; // Durée de validité en mois (1, 3, 6, 12)
  // userId sera extrait du token, pas besoin de l'inclure dans le DTO
}

export interface RevokeKeyRequestDto {
  keyId: number;
  userId: number;
}

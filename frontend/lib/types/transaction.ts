export interface TransactionRequestDto {
  owner_id: number;
  recipient_id: number;
  amount: number;
  validity: number; // Durée de validité en mois
  keyId: number;
  publicKey: string;
  video: File; // MultipartFile sera envoyé via FormData
}

export interface TransactionResponseDto {
  transactionId: number;
  date: string;
  userName: string;
  amount: number;
  objectName: string;
  status: "PENDING" | "VERIFIED";
  publicKey: string;
}

import api from "../axios";

export interface Notification {
  id: number;
  triggerId: number;
  receivedId: number;
  action: "TRANSACTION_CREATED" | "TRANSACTION_VERIFIED_OK" | "TRANSACTION_VERIFIED_NOK";
  transactionId: number;
  dateSeenAt: string | null;
}

const getNotificationsForUser = async (userId: number): Promise<Notification[]> => {
  // const response = await api.get(`/notifications/user/${userId}`);
  // return response.data;
  return [];
};

const createNotification = async (notification: Omit<Notification, "id" | "dateSeenAt" | "dateCreated">): Promise<Notification> => {
  // const response = await api.post("/notifications/create", notification);
  // return response.data;
  return { ...notification, id: 0, dateSeenAt: null};
};

const markAsSeen = async (id: number): Promise<Notification> => {
  // const response = await api.put(`/notifications/${id}/seen`);
  // return response.data;
  return { id, triggerId: 0, receivedId: 0, action: "TRANSACTION_CREATED", transactionId: 0, dateSeenAt: new Date().toISOString() };
};

const getAllNotificationsForUser = async (userId: number): Promise<Notification[]> => {
  // const response = await api.get(`/notifications/user/${userId}/all`);
  // return response.data;
  return [];
};

const markAllAsSeen = async (userId: number): Promise<void> => {
  await api.put(`/notifications/user/${userId}/mark-all-seen`);
};

export const NotificationService = {
  getNotificationsForUser,
  getAllNotificationsForUser,
  createNotification,
  markAsSeen,
  markAllAsSeen,
};

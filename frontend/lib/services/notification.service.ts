import api from "../axios";

export interface Notification {
  detail: string;
  timePassed: string;
}

const getNotificationsForUser = async (userId: number): Promise<Notification[]> => {
  try {
    const response = await api.get(`/notifications/not-seen?userId=${userId}`);
    return response.data || [];
  } catch (error) {
    console.error("Erreur lors de la récupération des notifications:", error);
    return [];
  }
};

const createNotification = async (notification: Notification): Promise<Notification> => {
  // const response = await api.post("/notifications/create", notification);
  // return response.data;
  return notification;
};

const markAsSeen = async (id: number): Promise<void> => {
  // const response = await api.put(`/notifications/${id}/seen`);
  // return response.data;
};

const getAllNotificationsForUser = async (userId: number): Promise<Notification[]> => {
  try {
    const response = await api.get(`/notifications/all?userId=${userId}`);
    return response.data || [];
  } catch (error) {
    console.error("Erreur lors de la récupération de toutes les notifications:", error);
    return [];
  }
};

const markAllAsSeen = async (userId: number): Promise<void> => {
  try {
    await api.put(`/notifications/mark-all-seen?userId=${userId}`);
  } catch (error) {
    console.error("Erreur lors du marquage de toutes les notifications:", error);
    throw error;
  }
};

export const NotificationService = {
  getNotificationsForUser,
  getAllNotificationsForUser,
  createNotification,
  markAsSeen,
  markAllAsSeen,
};

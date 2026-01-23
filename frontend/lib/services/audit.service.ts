import api from "@/lib/axios";

export const auditService = {
  async listLogs(): Promise<string[]> {
    const response = await api.get<string[]>("/audit/logs");
    return response.data;
  },

  async download(fileName: string): Promise<Blob> {
    const response = await api.get(`/audit/download`, {
      params: { fileToVerify: fileName },
      responseType: "blob",
    });
    return response.data;
  },

  /**
   * Verify a log file on the backend.
   * Returns an object with { status, data } so the UI can show messages
   * even when the server returns 4xx/5xx.
   */
  async verify(
    fileName: string,
  ): Promise<{ status: number; data: string | Record<string, any> }>
  {
    try {
      const response = await api.get(`/audit/verify`, {
        params: { fileToVerify: fileName },
        // let axios throw on non-2xx so we catch below and return status/data
      });
      return { status: response.status, data: response.data };
    } catch (error: any) {
      if (error?.response) {
        return { status: error.response.status, data: error.response.data };
      }
      throw error;
    }
  },
};

export default auditService;

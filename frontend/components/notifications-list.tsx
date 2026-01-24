"use client"
import { useState, useEffect } from "react"
import { Bell, CheckCircle2, CheckCheck } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, AlertCircle } from "lucide-react"
import { NotificationService, type Notification } from "@/lib/services/notification.service"
import { authService } from "@/lib/services/auth.service"
import { cn } from "@/lib/utils"

export function NotificationsList() {
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [userId, setUserId] = useState<string | null>(null)

  useEffect(() => {
    const userIdStr = authService.getUserId()
    if (!userIdStr) {
      setError("Utilisateur non authentifié")
      setIsLoading(false)
      return
    }
    setUserId(userIdStr)
  }, [])

  useEffect(() => {
    if (!userId) return

    const fetchNotifications = async () => {
      try {
        setIsLoading(true)
        setError(null)
        const notificationsData = await NotificationService.getAllNotificationsForUser(Number(userId))
        setNotifications(notificationsData)
      } catch (err: any) {
        const errorMessage = err.response?.data || err.message || "Erreur lors du chargement des notifications"
        setError(typeof errorMessage === "string" ? errorMessage : "Erreur de chargement")
      } finally {
        setIsLoading(false)
      }
    }

    fetchNotifications()
  }, [userId])

  const handleMarkAllAsSeen = async () => {
    if (!userId) return
    try {
      await NotificationService.markAllAsSeen(Number(userId))
      // Recharger les notifications après avoir marqué tout comme lu
      const notificationsData = await NotificationService.getAllNotificationsForUser(Number(userId))
      setNotifications(notificationsData)
    } catch (err) {
      console.error("Erreur lors du marquage de toutes les notifications:", err)
    }
  }

  if (isLoading) {
    return (
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle className="text-lg">Notifications</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-center py-8">
            <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
          </div>
        </CardContent>
      </Card>
    )
  }

  if (error) {
    return (
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle className="text-lg">Notifications</CardTitle>
        </CardHeader>
        <CardContent>
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="bg-card border-border">
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <CardTitle className="text-lg font-semibold text-foreground">
          Notifications ({notifications.length})
        </CardTitle>
        {notifications.length > 0 && (
          <Button variant="outline" size="sm" onClick={handleMarkAllAsSeen}>
            <CheckCheck className="mr-2 h-4 w-4" />
            Tout marquer comme lu
          </Button>
        )}
      </CardHeader>
      <CardContent>
        {notifications.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Bell className="w-12 h-12 mx-auto mb-4 opacity-50" />
            <p>Aucune notification</p>
          </div>
        ) : (
          <div className="space-y-3">
            {notifications.map((notification, index) => (
              <div
                key={index}
                className="flex items-start gap-4 p-4 rounded-lg border bg-card border-border hover:bg-sidebar-accent/30 transition-colors"
              >
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-foreground mb-1">{notification.detail}</p>
                  <p className="text-xs text-muted-foreground">{notification.timePassed}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

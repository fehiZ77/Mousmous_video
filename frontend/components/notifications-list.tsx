"use client"
import { useState, useEffect } from "react"
import { Bell, CheckCircle2, XCircle, Clock, CheckCheck } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Loader2, AlertCircle } from "lucide-react"
import { NotificationService, type Notification } from "@/lib/services/notification.service"
import { authService } from "@/lib/services/auth.service"
import { cn } from "@/lib/utils"

const actionLabels: Record<string, string> = {
  TRANSACTION_CREATED: "Transaction créée",
  TRANSACTION_VERIFIED_OK: "Transaction vérifiée (OK)",
  TRANSACTION_VERIFIED_NOK: "Transaction vérifiée (NOK)",
}

const actionIcons: Record<string, typeof CheckCircle2> = {
  TRANSACTION_CREATED: Clock,
  TRANSACTION_VERIFIED_OK: CheckCircle2,
  TRANSACTION_VERIFIED_NOK: XCircle,
}

const actionColors: Record<string, string> = {
  TRANSACTION_CREATED: "bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/20",
  TRANSACTION_VERIFIED_OK: "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20",
  TRANSACTION_VERIFIED_NOK: "bg-red-500/10 text-red-600 dark:text-red-400 border-red-500/20",
}

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
        // Récupérer toutes les notifications (non lues et lues)
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

  const handleMarkAsSeen = async (id: number) => {
    try {
      await NotificationService.markAsSeen(id)
      setNotifications((prev) =>
        prev.map((notif) => (notif.id === id ? { ...notif, dateSeenAt: new Date().toISOString() } : notif))
      )
    } catch (err) {
      console.error("Erreur lors du marquage de la notification:", err)
    }
  }

  const handleMarkAllAsSeen = async () => {
    if (!userId) return
    try {
      await NotificationService.markAllAsSeen(Number(userId))
      setNotifications((prev) =>
        prev.map((notif) => ({ ...notif, dateSeenAt: notif.dateSeenAt || new Date().toISOString() }))
      )
    } catch (err) {
      console.error("Erreur lors du marquage de toutes les notifications:", err)
    }
  }

  const unreadCount = notifications.filter((n) => !n.dateSeenAt).length

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
          Notifications {unreadCount > 0 && <span className="text-muted-foreground">({unreadCount} non lues)</span>}
        </CardTitle>
        {unreadCount > 0 && (
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
            {notifications.map((notification) => {
              const ActionIcon = actionIcons[notification.action] || Bell
              const isUnread = !notification.dateSeenAt

              return (
                <div
                  key={notification.id}
                  className={cn(
                    "flex items-start gap-4 p-4 rounded-lg border transition-colors",
                    isUnread
                      ? "bg-sidebar-accent/50 border-sidebar-border"
                      : "bg-card border-border hover:bg-sidebar-accent/30"
                  )}
                >
                  <div
                    className={cn(
                      "flex items-center justify-center w-10 h-10 rounded-lg",
                      actionColors[notification.action] || "bg-muted"
                    )}
                  >
                    <ActionIcon className="w-5 h-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <Badge
                            variant="outline"
                            className={cn(
                              "text-xs",
                              actionColors[notification.action] || "bg-muted"
                            )}
                          >
                            {actionLabels[notification.action] || notification.action}
                          </Badge>
                          {isUnread && (
                            <div className="w-2 h-2 rounded-full bg-emerald-500" />
                          )}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          Transaction ID: {notification.transactionId}
                        </p>
                        {notification.dateSeenAt && (
                          <p className="text-xs text-muted-foreground mt-1">
                            Lu le {new Date(notification.dateSeenAt).toLocaleString("fr-FR")}
                          </p>
                        )}
                      </div>
                      {isUnread && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleMarkAsSeen(notification.id)}
                          className="shrink-0"
                        >
                          <CheckCircle2 className="w-4 h-4" />
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { Loader2, CheckCircle2, AlertCircle, Bell, Send } from "lucide-react"
import { NotificationService, type Notification } from "@/lib/services/notification.service"
import { authService } from "@/lib/services/auth.service"

export function NotificationTest() {
  const [userId, setUserId] = useState<string | null>(null)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  
  // Formulaire pour créer une notification
  const [formData, setFormData] = useState({
    triggerId: "",
    receivedId: "",
    action: "TRANSACTION_CREATED" as Notification["action"],
    transactionId: "",
  })

  useEffect(() => {
    const currentUserId = authService.getUserId()
    if (currentUserId) {
      setUserId(currentUserId)
      setFormData((prev) => ({ ...prev, receivedId: currentUserId }))
      loadNotifications()
    }
  }, [])

  const loadNotifications = async () => {
    const currentUserId = authService.getUserId()
    if (!currentUserId) {
      setError("Utilisateur non authentifié")
      return
    }

    try {
      setIsLoading(true)
      setError(null)
      const data = await NotificationService.getAllNotificationsForUser(Number(currentUserId))
      setNotifications(data)
    } catch (err: any) {
      setError("Erreur lors du chargement: " + (err.message || "Erreur inconnue"))
    } finally {
      setIsLoading(false)
    }
  }

  const createNotification = async () => {
    if (!formData.triggerId || !formData.receivedId || !formData.transactionId) {
      setError("Veuillez remplir tous les champs")
      return
    }

    try {
      setIsLoading(true)
      setError(null)
      setSuccess(null)

      await NotificationService.createNotification({
        triggerId: Number(formData.triggerId),
        receivedId: Number(formData.receivedId),
        action: formData.action,
        transactionId: Number(formData.transactionId),
      })

      setSuccess("Notification créée avec succès!")
      await loadNotifications()
      
      // Réinitialiser le formulaire
      setFormData({
        triggerId: "",
        receivedId: userId || "",
        action: "TRANSACTION_CREATED",
        transactionId: "",
      })
    } catch (err: any) {
      setError("Erreur lors de la création: " + (err.message || "Erreur inconnue"))
    } finally {
      setIsLoading(false)
    }
  }

  const markAsSeen = async (id: number) => {
    try {
      await NotificationService.markAsSeen(id)
      await loadNotifications()
    } catch (err: any) {
      setError("Erreur lors du marquage: " + (err.message || "Erreur inconnue"))
    }
  }

  const markAllAsSeen = async () => {
    if (!userId) return
    try {
      await NotificationService.markAllAsSeen(Number(userId))
      await loadNotifications()
      setSuccess("Toutes les notifications ont été marquées comme lues")
    } catch (err: any) {
      setError("Erreur lors du marquage: " + (err.message || "Erreur inconnue"))
    }
  }

  const unreadCount = notifications.filter((n) => !n.dateSeenAt).length

  const actionLabels: Record<string, string> = {
    TRANSACTION_CREATED: "Transaction créée",
    TRANSACTION_VERIFIED_OK: "Transaction vérifiée (OK)",
    TRANSACTION_VERIFIED_NOK: "Transaction vérifiée (NOK)",
  }

  return (
    <div className="space-y-6">
      {/* Formulaire de création */}
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Bell className="w-5 h-5" />
            Créer une notification de test
          </CardTitle>
          <CardDescription>
            Créez une notification pour tester le système. L'ID utilisateur actuel est pré-rempli.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {success && (
            <Alert className="bg-emerald-500/10 border-emerald-500/20">
              <CheckCircle2 className="h-4 w-4 text-emerald-500" />
              <AlertDescription className="text-emerald-600 dark:text-emerald-400">
                {success}
              </AlertDescription>
            </Alert>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="triggerId">ID Déclencheur (Trigger ID)</Label>
              <Input
                id="triggerId"
                type="number"
                placeholder="1"
                value={formData.triggerId}
                onChange={(e) => setFormData({ ...formData, triggerId: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="receivedId">ID Destinataire (Received ID)</Label>
              <Input
                id="receivedId"
                type="number"
                placeholder={userId || "1"}
                value={formData.receivedId}
                onChange={(e) => setFormData({ ...formData, receivedId: e.target.value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="action">Type d'action</Label>
              <Select
                value={formData.action}
                onValueChange={(value) =>
                  setFormData({ ...formData, action: value as Notification["action"] })
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="TRANSACTION_CREATED">Transaction créée</SelectItem>
                  <SelectItem value="TRANSACTION_VERIFIED_OK">Transaction vérifiée (OK)</SelectItem>
                  <SelectItem value="TRANSACTION_VERIFIED_NOK">Transaction vérifiée (NOK)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="transactionId">ID Transaction</Label>
              <Input
                id="transactionId"
                type="number"
                placeholder="123"
                value={formData.transactionId}
                onChange={(e) => setFormData({ ...formData, transactionId: e.target.value })}
              />
            </div>
          </div>

          <Button onClick={createNotification} disabled={isLoading} className="w-full gap-2">
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Création en cours...
              </>
            ) : (
              <>
                <Send className="w-4 h-4" />
                Créer la notification
              </>
            )}
          </Button>
        </CardContent>
      </Card>

      {/* Liste des notifications */}
      <Card className="bg-card border-border">
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>Notifications ({notifications.length})</CardTitle>
            <CardDescription>
              {unreadCount > 0 ? `${unreadCount} notification(s) non lue(s)` : "Toutes les notifications sont lues"}
            </CardDescription>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={loadNotifications} disabled={isLoading}>
              {isLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : "Actualiser"}
            </Button>
            {unreadCount > 0 && (
              <Button variant="outline" size="sm" onClick={markAllAsSeen}>
                Tout marquer comme lu
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {isLoading && notifications.length === 0 ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
            </div>
          ) : notifications.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Bell className="w-12 h-12 mx-auto mb-4 opacity-50" />
              <p>Aucune notification</p>
            </div>
          ) : (
            <div className="space-y-3">
              {notifications.map((notification) => {
                const isUnread = !notification.dateSeenAt
                return (
                  <div
                    key={notification.id}
                    className={`p-4 rounded-lg border ${
                      isUnread
                        ? "bg-sidebar-accent/50 border-sidebar-border"
                        : "bg-card border-border"
                    }`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <Badge variant="outline">{actionLabels[notification.action] || notification.action}</Badge>
                          {isUnread && (
                            <Badge variant="default" className="bg-emerald-500">
                              Non lue
                            </Badge>
                          )}
                        </div>
                        <div className="text-sm text-muted-foreground space-y-1">
                          <p>ID: {notification.id}</p>
                          <p>Déclencheur: {notification.triggerId}</p>
                          <p>Destinataire: {notification.receivedId}</p>
                          <p>Transaction ID: {notification.transactionId}</p>
                          {notification.dateSeenAt && (
                            <p>Lu le: {new Date(notification.dateSeenAt).toLocaleString("fr-FR")}</p>
                          )}
                        </div>
                      </div>
                      {isUnread && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => markAsSeen(notification.id)}
                        >
                          <CheckCircle2 className="w-4 h-4" />
                        </Button>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Input } from "@/components/ui/input"
import { Bell, User, LogOut, Shield, CheckCircle2, CheckCheck } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { ThemeToggle } from "@/components/theme-toggle"
import { authService } from "@/lib/services/auth.service"
import { NotificationService, type Notification } from "@/lib/services/notification.service"

export function DashboardHeader() {
  const router = useRouter()
  const [userName, setUserName] = useState<string | null>(null)
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    // Récupérer le userName depuis localStorage (côté client uniquement)
    setUserName(authService.getUserName())
  }, [])

  useEffect(() => {
    const fetchNotifications = async () => {
      const userId = authService.getUserId()
      if (!userId) return

      try {
        const notificationsData = await NotificationService.getNotificationsForUser(Number(userId))
        setNotifications(notificationsData)
        setUnreadCount(notificationsData.length)
      } catch (err) {
        console.error("Erreur lors du chargement des notifications:", err)
      }
    }

    fetchNotifications()
    // Rafraîchir les notifications toutes les 30 secondes
    const interval = setInterval(fetchNotifications, 30000)
    return () => clearInterval(interval)
  }, [])

  const handleLogout = () => {
    authService.logout()
    router.push("/login")
  }

  // Fonction pour obtenir les initiales du userName
  const getInitials = (name: string | null): string => {
    if (!name) return "U"
    const parts = name.trim().split(" ")
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    }
    return name.substring(0, 2).toUpperCase()
  }

  const displayName = userName || "Utilisateur"

  return (
    <header className="flex items-center justify-between h-16 px-6 border-b border-border bg-background">
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-emerald-500/10 border border-emerald-500/20">
          <Shield className="w-4 h-4 text-emerald-500" />
          <span className="text-sm font-medium text-emerald-600 dark:text-emerald-400">Système sécurisé</span>
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500" />
        </div>
        <div className="hidden md:flex items-center gap-2 text-sm text-muted-foreground">
          <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span>Toutes les connexions sont chiffrées</span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <ThemeToggle />
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="relative">
              <Bell className="w-5 h-5 text-muted-foreground" />
              {unreadCount > 0 && (
                <Badge className="absolute -top-1 -right-1 h-5 w-5 rounded-full p-0 flex items-center justify-center text-xs bg-emerald-500 text-white border-0">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </Badge>
              )}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-80">
            <DropdownMenuLabel className="flex items-center justify-between">
              <span>Notifications</span>
              {unreadCount > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-6 text-xs"
                  onClick={async (e) => {
                    e.stopPropagation()
                    const userId = authService.getUserId()
                    if (userId) {
                      try {
                        await NotificationService.markAllAsSeen(Number(userId))
                        setNotifications([])
                        setUnreadCount(0)
                      } catch (err) {
                        console.error("Erreur lors du marquage des notifications:", err)
                      }
                    }
                  }}
                >
                  <CheckCheck className="mr-1 h-3 w-3" />
                  Tout marquer comme lu
                </Button>
              )}
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            {notifications.length === 0 ? (
              <div className="p-4 text-center text-sm text-muted-foreground">
                Aucune notification
              </div>
            ) : (
              <div className="max-h-96 overflow-y-auto">
                {notifications.slice(0, 5).map((notification) => (
                  <DropdownMenuItem
                    key={notification.id}
                    className="flex flex-col items-start p-3 cursor-pointer"
                    onClick={() => router.push("/notifications")}
                  >
                    <div className="flex items-center justify-between w-full mb-1">
                      <span className="text-xs font-medium">
                        {notification.action === "TRANSACTION_CREATED"
                          ? "Transaction créée"
                          : notification.action === "TRANSACTION_VERIFIED_OK"
                            ? "Transaction vérifiée (OK)"
                            : "Transaction vérifiée (NOK)"}
                      </span>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-5 w-5 p-0"
                        onClick={async (e) => {
                          e.stopPropagation()
                          try {
                            await NotificationService.markAsSeen(notification.id)
                            setNotifications((prev) => prev.filter((n) => n.id !== notification.id))
                            setUnreadCount((prev) => Math.max(0, prev - 1))
                          } catch (err) {
                            console.error("Erreur lors du marquage de la notification:", err)
                          }
                        }}
                      >
                        <CheckCircle2 className="h-3 w-3" />
                      </Button>
                    </div>
                    <span className="text-xs text-muted-foreground">
                      Transaction ID: {notification.transactionId}
                    </span>
                  </DropdownMenuItem>
                ))}
                {notifications.length > 5 && (
                  <>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem
                      className="text-center justify-center cursor-pointer"
                      onClick={() => router.push("/notifications")}
                    >
                      Voir toutes les notifications ({notifications.length})
                    </DropdownMenuItem>
                  </>
                )}
              </div>
            )}
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className="text-center justify-center cursor-pointer"
              onClick={() => router.push("/notifications")}
            >
              Voir toutes les notifications
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="gap-2 px-2">
              <Avatar className="h-8 w-8">
                <AvatarFallback className="bg-emerald-500/10 text-emerald-500 text-sm">
                  {getInitials(userName)}
                </AvatarFallback>
              </Avatar>
              <span className="text-sm font-medium text-foreground hidden sm:block">{displayName}</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel>Mon compte</DropdownMenuLabel>
            <DropdownMenuItem className="text-destructive" onClick={handleLogout}>
              <LogOut className="mr-2 h-4 w-4" />
              Déconnexion
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}

"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import {
  LayoutDashboard,
  Video,
  Users,
  Shield,
  FileText,
  Settings,
  Key,
  Bell,
  ChevronLeft,
  ChevronRight,
  Lock,
  Banknote,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { authService } from "@/lib/services/auth.service"

const allNavigationItems = [
  { name: "Tableau de bord", href: "/dashboard", icon: LayoutDashboard, roles: ["ADMIN", "USER"] },
  { name: "Ordres de virement", href: "/virements", icon: Banknote, roles: ["USER"] },
  { name: "Gestion key", href: "/keys", icon: Key, roles: ["USER"] },
  { name: "Utilisateurs", href: "/users", icon: Users, roles: ["ADMIN"] },
  // { name: "Gestion des Clés", href: "/keys", icon: Key },
  // { name: "Notifications", href: "/notifications", icon: Bell },
  // { name: "Journal d'Audit", href: "/audit", icon: FileText },
  // { name: "Sécurité", href: "/security", icon: Shield },
  // { name: "Paramètres", href: "/settings", icon: Settings },
  { name: "Notifications", href: "/notifications", icon: Bell, roles: ["USER"] },
]

export function DashboardSidebar() {
  const [collapsed, setCollapsed] = useState(false)
  const [userRole, setUserRole] = useState<string | null>(null)
  const pathname = usePathname()

  useEffect(() => {
    // Récupérer le rôle de l'utilisateur depuis localStorage (côté client uniquement)
    setUserRole(authService.getRole())
  }, [])

  // Filtrer les éléments de navigation selon le rôle
  const navigation = allNavigationItems.filter((item) => {
    if (!userRole) return false
    return item.roles.includes(userRole)
  })

  return (
    <aside
      className={cn(
        "flex flex-col bg-sidebar border-r border-sidebar-border transition-all duration-300",
        collapsed ? "w-16" : "w-64",
      )}
    >
      <div className="flex items-center gap-3 px-4 h-16 border-b border-sidebar-border">
        <div className="flex items-center justify-center w-8 h-8 rounded-lg bg-emerald-500/10">
          <Lock className="w-4 h-4 text-emerald-500" />
        </div>
        {!collapsed && <span className="font-semibold text-sidebar-foreground">Moustass vidéo</span>}
      </div>

      <nav className="flex-1 p-3 space-y-1">
        {navigation.map((item) => {
          const isActive = pathname === item.href
          return (
            <Link
              key={item.name}
              href={item.href}
              className={cn(
                "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                isActive
                  ? "bg-sidebar-accent text-sidebar-accent-foreground"
                  : "text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground",
              )}
            >
              <item.icon className="w-5 h-5 shrink-0" />
              {!collapsed && <span>{item.name}</span>}
            </Link>
          )
        })}
      </nav>

      <div className="p-3 border-t border-sidebar-border">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setCollapsed(!collapsed)}
          className="w-full justify-center text-sidebar-foreground/70 hover:text-sidebar-foreground"
        >
          {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </Button>
      </div>
    </aside>
  )
}

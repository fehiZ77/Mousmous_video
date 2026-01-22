"use client"

import { useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { UsersList } from "@/components/users-list"
import { CreateUserDialog } from "@/components/create-user-dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { UserPlus, Search, Filter } from "lucide-react"

export function UsersPageContent() {
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const handleUserCreated = () => {
    // Incrémenter refreshTrigger pour forcer le rafraîchissement de la liste
    setRefreshTrigger((prev) => prev + 1)
  }

  return (
    <div className="flex min-h-screen bg-background">
      <DashboardSidebar />
      <div className="flex-1 flex flex-col">
        <DashboardHeader />
        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-semibold text-foreground">Gestion des Utilisateurs</h1>
              <p className="text-muted-foreground mt-1">Créez et gérez les comptes utilisateurs du backoffice</p>
            </div>
            <Button onClick={() => setIsCreateDialogOpen(true)}>
              <UserPlus className="w-4 h-4 mr-2" />
              Nouvel utilisateur
            </Button>
          </div>
          <div className="flex items-center gap-4">
            <div className="relative flex-1 max-w-md">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Rechercher par nom, email ou rôle..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9 bg-secondary border-border"
              />
            </div>
          </div>
          <UsersList searchQuery={searchQuery} refreshTrigger={refreshTrigger} />

          <CreateUserDialog
            open={isCreateDialogOpen}
            onOpenChange={setIsCreateDialogOpen}
            onUserCreated={handleUserCreated}
          />
        </main>
      </div>
    </div>
  )
}

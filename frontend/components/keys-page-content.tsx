"use client"

import { useState } from "react"
import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { KeysList } from "@/components/keys-list"
import { GenerateKeyDialog } from "@/components/generate-key-dialog"
import { Button } from "@/components/ui/button"
import { Key } from "lucide-react"

export function KeysPageContent() {
  const [isGenerateDialogOpen, setIsGenerateDialogOpen] = useState(false)
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  const handleKeyGenerated = () => {
    // Incrémenter refreshTrigger pour forcer le rafraîchissement de la liste
    setRefreshTrigger((prev) => prev + 1)
  }

  const handleRefresh = () => {
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
              <h1 className="text-2xl font-semibold text-foreground">Gestion des Clés</h1>
              <p className="text-muted-foreground mt-1">Gérez vos paires de clés cryptographiques</p>
            </div>
            <Button onClick={() => setIsGenerateDialogOpen(true)}>
              <Key className="w-4 h-4 mr-2" />
              Générer une clé
            </Button>
          </div>

          <KeysList refreshTrigger={refreshTrigger} onRefresh={handleRefresh} />

          <GenerateKeyDialog
            open={isGenerateDialogOpen}
            onOpenChange={setIsGenerateDialogOpen}
            onKeyGenerated={handleKeyGenerated}
          />
        </main>
      </div>
    </div>
  )
}

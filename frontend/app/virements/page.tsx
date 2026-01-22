"use client"
import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { TransferOrderForm } from "@/components/transfer-order-form"

export default function VirementsPage() {
  return (
    <div className="flex min-h-screen bg-background">
      <DashboardSidebar />
      <div className="flex-1 flex flex-col">
        <DashboardHeader />
        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <div>
            <h1 className="text-2xl font-semibold text-foreground">Création d'ordre de virement</h1>
            <p className="text-muted-foreground mt-1">
              Initier un nouveau virement sécurisé avec authentification vidéo
            </p>
          </div>

          <TransferOrderForm />
        </main>
      </div>
    </div>
  )
}

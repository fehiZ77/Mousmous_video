"use client"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { TransferOrderForm } from "@/components/transfer-order-form"
import { AlertDialog, AlertDialogAction, AlertDialogDescription, AlertDialogHeader, AlertDialogTitle, AlertDialogContent } from "@/components/ui/alert-dialog"
import { keyService } from "@/lib/services/key.service"

export default function VirementsPage() {
  const router = useRouter()
  const [showNoKeysDialog, setShowNoKeysDialog] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const checkKeys = async () => {
      try {
        const validKeys = await keyService.listValidKeys()
        if (validKeys.length === 0) {
          setShowNoKeysDialog(true)
        }
      } catch (error) {
        console.error("Erreur lors de la vérification des clés:", error)
      } finally {
        setIsLoading(false)
      }
    }

    checkKeys()
  }, [])

  const handleNoKeysConfirm = () => {
    setShowNoKeysDialog(false)
    router.push("/keys")
  }

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

          {!isLoading && <TransferOrderForm />}
        </main>
      </div>

      <AlertDialog open={showNoKeysDialog} onOpenChange={setShowNoKeysDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Clé d'authentification requise</AlertDialogTitle>
            <AlertDialogDescription>
              Vous devez générer une paire de clé SK/PK avant de pouvoir créer un ordre de virement. Cliquez sur OK pour accéder à la gestion des clés.
            </AlertDialogDescription>
          </AlertDialogHeader>
            <AlertDialogAction onClick={handleNoKeysConfirm}>OK</AlertDialogAction>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

"use client"

import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { StatsCards } from "@/components/stats-cards"
import { CreatedTransactions } from "@/components/created-transactions"
import { TransactionsToVerify } from "@/components/transactions-to-verify"
import { getRole } from "@/lib/utils/jwt.utils"
import { useEffect, useState } from "react"

export default function DashboardPage() {
  const [isAdmin, setIsAdmin] = useState(false)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const role = getRole()
    setIsAdmin(role === "ADMIN")
    setIsLoading(false)
  }, [])

  if (isLoading) {
    return null
  }

  return (
    <div className="flex min-h-screen bg-background">
      <DashboardSidebar />
      <div className="flex-1 flex flex-col">
        <DashboardHeader />
        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <div>
            <h1 className="text-2xl font-semibold text-foreground">Tableau de bord</h1>
            <p className="text-muted-foreground mt-1">Vue d'ensemble de votre messagerie vidéo sécurisée</p>
          </div>

          <StatsCards />

          {!isAdmin && (
            <div className="grid grid-cols-1 gap-6">
              <CreatedTransactions />
              <TransactionsToVerify />
            </div>
          )}
        </main>
      </div>
    </div>
  )
}

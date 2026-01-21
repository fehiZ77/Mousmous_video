"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/lib/utils/jwt.utils"

export default function HomePage() {
  const router = useRouter()

  useEffect(() => {
    // Vérifier si l'utilisateur est authentifié
    if (isAuthenticated()) {
      router.push("/dashboard")
    } else {
      router.push("/login")
    }
  }, [router])

  // Afficher un loader pendant la vérification
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="text-muted-foreground">Chargement...</div>
    </div>
  )
}

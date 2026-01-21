"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Lock, Eye, EyeOff, Shield, AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { authService } from "@/lib/services/auth.service"
import { isAuthenticated } from "@/lib/utils/jwt.utils"

export default function LoginPage() {
  const router = useRouter()
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState("")
  const [isChecking, setIsChecking] = useState(true)
  const [formData, setFormData] = useState({
    userName: "",
    mdp: "",
  })

  // Vérifier si l'utilisateur est déjà authentifié
  useEffect(() => {
    if (isAuthenticated()) {
      router.push("/dashboard")
    } else {
      setIsChecking(false)
    }
  }, [router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError("")

    try {
      // Appel à l'API backend
      const authResponse = await authService.login({
        userName: formData.userName,
        mdp: formData.mdp,
      })

      // Enregistrer le token et les infos utilisateur
      authService.saveAuthData(authResponse)

      // Vérifier si c'est la première connexion
      if (authResponse.firstLogin) {
        // Rediriger vers la page de changement de mot de passe
        router.push("/change-password?first=true")
      } else {
        // Rediriger vers le dashboard
        router.push("/dashboard")
      }
    } catch (err: any) {
      // Gérer les erreurs
      const errorMessage = err.response?.data || err.message || "Une erreur est survenue lors de la connexion"
      setError(typeof errorMessage === "string" ? errorMessage : "Erreur de connexion")
    } finally {
      setIsLoading(false)
    }
  }

  // Afficher un loader pendant la vérification
  if (isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <div className="text-muted-foreground">Vérification...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <div className="w-full max-w-md space-y-6">
        {/* Logo et titre */}
        <div className="flex flex-col items-center space-y-4">
          <div className="flex items-center justify-center w-16 h-16 rounded-2xl bg-primary/10 border border-primary/20">
            <Lock className="w-8 h-8 text-primary" />
          </div>
          <div className="text-center">
            <h1 className="text-2xl font-semibold text-foreground">Barbichetz</h1>
            <p className="text-sm text-muted-foreground mt-1">Messagerie Vidéo Sécurisée</p>
          </div>
        </div>

        {/* Formulaire de connexion */}
        <Card className="bg-card border-border">
          <CardHeader className="space-y-1">
            <CardTitle className="text-xl">Connexion</CardTitle>
            <CardDescription>Accédez à votre espace d'administration sécurisé</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              {error && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
              )}

              <div className="space-y-2">
                <Label htmlFor="userName">Nom d'utilisateur</Label>
                <Input
                  id="userName"
                  type="text"
                  placeholder="admin"
                  value={formData.userName}
                  onChange={(e) =>
                    setFormData({ ...formData, userName: e.target.value })
                  }
                  required
                  className="bg-input border-border"
                />
              </div>

              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="password">Mot de passe</Label>
                </div>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    value={formData.mdp}
                    onChange={(e) => setFormData({ ...formData, mdp: e.target.value })}
                    required
                    className="bg-input border-border pr-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? "Connexion en cours..." : "Se connecter"}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Indicateurs de sécurité */}
        <div className="flex items-center justify-center gap-4 text-xs text-muted-foreground">
          <div className="flex items-center gap-1.5">
            <Shield className="w-3.5 h-3.5 text-primary" />
            <span>RSA</span>
          </div>
          <div className="h-3 w-px bg-border" />
          <div className="flex items-center gap-1.5">
            <Shield className="w-3.5 h-3.5 text-primary" />
            <span>SHA-256</span>
          </div>
          <div className="h-3 w-px bg-border" />
          <div className="flex items-center gap-1.5">
            <Shield className="w-3.5 h-3.5 text-primary" />
            <span>RGPD</span>
          </div>
        </div>
      </div>
    </div>
  )
}

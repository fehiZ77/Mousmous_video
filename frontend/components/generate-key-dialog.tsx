"use client"

import type React from "react"
import { useState } from "react"
import { CheckCircle, AlertCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { keyService } from "@/lib/services/key.service"
import type { GenerateKeyDto } from "@/lib/types/key"

interface GenerateKeyDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onKeyGenerated?: () => void
}

export function GenerateKeyDialog({ open, onOpenChange, onKeyGenerated }: GenerateKeyDialogProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showDownloadPrompt, setShowDownloadPrompt] = useState(false)

  const [formData, setFormData] = useState<GenerateKeyDto>({
    keyName: "",
    validity: 12, // Par défaut 12 mois
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setIsLoading(true)
    setError(null)
    try {
      if (!formData.keyName.trim()) {
        setError("Le nom de la clé est requis")
        setIsLoading(false)
        return
      }

      if (!formData.validity || ![1, 3, 6, 12].includes(formData.validity)) {
        setError("Veuillez sélectionner une validité valide")
        setIsLoading(false)
        return
      }

      // N'effectue pas encore la génération : afficher la popup de téléchargement
      setShowDownloadPrompt(true)
      setIsLoading(false)
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || "Erreur lors de la génération de la clé"
      setError(typeof errorMessage === "string" ? errorMessage : "Erreur de génération")
      setIsLoading(false)
    }
  }

  const handleDownload = async () => {
    setIsLoading(true)
    setError(null)

    try {
      const privateKey = await keyService.generateKeyPair(formData)

      const filename = `${formData.keyName.trim() || "private-key"}.pem`
      const blob = new Blob([privateKey], { type: "application/x-pem-file" })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement("a")
      a.href = url
      a.download = filename
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(url)

      // Indiquer le succès et rafraîchir la liste
      setShowDownloadPrompt(false)
      setSuccess(true)
      setIsLoading(false)

      if (onKeyGenerated) {
        onKeyGenerated()
      }

      setTimeout(() => {
        onOpenChange(false)
        setSuccess(false)
        setFormData({ keyName: "", validity: 12 })
      }, 2000)
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || "Erreur lors de la génération de la clé"
      setError(typeof errorMessage === "string" ? errorMessage : "Erreur de génération")
      setIsLoading(false)
    }
  }

  if (success) {
    return (
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="bg-card border-border sm:max-w-md">
          <div className="flex flex-col items-center text-center py-6 space-y-4">
            <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
              <CheckCircle className="w-8 h-8 text-primary" />
            </div>
            <div>
              <h2 className="text-xl font-semibold text-foreground">Clé générée avec succès</h2>
              <p className="text-sm text-muted-foreground mt-2">Votre nouvelle paire de clés a été créée</p>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    )
  }

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="bg-card border-border sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Générer une nouvelle clé</DialogTitle>
          <DialogDescription>
            Créez une nouvelle paire de clés cryptographiques. Le userId sera automatiquement extrait de votre token.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <div className="space-y-2">
            <Label htmlFor="keyName">Nom de la clé</Label>
            <Input
              id="keyName"
              value={formData.keyName}
              onChange={(e) => setFormData({ ...formData, keyName: e.target.value })}
              placeholder="Ex: Ma clé principale"
              required
              className="bg-input border-border"
            />
            <p className="text-xs text-muted-foreground">
              Choisissez un nom descriptif pour identifier cette clé
            </p>
          </div>

          <div className="space-y-2">
            <Label htmlFor="validity">Validité (en mois)</Label>
            <Select
              value={formData.validity.toString()}
              onValueChange={(value) => setFormData({ ...formData, validity: parseInt(value) })}
            >
              <SelectTrigger className="bg-input border-border" id="validity">
                <SelectValue placeholder="Sélectionner une durée" />
              </SelectTrigger>
              <SelectContent className="bg-card border-border">
                <SelectItem value="1">1 mois</SelectItem>
                <SelectItem value="3">3 mois</SelectItem>
                <SelectItem value="6">6 mois</SelectItem>
                <SelectItem value="12">12 mois</SelectItem>
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              Durée de validité de la clé en mois
            </p>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Annuler
            </Button>
            <Button type="submit" disabled={isLoading || !formData.keyName.trim()}>
              {isLoading ? "Génération en cours..." : "Générer la clé"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
      </Dialog>

      <Dialog open={showDownloadPrompt} onOpenChange={setShowDownloadPrompt}>
        <DialogContent className="bg-card border-border sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Télécharger la clé privée</DialogTitle>
            <DialogDescription>
              Vous pouvez télécharger votre clé privée sous le nom de <strong>{formData.keyName || "private-key"}.pem</strong>
            </DialogDescription>
          </DialogHeader>
           <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription className="space-y-2">
              <p>
                <strong>Attention :</strong> cette clé privée est générée{" "}
                <strong>une seule fois</strong>.
              </p>
              <ul className="list-disc pl-4 text-sm">
                <li>Elle ne pourra <strong>jamais être récupérée</strong> plus tard.</li>
                <li>Conservez-la dans un endroit <strong>sûr et hors ligne</strong>.</li>
                <li>Ne la partagez <strong>jamais</strong> (même avec un admin).</li>
                <li>Si vous la perdez, vous devrez <strong>générer une nouvelle clé</strong>.</li>
              </ul>
            </AlertDescription>
          </Alert>

          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setShowDownloadPrompt(false)}>
              Annuler
            </Button>
            <Button type="button" onClick={handleDownload} disabled={isLoading}>
              {isLoading ? "Génération en cours..." : "Télécharger"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

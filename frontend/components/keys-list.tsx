"use client"

import { useState, useEffect } from "react"
import { keyService } from "@/lib/services/key.service"
import type { UserKeys } from "@/lib/types/key"
import { KeyStatus } from "@/lib/types/key"
import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Eye, Copy, CheckCircle, Ban } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle } from "lucide-react"

interface KeysListProps {
  refreshTrigger?: number
  onRefresh?: () => void
}

export function KeysList({ refreshTrigger, onRefresh }: KeysListProps) {
  const [keys, setKeys] = useState<UserKeys[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedPublicKey, setSelectedPublicKey] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)
  const [keyToRevoke, setKeyToRevoke] = useState<{ id: number; name: string } | null>(null)
  const [isRevoking, setIsRevoking] = useState(false)

  const fetchKeys = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await keyService.listKeys()
      setKeys(data)
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Erreur lors du chargement des clés")
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchKeys()
  }, [refreshTrigger])

  const handleCopyPublicKey = async (publicKey: string) => {
    try {
      await navigator.clipboard.writeText(publicKey)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch (err) {
      console.error("Erreur lors de la copie:", err)
    }
  }

  const handleRevokeKey = async () => {
    if (!keyToRevoke) return

    setIsRevoking(true)
    setError(null)
    try {
      await keyService.revokeKey(keyToRevoke.id)
      setKeyToRevoke(null)
      // Rafraîchir la liste
      await fetchKeys()
      if (onRefresh) {
        onRefresh()
      }
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || "Erreur lors de la révocation de la clé")
    } finally {
      setIsRevoking(false)
    }
  }

  const getStatusBadgeVariant = (status: KeyStatus) => {
    switch (status) {
      case KeyStatus.ACTIVE:
        return "default"
      case KeyStatus.REVOKED:
        return "destructive"
      case KeyStatus.EXPIRED:
        return "secondary"
      default:
        return "outline"
    }
  }

  const formatDate = (dateString: string | null) => {
    if (!dateString) return "N/A"
    return new Date(dateString).toLocaleDateString("fr-FR", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-muted-foreground">Chargement des clés...</div>
      </div>
    )
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    )
  }

  if (keys.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <div className="text-muted-foreground mb-4">Aucune clé trouvée</div>
        <div className="text-sm text-muted-foreground">Générez votre première clé pour commencer</div>
      </div>
    )
  }

  return (
    <>
      <div className="rounded-lg border border-border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nom de la clé</TableHead>
              <TableHead>Statut</TableHead>
              <TableHead>Date d'expiration</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {keys.map((key) => (
              <TableRow key={key.id}>
                <TableCell className="font-medium">{key.keyName}</TableCell>
                <TableCell>
                  <Badge variant={getStatusBadgeVariant(key.status)}>
                    {key.status}
                  </Badge>
                </TableCell>
                <TableCell>{formatDate(key.expiredAt)}</TableCell>
                <TableCell className="text-right">
                  <div className="flex items-center justify-end gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setSelectedPublicKey(key.publicKey)}
                    >
                      <Eye className="w-4 h-4 mr-2" />
                      Voir la clé publique
                    </Button>
                    {key.status === KeyStatus.ACTIVE && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setKeyToRevoke({ id: key.id, name: key.keyName })}
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                      >
                        <Ban className="w-4 h-4 mr-2" />
                        Révoquer
                      </Button>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Dialog pour afficher la clé publique */}
      <Dialog open={selectedPublicKey !== null} onOpenChange={(open) => !open && setSelectedPublicKey(null)}>
        <DialogContent className="bg-card border-border sm:max-w-3xl max-h-[90vh] flex flex-col">
          <DialogHeader>
            <DialogTitle>Clé publique</DialogTitle>
            <DialogDescription>
              Voici votre clé publique. Vous pouvez la copier pour la partager.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 flex-1 flex flex-col min-h-0">
            <div className="relative flex-1 min-h-0">
              <textarea
                readOnly
                value={selectedPublicKey || ""}
                className="w-full h-full min-h-[300px] p-4 bg-secondary rounded-lg border border-border text-xs font-mono resize-none overflow-auto"
                style={{ wordBreak: "break-all", whiteSpace: "pre-wrap" }}
              />
            </div>
            <Button
              onClick={() => selectedPublicKey && handleCopyPublicKey(selectedPublicKey)}
              className="w-full"
              variant="outline"
            >
              {copied ? (
                <>
                  <CheckCircle className="w-4 h-4 mr-2" />
                  Copié !
                </>
              ) : (
                <>
                  <Copy className="w-4 h-4 mr-2" />
                  Copier la clé publique
                </>
              )}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      {/* AlertDialog pour confirmer la révocation */}
      <AlertDialog open={keyToRevoke !== null} onOpenChange={(open) => !open && setKeyToRevoke(null)}>
        <AlertDialogContent className="bg-card border-border">
          <AlertDialogHeader>
            <AlertDialogTitle>Confirmer la révocation</AlertDialogTitle>
            <AlertDialogDescription>
              Êtes-vous sûr de vouloir révoquer la clé <strong>{keyToRevoke?.name}</strong> ?
              <br />
              Cette action est irréversible et la clé ne pourra plus être utilisée.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel disabled={isRevoking}>Annuler</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleRevokeKey}
              disabled={isRevoking}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {isRevoking ? "Révocation..." : "Révoquer la clé"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}

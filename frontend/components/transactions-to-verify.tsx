"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { transactionService } from "@/lib/services/transaction.service"
import type { TransactionResponseDto } from "@/lib/types/transaction"
import { Play, Loader2, Copy, AlertTriangle, CheckCircle2 } from "lucide-react"

export function TransactionsToVerify() {
  const [transactions, setTransactions] = useState<TransactionResponseDto[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedVideo, setSelectedVideo] = useState<string | null>(null)
  const [isVideoDialogOpen, setIsVideoDialogOpen] = useState(false)
  const [isLoadingVideo, setIsLoadingVideo] = useState(false)
  const [isPublicKeyDialogOpen, setIsPublicKeyDialogOpen] = useState(false)
  const [selectedPublicKey, setSelectedPublicKey] = useState<string | null>(null)
  const [copiedKey, setCopiedKey] = useState(false)
  const [isVerifyDialogOpen, setIsVerifyDialogOpen] = useState(false)
  const [selectedTransactionId, setSelectedTransactionId] = useState<number | null>(null)
  const [publicKey, setPublicKey] = useState("")
  const [isVerifying, setIsVerifying] = useState(false)
  const [verifyError, setVerifyError] = useState<string | null>(null)
  const [verifySuccess, setVerifySuccess] = useState(false)

  useEffect(() => {
    fetchTransactions()
  }, [])

  const fetchTransactions = async () => {
    try {
      setIsLoading(true)
      setError(null)
      const data = await transactionService.getTransactionsToVerify()
      setTransactions(data)
    } catch (err) {
      console.error("Erreur lors du chargement des transactions:", err)
      setError("Impossible de charger les transactions à vérifier")
    } finally {
      setIsLoading(false)
    }
  }

  const handleWatchVideo = async (objectName: string) => {
    setIsLoadingVideo(true)
    try {
      const blob = await transactionService.getVideoStream(objectName)
      const videoUrl = URL.createObjectURL(blob)
      setSelectedVideo(videoUrl)
      setIsVideoDialogOpen(true)
    } catch (err) {
      console.error("Erreur lors du chargement de la vidéo:", err)
      alert("Impossible de charger la vidéo")
    } finally {
      setIsLoadingVideo(false)
    }
  }

  const handleShowPublicKey = (publicKey: string) => {
    setSelectedPublicKey(publicKey)
    setIsPublicKeyDialogOpen(true)
    setCopiedKey(false)
  }

  const handleCopyKey = () => {
    if (selectedPublicKey) {
      navigator.clipboard.writeText(selectedPublicKey)
      setCopiedKey(true)
      setTimeout(() => setCopiedKey(false), 2000)
    }
  }

  const handleVerify = (transactionId: number) => {
    setSelectedTransactionId(transactionId)
    setPublicKey("")
    setVerifyError(null)
    setVerifySuccess(false)
    setIsVerifyDialogOpen(true)
  }

  const handleVerifySubmit = async () => {
    if (!publicKey.trim()) {
      setVerifyError("Veuillez entrer une clé publique")
      return
    }

    setIsVerifying(true)
    setVerifyError(null)
    setVerifySuccess(false)

    try {
      const isValid = await transactionService.verifyTransaction(selectedTransactionId!, publicKey)

      if (isValid) {
        setVerifySuccess(true)
        setTimeout(() => {
          setIsVerifyDialogOpen(false)
          setPublicKey("")
          setSelectedTransactionId(null)
          fetchTransactions()
        }, 2000)
      } else {
        setVerifyError("Transaction corrompue - La clé publique ne correspond pas")
      }
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || "Erreur lors de la vérification"
      setVerifyError(typeof errorMessage === "string" ? errorMessage : "Erreur de vérification")
    } finally {
      setIsVerifying(false)
    }
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString("fr-FR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  const formatAmount = (amount: number) => {
    return new Intl.NumberFormat("fr-FR", {
      style: "currency",
      currency: "MUR",
    }).format(amount)
  }

  return (
    <>
      <Card className="bg-card border-border">
        <CardHeader>
          <CardTitle className="text-lg font-semibold text-foreground">Transactions à vérifier</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
            </div>
          ) : error ? (
            <div className="text-center py-8 text-destructive">{error}</div>
          ) : transactions.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">Aucune transaction à vérifier</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="border-border hover:bg-transparent">
                    <TableHead className="text-muted-foreground">Date</TableHead>
                    <TableHead className="text-muted-foreground">Créateur</TableHead>
                    <TableHead className="text-muted-foreground">Montant</TableHead>
                    <TableHead className="text-muted-foreground">Vidéo</TableHead>
                    <TableHead className="text-muted-foreground">Clé publique</TableHead>
                    <TableHead className="text-muted-foreground">Status</TableHead>
                    <TableHead className="text-muted-foreground">Action</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {transactions.map((transaction) => (
                    <TableRow key={transaction.transactionId} className="border-border">
                      <TableCell className="text-sm text-foreground">
                        {formatDate(transaction.date)}
                      </TableCell>
                      <TableCell className="text-sm text-foreground">{transaction.userName}</TableCell>
                      <TableCell className="text-sm text-foreground font-medium">
                        {formatAmount(transaction.amount)}
                      </TableCell>
                      <TableCell className="text-sm">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="gap-2 text-primary hover:text-primary/80"
                          onClick={() => handleWatchVideo(transaction.objectName)}
                          disabled={isLoadingVideo}
                        >
                          {isLoadingVideo ? <Loader2 className="w-4 h-4 animate-spin" /> : <Play className="w-4 h-4" />}
                          Voir vidéo
                        </Button>
                      </TableCell>
                      <TableCell className="text-sm">
                        <Button
                          variant="ghost"
                          size="sm"
                          className="gap-2 text-primary hover:text-primary/80"
                          onClick={() => handleShowPublicKey(transaction.publicKey)}
                        >
                          Voir PK
                        </Button>
                      </TableCell>
                      <TableCell className="text-sm">
                        {transaction.status === "VERIFIED" ? (
                          <span className="text-emerald-600 dark:text-emerald-400 font-medium">✓ Transaction safe</span>
                        ) : (
                          <span className="text-yellow-600 dark:text-yellow-400 font-medium">⏳ En attente</span>
                        )}
                      </TableCell>
                      <TableCell className="text-sm">
                        <Button
                          size="sm"
                          className="gap-2"
                          onClick={() => handleVerify(transaction.transactionId)}
                        >
                          Vérifier
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialog Vidéo */}
      <Dialog open={isVideoDialogOpen} onOpenChange={setIsVideoDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Vidéo de la transaction</DialogTitle>
          </DialogHeader>
          {selectedVideo && (
            <div className="w-full">
              <video
                controls
                className="w-full rounded-lg bg-black"
                src={selectedVideo}
              />
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Dialog Clé publique */}
      <Dialog open={isPublicKeyDialogOpen} onOpenChange={setIsPublicKeyDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Clé publique</DialogTitle>
          </DialogHeader>
          {selectedPublicKey && (
            <div className="space-y-4">
              <div className="relative">
                <textarea
                  readOnly
                  value={selectedPublicKey}
                  className="w-full h-40 p-4 bg-muted border border-border rounded-lg font-mono text-sm resize-none focus:outline-none focus:ring-1 focus:ring-primary"
                />
                <Button
                  size="sm"
                  variant="outline"
                  className="absolute top-2 right-2 gap-2"
                  onClick={handleCopyKey}
                >
                  <Copy className="w-4 h-4" />
                  {copiedKey ? "Copié!" : "Copier"}
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Dialog Vérification */}
      <Dialog open={isVerifyDialogOpen} onOpenChange={setIsVerifyDialogOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Vérifier la transaction</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            {verifyError && (
              <Alert variant="destructive">
                <AlertTriangle className="h-4 w-4" />
                <AlertDescription>{verifyError}</AlertDescription>
              </Alert>
            )}

            {verifySuccess && (
              <Alert className="bg-emerald-500/10 border-emerald-500/20">
                <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                <AlertDescription className="text-emerald-700 dark:text-emerald-400">
                  Transaction vérifiée avec succès !
                </AlertDescription>
              </Alert>
            )}

            <div className="space-y-2">
              <Label htmlFor="private-key">Clé publique de l'expéditeur</Label>
              <textarea
                id="private-key"
                placeholder="Entrez la clé publique de l'expéditeur..."
                value={publicKey}
                onChange={(e) => setPublicKey(e.target.value)}
                className="w-full h-32 p-3 bg-input border border-border rounded-lg font-mono text-sm resize-none focus:outline-none focus:ring-1 focus:ring-primary"
                disabled={isVerifying || verifySuccess}
              />
            </div>

            <div className="flex gap-3 justify-end">
              <Button
                variant="outline"
                onClick={() => setIsVerifyDialogOpen(false)}
                disabled={isVerifying || verifySuccess}
              >
                Annuler
              </Button>
              <Button
                onClick={handleVerifySubmit}
                disabled={!publicKey.trim() || isVerifying || verifySuccess}
              >
                {isVerifying ? "Vérification..." : "Vérifier"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}

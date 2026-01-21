"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { transactionService } from "@/lib/services/transaction.service"
import type { TransactionResponseDto } from "@/lib/types/transaction"
import { Play, Loader2 } from "lucide-react"

export function CreatedTransactions() {
  const [transactions, setTransactions] = useState<TransactionResponseDto[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedVideo, setSelectedVideo] = useState<string | null>(null)
  const [isVideoDialogOpen, setIsVideoDialogOpen] = useState(false)
  const [isLoadingVideo, setIsLoadingVideo] = useState(false)

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        setIsLoading(true)
        setError(null)
        const data = await transactionService.getCreatedTransactions()
        setTransactions(data)
      } catch (err) {
        console.error("Erreur lors du chargement des transactions:", err)
        setError("Impossible de charger les transactions créées")
      } finally {
        setIsLoading(false)
      }
    }

    fetchTransactions()
  }, [])

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

  const getStatusBadge = (status: string) => {
    if (status === "PENDING") {
      return <Badge variant="outline" className="bg-yellow-500/10 text-yellow-600 border-yellow-500/30">PENDING</Badge>
    }
    return <Badge variant="outline" className="bg-green-500/10 text-green-600 border-green-500/30">VERIFIED</Badge>
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
          <CardTitle className="text-lg font-semibold text-foreground">Transactions créées</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
            </div>
          ) : error ? (
            <div className="text-center py-8 text-destructive">{error}</div>
          ) : transactions.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">Aucune transaction créée</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow className="border-border hover:bg-transparent">
                    <TableHead className="text-muted-foreground">Date</TableHead>
                    <TableHead className="text-muted-foreground">Destinataire</TableHead>
                    <TableHead className="text-muted-foreground">Montant</TableHead>
                    <TableHead className="text-muted-foreground">Vidéo</TableHead>
                    <TableHead className="text-muted-foreground">Statut</TableHead>
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
                      <TableCell className="text-sm">{getStatusBadge(transaction.status)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

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
    </>
  )
}

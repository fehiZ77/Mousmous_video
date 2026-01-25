'use client'

import React, { useEffect, useState } from 'react'
import { DashboardHeader } from '@/components/dashboard-header'
import { DashboardSidebar } from '@/components/dashboard-sidebar'
import { auditService } from '@/lib/services/audit.service'
import { authService } from '@/lib/services/auth.service'
import { Button } from '@/components/ui/button'
import { useToast } from '@/hooks/use-toast'
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert'
import { AlertTriangle, CheckCircle2 } from "lucide-react"
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'

export default function AuditPage() {
  const [files, setFiles] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const { toast } = useToast()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [dialogTitle, setDialogTitle] = useState<string | undefined>('')
  const [dialogDescription, setDialogDescription] = useState<string | undefined>('')
  const [dialogDestructive, setDialogDestructive] = useState(false)

 // Créer une fonction pour charger les fichiers
const loadFiles = async () => {
  const role = authService.getRole()
  if (role !== 'ADMIN') return

  setLoading(true)
  try {
    const res = await auditService.listLogs()
    setFiles(res)
  } catch (err) {
    console.error('Erreur lors de la récupération des logs', err)
    toast({ title: 'Erreur', description: 'Impossible de récupérer la liste des logs', type: 'error' })
  } finally {
    setLoading(false)
  }
}

// Dans useEffect
useEffect(() => {
  loadFiles()
}, [])

  const handleDownload = async (fileName: string) => {
    try {
      const blob = await auditService.download(fileName)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      console.error('Download error', err)
      toast({ title: 'Erreur', description: 'Téléchargement échoué', type: 'error' })
    }
  }

 const handleVerify = async (fileName: string) => {
  try {
    const res = await auditService.verify(fileName)
    const line = res.data as number

    if (line === 0) {
      setDialogTitle('Vérification réussie')
      setDialogDescription('Fichier non corrompu')
      setDialogDestructive(false)
    } else {
      setDialogTitle('Corruption détectée')
      setDialogDescription(`Fichier corrompu à la ligne ${line}`)
      setDialogDestructive(true)
    }

    setDialogOpen(true)
  } catch (err: any) {
    console.error('Verify error', err)
    setDialogTitle('Erreur')
    setDialogDescription('Vérification échouée')
    setDialogDestructive(true)
    setDialogOpen(true)
  }
}


  return (
    <div className="flex min-h-screen bg-background">
      <DashboardSidebar />
      <div className="flex-1 flex flex-col">
        <DashboardHeader />
        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <div>
            <h1 className="text-2xl font-semibold text-foreground">Journal d'audit</h1>
            <p className="text-muted-foreground mt-1">Consultez et vérifiez les fichiers de logs d'audit</p>
          </div>

          {loading && <p>Chargement...</p>}
          {!loading && files.length === 0 && <p>Aucun fichier de log trouvé.</p>}

          <div className="space-y-2">
            {files.map((f) => (
              <div key={f} className="flex items-center justify-between p-3 border rounded">
                <div className="truncate">{f}</div>
                <div className="flex gap-2">
                  <Button size="sm" onClick={() => handleVerify(f)} variant="outline">
                    Verify
                  </Button>
                  <Button size="sm" onClick={() => handleDownload(f)}>Download</Button>
                </div>
              </div>
            ))}
          </div>
        </main>
      </div>
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogContent className="max-w-md">
                <DialogHeader>
                <DialogTitle>{dialogTitle}</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                {dialogDestructive ? (
                    <Alert variant="destructive">
                        <AlertTriangle className="h-4 w-4" />
                        <AlertDescription>{dialogDescription}</AlertDescription>
                    </Alert>
                ) : (
                    <Alert className="bg-emerald-500/10 border-emerald-500/20">
                        <CheckCircle2 className="h-4 w-4 text-emerald-500" />
                        <AlertDescription className="text-emerald-700 dark:text-emerald-400">
                            {dialogDescription}
                        </AlertDescription>
                    </Alert>
                )}
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => {
                        setDialogOpen(false)
                        loadFiles()
                    }}>
                        Fermer
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    </div>
  )
}

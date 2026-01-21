import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { ChevronRight } from "lucide-react"

const logs = [
  {
    id: 1,
    owner_id: "456def",
    recepient_id: "msg_9a8b7c6d",
    mime: "video/mp4",
    size: "192.4 MB",
    sig_alg: "RSA-PSS",
    created_at: "2026-01-10 14:32:15",
    expired_at: "2026-02-02 14:32:15",
  },

]

const actionLabels: Record<string, string> = {
  VIDEO_UPLOAD: "Upload Vidéo",
  VIDEO_READ: "Lecture Vidéo",
  KEY_ROTATION: "Rotation Clé",
  AUTH_LOGIN: "Connexion",
  AUTH_FAILED: "Échec Auth",
}

export function TransactionList() {
  return (
    <Card className="bg-card border-border">
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <CardTitle className="text-lg font-semibold text-foreground">Liste des transactions</CardTitle>
        <Button variant="ghost" size="sm" className="text-muted-foreground">
          Vérifier vidéo
          <ChevronRight className="ml-1 h-4 w-4" />
        </Button>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow className="border-border hover:bg-transparent">
              <TableHead className="text-muted-foreground">Propriétaire</TableHead>
              <TableHead className="text-muted-foreground">Type de fichier</TableHead>
              <TableHead className="text-muted-foreground">Taille</TableHead>
              <TableHead className="text-muted-foreground">Signature</TableHead>
              <TableHead className="text-muted-foreground">Date de création</TableHead>
              <TableHead className="text-muted-foreground">Date d'expiration</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {logs.map((log) => (
              <TableRow key={log.id} className="border-border">
                <TableCell className="text-sm text-foreground">{log.owner_id}</TableCell>
                <TableCell className="text-sm text-muted-foreground font-mono">{log.mime}</TableCell>
                <TableCell className="text-sm text-muted-foreground font-mono">{log.size}</TableCell>
                <TableCell className="text-sm text-muted-foreground">{log.sig_alg}</TableCell>
                <TableCell className="text-sm text-muted-foreground">{log.created_at}</TableCell>
                <TableCell className="text-sm text-muted-foreground">{log.expired_at}</TableCell>
                
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  )
}

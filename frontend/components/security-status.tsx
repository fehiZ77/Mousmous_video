import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Shield, Key, Lock, Server } from "lucide-react"

const securityMetrics = [
  {
    name: "Chiffrement E2EE",
    value: 100,
    status: "Actif",
    icon: Lock,
  },
  {
    name: "Signatures RSA-PSS",
    value: 99.8,
    status: "Opérationnel",
    icon: Key,
  },
  {
    name: "TLS 1.3",
    value: 100,
    status: "Actif",
    icon: Shield,
  },
  {
    name: "mTLS Interne",
    value: 100,
    status: "Actif",
    icon: Server,
  },
]

export function SecurityStatus() {
  return (
    <Card className="bg-card border-border">
      <CardHeader className="pb-3">
        <CardTitle className="text-lg font-semibold text-foreground">État de la Sécurité</CardTitle>
      </CardHeader>
      <CardContent className="space-y-5">
        {securityMetrics.map((metric) => (
          <div key={metric.name} className="space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <metric.icon className="w-4 h-4 text-emerald-500" />
                <span className="text-sm font-medium text-foreground">{metric.name}</span>
              </div>
              <span className="text-xs text-emerald-500 font-medium">{metric.status}</span>
            </div>
            <Progress value={metric.value} className="h-2" />
            <p className="text-xs text-muted-foreground text-right">{metric.value}%</p>
          </div>
        ))}
      </CardContent>
    </Card>
  )
}

import { Card, CardContent } from "@/components/ui/card"
import { Zap, Activity, TrendingUp } from "lucide-react"

const stats = [
  {
    name: "Performance système",
    value: "Excellent",
    change: "99.9%",
    changeType: "positive",
    icon: Zap,
    description: "Temps de réponse optimal",
  },
  {
    name: "Disponibilité",
    value: "24/7",
    change: "100%",
    changeType: "positive",
    icon: Activity,
    description: "Service toujours actif",
  },
]

export function StatsCards() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
      {stats.map((stat) => (
        <Card key={stat.name} className="bg-card border-border hover:shadow-lg transition-shadow">
          <CardContent className="p-6">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-4">
                <div className="flex items-center justify-center w-12 h-12 rounded-lg bg-gradient-to-br from-emerald-500/20 to-emerald-600/10 border border-emerald-500/20">
                  <stat.icon className="w-6 h-6 text-emerald-500" />
                </div>
                <div>
                  <p className="text-2xl font-bold text-foreground">{stat.value}</p>
                  <p className="text-sm font-medium text-foreground mt-1">{stat.name}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{stat.description}</p>
                </div>
              </div>
              <div className="flex flex-col items-end">
                <span
                  className={`text-lg font-semibold ${
                    stat.changeType === "positive" ? "text-emerald-500" : "text-destructive"
                  }`}
                >
                  {stat.change}
                </span>
                <TrendingUp className={`w-4 h-4 mt-1 ${
                  stat.changeType === "positive" ? "text-emerald-500" : "text-destructive"
                }`} />
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}

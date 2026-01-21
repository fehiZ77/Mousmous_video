import { DashboardHeader } from "@/components/dashboard-header"
import { DashboardSidebar } from "@/components/dashboard-sidebar"
import { NotificationTest } from "@/components/notification-test"

export default function NotificationTestPage() {
  return (
    <div className="flex min-h-screen bg-background">
      <DashboardSidebar />
      <div className="flex-1 flex flex-col">
        <DashboardHeader />
        <main className="flex-1 p-6 space-y-6 overflow-auto">
          <div>
            <h1 className="text-2xl font-semibold text-foreground">Test des Notifications</h1>
            <p className="text-muted-foreground mt-1">
              Créez et testez les notifications pour vérifier le fonctionnement du service
            </p>
          </div>
          <NotificationTest />
        </main>
      </div>
    </div>
  )
}
